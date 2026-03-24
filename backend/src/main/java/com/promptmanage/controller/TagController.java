package com.promptmanage.controller;

import com.promptmanage.common.Result;
import com.promptmanage.entity.Tag;
import com.promptmanage.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 标签 Controller
 */
@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    /**
     * 获取所有标签
     * GET /api/tags
     */
    @GetMapping
    public Result<List<Tag>> listTags() {
        return Result.success(tagService.listTags());
    }

    /**
     * 创建标签
     * POST /api/tags
     * Body: { "name": "标签名" }
     */
    @PostMapping
    public Result<Tag> createTag(@RequestBody Map<String, String> body) {
        String name = body.get("name");
        return Result.success(tagService.createTag(name));
    }

    /**
     * 删除标签
     * DELETE /api/tags/{id}
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteTag(@PathVariable Long id) {
        tagService.deleteTag(id);
        return Result.success();
    }
}
