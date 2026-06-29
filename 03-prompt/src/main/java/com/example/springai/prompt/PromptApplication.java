package com.example.springai.prompt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ============================================================================
 * 模块 03：Prompt 提示词模板 —— 启动类
 * ============================================================================
 *
 * 【这个模块是做什么的】
 *   学习 Spring AI 里"提示词（Prompt）"的多种构建方式。提示词就是我们发给大模型的
 *   "话"，写得好不好直接决定回答的质量。本模块演示 4 种由浅入深的写法：
 *     1) 模板变量占位符：在 user()/system() 里写 {变量名}，再用 param() 填值。
 *     2) 底层 PromptTemplate：手动创建模板对象，用 Map 一次性填充所有变量。
 *     3) 消息列表 + 角色：用 SystemMessage / UserMessage 组装成一个 Prompt。
 *     4) 从资源文件加载模板：把提示词写在 classpath 的 .st 文件里，与代码解耦。
 *
 * 【怎么做】
 *   注入 Spring AI 自动配置好的 ChatClient.Builder 构建 ChatClient，
 *   再分别用上面 4 种方式构造提示词并调用大模型。
 *
 * 【达到的目的】
 *   理解"提示词模板 + 变量 + 角色"这套机制，学会把提示词参数化、文件化，
 *   写出可复用、易维护的提示词。这是做 RAG、Agent 等高级应用的基本功。
 * ============================================================================
 */
@SpringBootApplication
public class PromptApplication {
    public static void main(String[] args) {
        SpringApplication.run(PromptApplication.class, args);
    }
}
