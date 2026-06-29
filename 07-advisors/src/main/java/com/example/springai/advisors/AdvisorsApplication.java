package com.example.springai.advisors;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ============================================================================
 * 模块 07：Advisor（顾问 / 拦截器）—— 启动类
 * ============================================================================
 *
 * 【这个模块是做什么的】
 *   学习 Spring AI 的 Advisor 机制。Advisor 非常像 Spring 的 AOP（面向切面编程）：
 *   它能在"对话请求真正发给大模型之前"和"模型返回响应之后"插入自己的逻辑，
 *   比如：打印日志、统计调用耗时、改写请求内容、给请求挂上记忆/检索结果等。
 *
 *   ★ 重要认知：Spring AI 的"对话记忆(Chat Memory)"、"RAG 检索增强"等高级能力，
 *     底层全都是用 Advisor 实现的。掌握 Advisor，就掌握了这些能力的扩展点。
 *
 * 【怎么做】
 *   1) 使用内置的 SimpleLoggerAdvisor，它会自动打印每次请求与响应（需把日志级别调到 DEBUG）。
 *   2) 自己写一个 ElapsedTimeAdvisor（实现 CallAdvisor 接口），统计并打印每次调用耗时。
 *   3) 把这两个 Advisor 注册到 ChatClient，发起一次调用，观察它们的执行顺序。
 *
 * 【达到的目的】
 *   理解 Advisor 的"责任链"模型与执行顺序（order），学会自定义增强逻辑，
 *   为后续学习"对话记忆"和"RAG"打下基础。
 * ============================================================================
 */
@SpringBootApplication
public class AdvisorsApplication {
    public static void main(String[] args) {
        SpringApplication.run(AdvisorsApplication.class, args);
    }
}
