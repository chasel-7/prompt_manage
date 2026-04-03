package com.promptmanage.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.promptmanage.common.BusinessException;
import com.promptmanage.config.AiConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * AI 调用服务（适配 OpenAI 兼容接口）
 * 包含重试机制，防止网络抖动
 * 支持前端传入动态模型配置（优先级高于服务端默认配置）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiService {

    private final AiConfig aiConfig;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 使用服务端默认配置调用 AI
     */
    public String chat(String systemPrompt, String userMessage) {
        return chat(systemPrompt, userMessage, null, null, null);
    }

    /**
     * 支持动态模型配置的 AI 调用
     *
     * @param systemPrompt 系统提示词
     * @param userMessage  用户消息
     * @param model        模型名称（优先于配置文件）
     * @param apiKey       API Key（优先于配置文件）
     * @param baseUrl      Base URL（优先于配置文件）
     */
    public String chat(String systemPrompt, String userMessage,
                       String model, String apiKey, String baseUrl) {
        // 合并配置：前端传入优先
        String resolvedModel   = StringUtils.hasText(model)   ? model   : aiConfig.getModel();
        String resolvedApiKey  = StringUtils.hasText(apiKey)  ? apiKey  : aiConfig.getApiKey();
        String resolvedBaseUrl = StringUtils.hasText(baseUrl) ? baseUrl : aiConfig.getBaseUrl();

        // 基础校验
        if (!StringUtils.hasText(resolvedApiKey)) {
            throw new BusinessException("未配置 API Key，请在页面设置中填写或联系管理员配置");
        }
        if (!StringUtils.hasText(resolvedBaseUrl)) {
            throw new BusinessException("未配置 AI 服务地址（Base URL）");
        }

        int retries = aiConfig.getMaxRetries();
        Exception lastException = null;

        for (int i = 0; i < retries; i++) {
            try {
                return doChat(systemPrompt, userMessage, resolvedModel, resolvedApiKey, resolvedBaseUrl);
            } catch (Exception e) {
                lastException = e;
                log.warn("AI 调用第 {} 次失败: {}", i + 1, e.getMessage());
                if (i < retries - 1) {
                    try {
                        Thread.sleep((long) Math.pow(2, i) * 1000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new BusinessException("AI 调用被中断");
                    }
                }
            }
        }

        log.error("AI 调用重试 {} 次后仍失败", retries, lastException);
        throw new BusinessException("AI 调用失败，请稍后重试: " +
                (lastException != null ? lastException.getMessage() : "未知错误"));
    }

    private String doChat(String systemPrompt, String userMessage,
                          String model, String apiKey, String baseUrl) throws Exception {
        String url = baseUrl.replaceAll("/+$", "") + "/v1/chat/completions";

        Map<String, Object> body = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userMessage)
                ),
                "temperature", 0.7
        );

        String jsonBody = objectMapper.writeValueAsString(body);

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(aiConfig.getTimeoutSeconds()))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .timeout(Duration.ofSeconds(aiConfig.getTimeoutSeconds()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("AI API 返回错误: HTTP " + response.statusCode() + " - " + response.body());
        }

        JsonNode root = objectMapper.readTree(response.body());
        return root.path("choices").get(0).path("message").path("content").asText();
    }
}
