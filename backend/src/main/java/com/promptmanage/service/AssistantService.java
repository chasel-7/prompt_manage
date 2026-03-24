package com.promptmanage.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.promptmanage.common.BusinessException;
import com.promptmanage.entity.Assistant;
import com.promptmanage.mapper.AssistantMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 助手管理服务
 */
@Service
@RequiredArgsConstructor
public class AssistantService {

    private final AssistantMapper assistantMapper;

    /**
     * 获取所有助手
     */
    public List<Assistant> listAssistants() {
        LambdaQueryWrapper<Assistant> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Assistant::getUpdatedAt);
        return assistantMapper.selectList(wrapper);
    }

    /**
     * 获取助手详情
     */
    public Assistant getAssistant(Long id) {
        Assistant assistant = assistantMapper.selectById(id);
        if (assistant == null) {
            throw new BusinessException("助手不存在");
        }
        return assistant;
    }

    /**
     * 创建助手
     */
    public Assistant createAssistant(Assistant assistant) {
        if (assistant.getName() == null || assistant.getName().trim().isEmpty()) {
            throw new BusinessException("助手名称不能为空");
        }
        if (assistant.getSystemPrompt() == null || assistant.getSystemPrompt().trim().isEmpty()) {
            throw new BusinessException("系统提示词不能为空");
        }
        if (assistant.getIcon() == null || assistant.getIcon().trim().isEmpty()) {
            assistant.setIcon("🤖");
        }
        assistant.setIsDeleted(0);
        assistant.setCreatedAt(LocalDateTime.now());
        assistant.setUpdatedAt(LocalDateTime.now());
        assistantMapper.insert(assistant);
        return assistant;
    }

    /**
     * 更新助手
     */
    public Assistant updateAssistant(Long id, Assistant updated) {
        Assistant assistant = assistantMapper.selectById(id);
        if (assistant == null) {
            throw new BusinessException("助手不存在");
        }
        assistant.setName(updated.getName());
        assistant.setDescription(updated.getDescription());
        assistant.setSystemPrompt(updated.getSystemPrompt());
        if (updated.getIcon() != null) {
            assistant.setIcon(updated.getIcon());
        }
        assistant.setUpdatedAt(LocalDateTime.now());
        assistantMapper.updateById(assistant);
        return assistant;
    }

    /**
     * 删除助手
     */
    public void deleteAssistant(Long id) {
        if (assistantMapper.selectById(id) == null) {
            throw new BusinessException("助手不存在");
        }
        assistantMapper.deleteById(id);
    }
}
