package com.promptmanage.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.promptmanage.common.Result;
import com.promptmanage.dto.PromptRequest;
import com.promptmanage.entity.PolishingHistory;
import com.promptmanage.entity.Prompt;
import com.promptmanage.service.PolishingService;
import com.promptmanage.service.PromptService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 提示词润色 Controller
 */
@RestController
@RequestMapping("/api/polishing")
@RequiredArgsConstructor
public class PolishingController {

    private final PolishingService polishingService;
    private final PromptService promptService;

    /**
     * 执行润色
     * POST /api/polishing
     * Body: {
     *   "text": "原始文本",
     *   "strategy": "professional|structured|specific",
     *   "modelConfig": { "model": "gpt-4o", "apiKey": "sk-...", "baseUrl": "https://..." }
     * }
     */
    @PostMapping
    public Result<PolishingHistory> polish(@RequestBody Map<String, Object> body) {
        String text = (String) body.get("text");
        String strategy = (String) body.get("strategy");

        // 解析前端传入的模型配置（可选）
        String model = null;
        String apiKey = null;
        String baseUrl = null;
        Object mc = body.get("modelConfig");
        if (mc instanceof Map<?, ?> modelConfig) {
            model   = (String) modelConfig.get("model");
            apiKey  = (String) modelConfig.get("apiKey");
            baseUrl = (String) modelConfig.get("baseUrl");
        }

        return Result.success(polishingService.polish(text, strategy, model, apiKey, baseUrl));
    }

    /**
     * 查询润色历史
     * GET /api/polishing/history?pageNum=1&pageSize=10
     */
    @GetMapping("/history")
    public Result<Page<PolishingHistory>> listHistory(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(polishingService.listHistory(pageNum, pageSize));
    }

    /**
     * 删除历史记录
     * DELETE /api/polishing/history/{id}
     */
    @DeleteMapping("/history/{id}")
    public Result<Void> deleteHistory(@PathVariable Long id) {
        polishingService.deleteHistory(id);
        return Result.success();
    }

    /**
     * 将润色结果保存到词库
     * POST /api/polishing/save-to-library
     */
    @PostMapping("/save-to-library")
    public Result<Prompt> saveToLibrary(@RequestBody PromptRequest request) {
        return Result.success(promptService.createPrompt(request));
    }
}
