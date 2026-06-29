package com.example.springai.agents;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ============================================================================
 * 模块 18：构建高效智能体（Building Effective Agents）—— 启动类
 * ============================================================================
 *
 * 【这个模块是做什么的】
 *   前面的模块都是"问一句、答一句"的单次调用。但真正能解决复杂任务的"智能体"，
 *   往往是用代码把【多次 LLM 调用】按一定结构编排起来。本模块用纯 Java + ChatClient
 *   手写编排，演示 Anthropic《Building Effective Agents》中提出、Spring AI 官方
 *   Guides 收录的 4 种经典工作流/智能体模式：
 *     1) 提示链 Chain Workflow ：大任务拆成多步，前一步输出 = 后一步输入。
 *     2) 路由 Routing          ：先分类，再把请求路由到不同的专门提示词处理。
 *     3) 并行化 Parallelization ：同一输入用多个视角并行调用，再汇总。
 *     4) 评估-优化 Evaluator-Optimizer：生成初稿 → 评估给意见 → 据此改进。
 *
 * 【怎么做】
 *   注入 Spring AI 自动配置的 ChatClient.Builder 构建一个共享 ChatClient，
 *   每种模式写一个独立的"工作流类"（普通 Java 类，不依赖任何 Agent 框架），
 *   在 CommandLineRunner 里依次运行并打印每一步的中间产物，让你看清数据如何流动。
 *
 * 【达到的目的】
 *   建立一个关键认知：智能体 = 用代码编排多次 LLM 调用，而不是单次问答。
 *   掌握 4 种可直接套用的编排骨架，能自己拼装出解决复杂任务的流程。
 *
 * 【参考】Spring AI 官方 Guides —— Building Effective Agents
 *        （基于 Anthropic 同名文章的工作流/智能体模式）。
 * ============================================================================
 */
@SpringBootApplication
public class AgentsApplication {
    public static void main(String[] args) {
        SpringApplication.run(AgentsApplication.class, args);
    }
}
