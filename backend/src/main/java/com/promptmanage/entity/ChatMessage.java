package com.promptmanage.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 对话消息实体
 */
@Data
@TableName("chat_message")
public class ChatMessage {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 对话ID */
    private Long conversationId;

    /** 角色：user / assistant / system */
    private String role;

    /** 消息内容 */
    private String content;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
