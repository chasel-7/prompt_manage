package com.promptmanage.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 提示词创建/编辑请求
 */
@Data
public class PromptRequest {

    /** 标题（必填） */
    @NotBlank(message = "标题不能为空")
    @Size(max = 200, message = "标题长度不能超过200字符")
    private String title;

    /** 内容（必填） */
    @NotBlank(message = "内容不能为空")
    private String content;

    /** 描述（选填） */
    @Size(max = 500, message = "描述长度不能超过500字符")
    private String description;

    /** 所属分组ID */
    private Long groupId;

    /** 标签ID列表 */
    private List<Long> tagIds;
}
