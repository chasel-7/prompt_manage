package com.promptmanage.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 提示词实体
 */
@Data
@TableName("prompt")
public class Prompt {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 标题 */
    private String title;

    /** 提示词内容 */
    private String content;

    /** 描述 */
    private String description;

    /** 所属分组ID */
    private Long groupId;

    /** 是否收藏 0-否 1-是 */
    private Integer isFavorite;

    /** 逻辑删除 0-否 1-是 */
    @TableLogic
    private Integer isDeleted;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /** 标签ID列表（非数据库字段） */
    @TableField(exist = false)
    private java.util.List<Long> tagIds;

    /** 标签名称列表（非数据库字段） */
    @TableField(exist = false)
    private java.util.List<String> tagNames;
}
