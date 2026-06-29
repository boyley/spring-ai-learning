package com.example.springai.prompteng;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ============================================================================
 * 模块 17：提示工程模式（Prompt Engineering Patterns）—— 启动类
 * ============================================================================
 *
 * 【这个模块是做什么的】
 *   "提示工程"就是"如何把话说好，让大模型给出更准、更稳、更符合预期的回答"的
 *   一整套方法论。同样一个问题，换一种问法，模型的表现可能天差地别。
 *   本模块用 ChatClient 系统化演示 5 种最核心、最常用的提示模式：
 *     1) 零样本 Zero-shot：直接提问，不给任何例子。
 *     2) 少样本 Few-shot：在提示里塞几个"输入→输出"示例，让模型照葫芦画瓢。
 *     3) 角色/人设 Role/Persona：用 system 指令给模型设定专家身份，提升专业度。
 *     4) 思维链 Chain-of-Thought：要求模型"一步步推理"再给答案，提升复杂推理正确率。
 *     5) 参数控制：用 OpenAiChatOptions 调 temperature / topP，控制输出的确定性与创造性。
 *
 * 【怎么做】
 *   注入 Spring AI 自动配置好的 ChatClient.Builder 构建 ChatClient，
 *   然后在一个 CommandLineRunner 里，每种模式写一个独立方法分别演示，
 *   并对"加 vs 不加"某模式做对比，让你直观感受效果差异。
 *
 * 【达到的目的】
 *   让初学者建立"提示词是可以工程化设计的"这一认知，
 *   掌握 5 种即学即用的提示套路，写出更高质量的提示词。
 *
 * 【参考】Spring AI 官方 Guides —— Prompt Engineering Patterns。
 * ============================================================================
 */
@SpringBootApplication
public class PromptEngineeringApplication {
    public static void main(String[] args) {
        SpringApplication.run(PromptEngineeringApplication.class, args);
    }
}
