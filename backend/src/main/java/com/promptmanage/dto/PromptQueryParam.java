package com.promptmanage.dto;

import lombok.Data;

/**
 * 提示词查询参数
 */
@Data
public class PromptQueryParam {

    /** 搜索关键词（模糊匹配标题、描述、内容） */
    private String keyword;

    /** 按分组过滤 */
    private Long groupId;

    /** 按标签过滤 */
    private Long tagId;

    /** 仅收藏 */
    private Boolean favorite;

    /** 页码（默认1） */
    private Integer pageNum = 1;

    /** 每页条数（默认10） */
    private Integer pageSize = 10;

    /** 排序字段：created_at / updated_at / title */
    private String sortBy = "updated_at";

    /** 排序方向：asc / desc */
    private String sortOrder = "desc";
}
