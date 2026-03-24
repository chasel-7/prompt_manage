package com.promptmanage.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 助手配置实体
 */
@Data
@TableName("assistant")
public class Assistant {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 助手名称 */
    private String name;

    /** 助手描述 */
    private String description;

    /** 系统提示词 */
    private String systemPrompt;

    /** 图标 emoji */
    private String icon;

    /** 逻辑删除 */
    @TableLogic
    private Integer isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
