package com.promptmanage.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 标签实体
 */
@Data
@TableName("tag")
public class Tag {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 标签名称 */
    private String name;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
