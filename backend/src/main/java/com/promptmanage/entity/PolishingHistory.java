package com.promptmanage.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 润色历史记录实体
 */
@Data
@TableName("polishing_history")
public class PolishingHistory {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 原始文本 */
    private String originalText;

    /** 润色后文本 */
    private String polishedText;

    /** 润色策略标识 */
    private String strategy;

    /** 策略名称 */
    private String strategyName;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
