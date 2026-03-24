package com.promptmanage.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 分组创建/编辑请求
 */
@Data
public class GroupRequest {

    @NotBlank(message = "分组名称不能为空")
    @Size(max = 100, message = "分组名称长度不能超过100字符")
    private String name;

    @Size(max = 500, message = "分组描述长度不能超过500字符")
    private String description;

    /** 排序序号 */
    private Integer sortOrder;
}
