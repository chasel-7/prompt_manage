package com.promptmanage.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.promptmanage.common.BusinessException;
import com.promptmanage.entity.Assistant;
import com.promptmanage.entity.ChatMessage;
import com.promptmanage.entity.Conversation;
import com.promptmanage.mapper.AssistantMapper;
import com.promptmanage.mapper.ChatMessageMapper;
import com.promptmanage.mapper.ConversationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 对话管理服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationService {

    private final ConversationMapper conversationMapper;
    private final ChatMessageMapper chatMessageMapper;
    private final AssistantMapper assistantMapper;
    private final AiService aiService;

    /**
     * 获取助手的所有对话
     */
    public List<Conversation> listConversations(Long assistantId) {
        LambdaQueryWrapper<Conversation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Conversation::getAssistantId, assistantId)
               .orderByDesc(Conversation::getUpdatedAt);
        List<Conversation> list = conversationMapper.selectList(wrapper);

        // 填充助手信息
        if (!list.isEmpty()) {
            Assistant assistant = assistantMapper.selectById(assistantId);
            if (assistant != null) {
                list.forEach(c -> {
                    c.setAssistantName(assistant.getName());
                    c.setAssistantIcon(assistant.getIcon());
                });
            }
        }
        return list;
    }

    /**
     * 创建新对话
     */
    public Conversation createConversation(Long assistantId, String title) {
        Assistant assistant = assistantMapper.selectById(assistantId);
        if (assistant == null) {
            throw new BusinessException("助手不存在");
        }

        Conversation conv = new Conversation();
        conv.setAssistantId(assistantId);
        conv.setTitle(title != null && !title.trim().isEmpty() ? title.trim() : "新对话");
        conv.setCreatedAt(LocalDateTime.now());
        conv.setUpdatedAt(LocalDateTime.now());
        conversationMapper.insert(conv);

        conv.setAssistantName(assistant.getName());
        conv.setAssistantIcon(assistant.getIcon());
        return conv;
    }

    /**
     * 获取对话的所有消息
     */
    public List<ChatMessage> getMessages(Long conversationId) {
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessage::getConversationId, conversationId)
               .orderByAsc(ChatMessage::getCreatedAt);
        return chatMessageMapper.selectList(wrapper);
    }

    /**
     * 发送消息并获取 AI 回复
     */
    @Transactional(rollbackFor = Exception.class)
    public ChatMessage sendMessage(Long conversationId, String userContent) {
        if (userContent == null || userContent.trim().isEmpty()) {
            throw new BusinessException("消息内容不能为空");
        }

        Conversation conv = conversationMapper.selectById(conversationId);
        if (conv == null) {
            throw new BusinessException("对话不存在");
        }

        Assistant assistant = assistantMapper.selectById(conv.getAssistantId());
        if (assistant == null) {
            throw new BusinessException("助手不存在");
        }

        // 保存用户消息
        ChatMessage userMsg = new ChatMessage();
        userMsg.setConversationId(conversationId);
        userMsg.setRole("user");
        userMsg.setContent(userContent.trim());
        userMsg.setCreatedAt(LocalDateTime.now());
        chatMessageMapper.insert(userMsg);

        // 构建上下文：system prompt + 历史消息
        List<ChatMessage> history = getMessages(conversationId);

        // 拼接历史消息为上下文（最多保留最近 20 条）
        List<ChatMessage> contextMessages = history;
        if (contextMessages.size() > 20) {
            contextMessages = contextMessages.subList(contextMessages.size() - 20, contextMessages.size());
        }

        StringBuilder contextBuilder = new StringBuilder();
        for (ChatMessage msg : contextMessages) {
            if ("user".equals(msg.getRole())) {
                contextBuilder.append("用户: ").append(msg.getContent()).append("\n\n");
            } else if ("assistant".equals(msg.getRole())) {
                contextBuilder.append("助手: ").append(msg.getContent()).append("\n\n");
            }
        }

        // 调用 AI
        String aiReply = aiService.chat(assistant.getSystemPrompt(), contextBuilder.toString());

        // 保存 AI 回复
        ChatMessage aiMsg = new ChatMessage();
        aiMsg.setConversationId(conversationId);
        aiMsg.setRole("assistant");
        aiMsg.setContent(aiReply);
        aiMsg.setCreatedAt(LocalDateTime.now());
        chatMessageMapper.insert(aiMsg);

        // 更新对话时间
        conv.setUpdatedAt(LocalDateTime.now());
        // 如果是第一条消息，用用户消息前30字作标题
        if (history.size() <= 1) {
            String autoTitle = userContent.trim();
            if (autoTitle.length() > 30) {
                autoTitle = autoTitle.substring(0, 30) + "...";
            }
            conv.setTitle(autoTitle);
        }
        conversationMapper.updateById(conv);

        return aiMsg;
    }

    /**
     * 删除对话及其所有消息
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteConversation(Long id) {
        // 删除消息
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessage::getConversationId, id);
        chatMessageMapper.delete(wrapper);

        conversationMapper.deleteById(id);
    }

    /**
     * 导出对话为 Markdown
     */
    public String exportMarkdown(Long conversationId) {
        Conversation conv = conversationMapper.selectById(conversationId);
        if (conv == null) {
            throw new BusinessException("对话不存在");
        }

        Assistant assistant = assistantMapper.selectById(conv.getAssistantId());
        List<ChatMessage> messages = getMessages(conversationId);

        StringBuilder md = new StringBuilder();
        md.append("# ").append(conv.getTitle()).append("\n\n");
        md.append("> 助手: ").append(assistant != null ? assistant.getName() : "未知").append("\n\n");
        md.append("---\n\n");

        for (ChatMessage msg : messages) {
            if ("user".equals(msg.getRole())) {
                md.append("## 👤 用户\n\n");
            } else if ("assistant".equals(msg.getRole())) {
                md.append("## 🤖 助手\n\n");
            }
            md.append(msg.getContent()).append("\n\n");
        }

        return md.toString();
    }
}
