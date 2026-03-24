package com.promptmanage.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 对话实体
 */
@Data
@TableName("conversation")
public class Conversation {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 助手ID */
    private Long assistantId;

    /** 对话标题 */
    private String title;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /** 助手名称（非数据库字段） */
    @TableField(exist = false)
    private String assistantName;

    /** 助手图标（非数据库字段） */
    @TableField(exist = false)
    private String assistantIcon;
}
