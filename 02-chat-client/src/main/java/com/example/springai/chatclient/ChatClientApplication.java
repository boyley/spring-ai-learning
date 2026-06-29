package com.example.springai.chatclient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ============================================================================
 * 模块 02：ChatClient API —— 启动类
 * ============================================================================
 *
 * 【这个模块是做什么的】
 *   深入学习 Spring AI 最常用的高级客户端 ChatClient 的几种核心用法：
 *     1) 非流式调用（call）：一次性等待完整回答（模块01已用过，这里再巩固）。
 *     2) 流式调用（stream）：像打字机一样，回答一个字一个字地实时返回。
 *     3) System 角色：给 AI 设定"人设/系统指令"，约束它的回答风格。
 *     4) 运行时参数：临时覆盖模型名、temperature 等，无需改配置文件。
 *
 * 【怎么做】
 *   通过注入 ChatClient.Builder 构建 ChatClient，然后用不同的链式 API 演示上述用法。
 *
 * 【达到的目的】
 *   全面掌握 ChatClient 的日常使用姿势，这是后续所有对话类模块的基础。
 * ============================================================================
 */
@SpringBootApplication
public class ChatClientApplication {
    public static void main(String[] args) {
        SpringApplication.run(ChatClientApplication.class, args);
    }
}
