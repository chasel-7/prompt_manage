package com.promptmanage;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 提示词词库管理系统启动类
 */
@SpringBootApplication
@MapperScan("com.promptmanage.mapper")
public class PromptManageApplication {

    public static void main(String[] args) {
        SpringApplication.run(PromptManageApplication.class, args);
    }
}
