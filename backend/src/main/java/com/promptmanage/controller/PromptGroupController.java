package com.promptmanage.controller;

import com.promptmanage.common.Result;
import com.promptmanage.dto.GroupRequest;
import com.promptmanage.entity.PromptGroup;
import com.promptmanage.service.PromptGroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 提示词分组 Controller
 */
@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class PromptGroupController {

    private final PromptGroupService promptGroupService;

    /**
     * 获取所有分组
     * GET /api/groups
     */
    @GetMapping
    public Result<List<PromptGroup>> listGroups() {
        return Result.success(promptGroupService.listGroups());
    }

    /**
     * 创建分组
     * POST /api/groups
     */
    @PostMapping
    public Result<PromptGroup> createGroup(@Valid @RequestBody GroupRequest request) {
        return Result.success(promptGroupService.createGroup(request));
    }

    /**
     * 更新分组
     * PUT /api/groups/{id}
     */
    @PutMapping("/{id}")
    public Result<PromptGroup> updateGroup(@PathVariable Long id, @Valid @RequestBody GroupRequest request) {
        return Result.success(promptGroupService.updateGroup(id, request));
    }

    /**
     * 删除分组
     * DELETE /api/groups/{id}
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteGroup(@PathVariable Long id) {
        promptGroupService.deleteGroup(id);
        return Result.success();
    }
}
