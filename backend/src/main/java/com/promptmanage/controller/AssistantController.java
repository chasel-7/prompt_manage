package com.promptmanage.controller;

import com.promptmanage.common.Result;
import com.promptmanage.entity.Assistant;
import com.promptmanage.service.AssistantService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 助手管理 Controller
 */
@RestController
@RequestMapping("/api/assistants")
@RequiredArgsConstructor
public class AssistantController {

    private final AssistantService assistantService;

    /** 获取所有助手 */
    @GetMapping
    public Result<List<Assistant>> list() {
        return Result.success(assistantService.listAssistants());
    }

    /** 获取助手详情 */
    @GetMapping("/{id}")
    public Result<Assistant> get(@PathVariable Long id) {
        return Result.success(assistantService.getAssistant(id));
    }

    /** 创建助手 */
    @PostMapping
    public Result<Assistant> create(@RequestBody Assistant assistant) {
        return Result.success(assistantService.createAssistant(assistant));
    }

    /** 更新助手 */
    @PutMapping("/{id}")
    public Result<Assistant> update(@PathVariable Long id, @RequestBody Assistant assistant) {
        return Result.success(assistantService.updateAssistant(id, assistant));
    }

    /** 删除助手 */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        assistantService.deleteAssistant(id);
        return Result.success();
    }
}
