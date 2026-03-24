package com.promptmanage.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * AI 接口配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "ai")
public class AiConfig {

    /** API 密钥 */
    private String apiKey;

    /** API 基础地址 */
    private String baseUrl;

    /** 模型名称 */
    private String model;

    /** 最大重试次数 */
    private int maxRetries = 3;

    /** 超时时间（秒） */
    private int timeoutSeconds = 60;
}
