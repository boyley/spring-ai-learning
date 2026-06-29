package com.example.springai.overview;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ============================================================================
 * 模块 01：Spring AI 概念与快速上手 —— 启动类
 * ============================================================================
 *
 * 【这个模块是做什么的】
 *   带你认识 Spring AI 的最核心概念，并完成"人生第一个" Spring AI 调用：
 *   向大模型提一个问题，拿到它的回答并打印出来。
 *
 * 【需要先懂的几个概念（零基础必读）】
 *   1. 大模型(LLM)：像 DeepSeek、GPT 这样的 AI，输入文字、输出文字。
 *   2. ChatModel：Spring AI 对"对话大模型"的底层抽象（直接对接 HTTP 接口）。
 *   3. ChatClient：建立在 ChatModel 之上的"高级客户端"，链式调用，最常用（本项目主角）。
 *   4. Starter + 自动配置：我们只在 pom 里引入 spring-ai-starter-model-openai，
 *      Spring Boot 就会自动帮我们创建好 ChatClient.Builder，拿来即用。
 *
 * 【怎么做】
 *   - @SpringBootApplication 标记这是一个 Spring Boot 应用的入口。
 *   - main 方法启动 Spring 容器，容器启动后会自动执行实现了 CommandLineRunner
 *     的 Bean（见 QuickStartRunner），从而触发我们的演示代码。
 *
 * 【达到的目的】
 *   跑起来后，控制台会打印出大模型对一个问题的真实回答，证明你的环境与 Key 配置成功。
 * ============================================================================
 */
@SpringBootApplication
public class OverviewApplication {

    public static void main(String[] args) {
        // 启动 Spring Boot 应用。这一行会：
        //   1. 创建 Spring 容器（IoC 容器）
        //   2. 触发自动配置（根据 starter + 配置文件，自动创建 ChatClient.Builder 等 Bean）
        //   3. 容器就绪后，执行所有 CommandLineRunner（我们的演示在 QuickStartRunner 里）
        SpringApplication.run(OverviewApplication.class, args);
    }
}
