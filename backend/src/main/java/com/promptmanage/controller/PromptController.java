package com.promptmanage.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.promptmanage.common.Result;
import com.promptmanage.dto.PromptQueryParam;
import com.promptmanage.dto.PromptRequest;
import com.promptmanage.entity.Prompt;
import com.promptmanage.service.PromptService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;


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

    /**
     * 下载导入模板
     * GET /api/prompts/import-template
     */
    @GetMapping("/import-template")
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        byte[] bytes = promptService.buildImportTemplate();
        String filename = URLEncoder.encode("提示词导入模板.xlsx", StandardCharsets.UTF_8);
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + filename);
        response.getOutputStream().write(bytes);
    }

    /**
     * 导出提示词为 Excel
     * GET /api/prompts/export
     */
    @GetMapping("/export")
    public void exportPrompts(PromptQueryParam param, HttpServletResponse response) throws IOException {
        byte[] bytes = promptService.exportToExcel(param);
        String filename = URLEncoder.encode("提示词导出_" + LocalDate.now() + ".xlsx", StandardCharsets.UTF_8);
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + filename);
        response.getOutputStream().write(bytes);
    }

    /**
     * 导入提示词 Excel
     * POST /api/prompts/import
     */
    @PostMapping("/import")
    public Result<java.util.Map<String, Integer>> importPrompts(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return Result.error("请上传有效的 Excel 文件");
        }
        return Result.success(promptService.importFromExcel(file));
    }
}

