package com.promptmanage.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.promptmanage.common.Result;
import com.promptmanage.dto.PromptQueryParam;
import com.promptmanage.dto.PromptRequest;
import com.promptmanage.entity.Prompt;
import com.promptmanage.service.PromptService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 提示词管理 Controller
 */
@RestController
@RequestMapping("/api/prompts")
@RequiredArgsConstructor
public class PromptController {

    private final PromptService promptService;

    /**
     * 分页查询提示词列表
     * GET
     * /api/prompts?keyword=xxx&groupId=1&tagId=2&favorite=true&pageNum=1&pageSize=10&sortBy=updated_at&sortOrder=desc
     */
    @GetMapping
    public Result<Page<Prompt>> listPrompts(PromptQueryParam param) {
        return Result.success(promptService.listPrompts(param));
    }

    /**
     * 查询提示词详情
     * GET /api/prompts/{id}
     */
    @GetMapping("/{id}")
    public Result<Prompt> getPrompt(@PathVariable Long id) {
        return Result.success(promptService.getPromptById(id));
    }

    /**
     * 创建提示词
     * POST /api/prompts
     */
    @PostMapping
    public Result<Prompt> createPrompt(@Valid @RequestBody PromptRequest request) {
        return Result.success(promptService.createPrompt(request));
    }

    /**
     * 更新提示词
     * PUT /api/prompts/{id}
     */
    @PutMapping("/{id}")
    public Result<Prompt> updatePrompt(@PathVariable Long id, @Valid @RequestBody PromptRequest request) {
        return Result.success(promptService.updatePrompt(id, request));
    }

    /**
     * 逻辑删除提示词
     * DELETE /api/prompts/{id}
     */
    @DeleteMapping("/{id}")
    public Result<Void> deletePrompt(@PathVariable Long id) {
        promptService.deletePrompt(id);
        return Result.success();
    }

    /**
     * 切换收藏状态
     * PUT /api/prompts/{id}/favorite
     */
    @PutMapping("/{id}/favorite")
    public Result<Prompt> toggleFavorite(@PathVariable Long id) {
        return Result.success(promptService.toggleFavorite(id));
    }

    /**
     * 查询回收站列表
     * GET /api/prompts/trash?pageNum=1&pageSize=10
     */
    @GetMapping("/trash")
    public Result<Page<Prompt>> listTrash(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(promptService.listTrash(pageNum, pageSize));
    }

    /**
     * 恢复已删除的提示词
     * PUT /api/prompts/{id}/restore
     */
    @PutMapping("/{id}/restore")
    public Result<Void> restorePrompt(@PathVariable Long id) {
        promptService.restorePrompt(id);
        return Result.success();
    }

    /**
     * 永久删除提示词
     * DELETE /api/prompts/{id}/force
     */
    @DeleteMapping("/{id}/force")
    public Result<Void> forceDeletePrompt(@PathVariable Long id) {
        promptService.forceDeletePrompt(id);
        return Result.success();
    }
}
