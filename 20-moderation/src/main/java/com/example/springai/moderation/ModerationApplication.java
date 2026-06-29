package com.example.springai.moderation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ============================================================================
 * 模块 20：内容审核（Moderation）—— 启动类
 * ============================================================================
 *
 * 【这个模块是做什么的】
 *   用「审核模型（Moderation Model）」检测一段文本是否包含违规内容
 *   （暴力、仇恨、骚扰、色情、自残等），并拿到：
 *     - 是否被标记违规（flagged）；
 *     - 命中了哪些类别（categories，布尔）；
 *     - 各类别的置信分数（categoryScores，0~1）。
 *
 * 【先理解几个概念（零基础必读）】
 *   1. 内容审核：不是“对话”，而是给模型一段文本，让它判定该文本的安全性，
 *      返回一份“违规体检报告”。常用于 UGC（用户生成内容）过滤、合规风控等场景。
 *   2. ModerationModel：Spring AI 对“审核模型”的统一抽象，调用方式 call(ModerationPrompt)。
 *   3. ★ 重要：审核能力只有 OpenAI 提供，DeepSeek 不支持 ★，所以本模块走真正的 OpenAI，
 *      需要 OPENAI_API_KEY。共享配置里父级 base-url 已是 https://api.openai.com。
 *
 * 【怎么做】
 *   - 引入 spring-ai-starter-model-openai，它会自动配置出 OpenAiModerationModel（即 ModerationModel）。
 *   - 注入 ModerationModel，对“正常文本”和“明显违规文本”分别调用 call(...)，
 *     从响应里取出 flagged / 命中类别 / 分数并打印（见 ModerationDemoRunner）。
 *
 * 【达到的目的】
 *   学会用一行 API 给任意文本做“安全体检”，为合规、社区内容过滤等场景打基础。
 *
 * 【★ 关于运行 ★】
 *   本模块只要求 mvn -q compile 通过。真正运行需要 OPENAI_API_KEY；
 *   若你当前 OpenAI 账户无额度，实际调用会返回 HTTP 429（额度不足）——这不影响代码与配置的正确性。
 * ============================================================================
 */
@SpringBootApplication
public class ModerationApplication {

    public static void main(String[] args) {
        // 启动 Spring Boot 应用：
        //   1. 创建 Spring 容器；
        //   2. OpenAiModerationAutoConfiguration 自动配置出 ModerationModel（OpenAiModerationModel）；
        //   3. 容器就绪后执行 CommandLineRunner（演示在 ModerationDemoRunner 里）。
        SpringApplication.run(ModerationApplication.class, args);
    }
}
