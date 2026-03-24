package com.promptmanage.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

/**
 * 提示词-标签关联实体
 */
@Data
@TableName("prompt_tag")
public class PromptTag {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 提示词ID */
    private Long promptId;

    /** 标签ID */
    private Long tagId;
}
