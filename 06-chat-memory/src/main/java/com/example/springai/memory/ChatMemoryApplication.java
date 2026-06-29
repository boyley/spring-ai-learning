package com.example.springai.memory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ============================================================================
 * 模块 06：对话记忆（Chat Memory）—— 启动类
 * ============================================================================
 *
 * 【这个模块是做什么的】
 *   让 AI"记住"前面说过的话，实现多轮连续对话。
 *   比如第一轮告诉它"我叫小明，今年18岁"，第二轮问"我叫什么名字？"它还能答出来。
 *
 * 【为什么需要它】
 *   大模型本身是"无状态"的——每次调用它都不记得上一次说了什么。
 *   要实现连续对话，必须由我们的程序把"历史消息"存起来，并在下次调用时一起带上。
 *   Spring AI 用 ChatMemory（记忆仓库）+ MessageChatMemoryAdvisor（顾问/拦截器）
 *   自动完成"存历史 + 下次自动带上历史"这件事，我们几乎不用手写。
 *
 * 【怎么做】
 *   1) 建一个 ChatMemory（这里用滑动窗口记忆 MessageWindowChatMemory，最多记 10 条）。
 *   2) 把它包成 MessageChatMemoryAdvisor，挂到 ChatClient 上（defaultAdvisors）。
 *   3) 每次调用时用一个"会话 ID"（CONVERSATION_ID）区分是谁的对话。
 *      同一个会话 ID 共享同一段记忆；不同会话 ID 互相隔离、互不知情。
 *
 * 【达到的目的】
 *   理解对话记忆的原理，掌握用 ChatMemory + Advisor 实现多轮对话与会话隔离。
 *
 * 【模型说明】
 *   本模块用对话即可，沿用共享配置里的 DeepSeek（无需切换模型）。
 * ============================================================================
 */
@SpringBootApplication
public class ChatMemoryApplication {
    public static void main(String[] args) {
        SpringApplication.run(ChatMemoryApplication.class, args);
    }
}
