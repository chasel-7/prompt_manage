package com.promptmanage.controller;

import com.promptmanage.common.Result;
import com.promptmanage.entity.ChatMessage;
import com.promptmanage.entity.Conversation;
import com.promptmanage.service.ConversationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * 对话管理 Controller
 */
@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;

    /** 获取某助手的所有对话 */
    @GetMapping
    public Result<List<Conversation>> list(@RequestParam Long assistantId) {
        return Result.success(conversationService.listConversations(assistantId));
    }

    /** 创建新对话 */
    @PostMapping
    public Result<Conversation> create(@RequestBody Map<String, Object> body) {
        Long assistantId = Long.valueOf(body.get("assistantId").toString());
        String title = body.get("title") != null ? body.get("title").toString() : null;
        return Result.success(conversationService.createConversation(assistantId, title));
    }

    /** 获取对话消息列表 */
    @GetMapping("/{id}/messages")
    public Result<List<ChatMessage>> getMessages(@PathVariable Long id) {
        return Result.success(conversationService.getMessages(id));
    }

    /** 发送消息 */
    @PostMapping("/{id}/messages")
    public Result<ChatMessage> sendMessage(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String content = body.get("content");
        return Result.success(conversationService.sendMessage(id, content));
    }

    /** 删除对话 */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        conversationService.deleteConversation(id);
        return Result.success();
    }

    /** 导出对话为 Markdown */
    @GetMapping("/{id}/export")
    public ResponseEntity<byte[]> exportMarkdown(@PathVariable Long id) {
        String markdown = conversationService.exportMarkdown(id);
        byte[] bytes = markdown.getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=conversation_" + id + ".md")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(bytes);
    }
}
