package com.promptmanage.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 提示词分组实体
 */
@Data
@TableName("prompt_group")
public class PromptGroup {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 分组名称 */
    private String name;

    /** 分组描述 */
    private String description;

    /** 排序序号 */
    private Integer sortOrder;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /** 分组下提示词数量（非数据库字段） */
    @TableField(exist = false)
    private Integer promptCount;
}
