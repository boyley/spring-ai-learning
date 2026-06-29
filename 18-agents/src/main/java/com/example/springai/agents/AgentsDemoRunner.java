package com.example.springai.agents;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * ============================================================================
 * 智能体 4 大编排模式总演示
 * ============================================================================
 *
 * 【这个类做什么】依次运行 4 种工作流，展示"用代码编排多次 LLM 调用"如何解决复杂任务。
 *
 * 【怎么做】构建一个共享 ChatClient，分别传给 4 个工作流类去执行，并打印每一步中间产物。
 *
 * 【4 种模式一览 ASCII 图】
 *
 *   提示链   ：A ─► B ─► C            （串行，前一步输出=后一步输入）
 *   路由     ：判类别 ─► 选择某条专线  （先分类，再分流处理）
 *   并行化   ：输入 ═╣ 多视角并行 ╠═► 汇总   （同时跑，再合并）
 *   评估-优化：初稿 ─► 评估 ─► 改进     （自我审稿返工闭环）
 *
 * 【关键认知】这些都不是"问一句答一句"的单次问答，而是把多次调用按结构拼起来的"工作流/智能体"。
 * ============================================================================
 */
@Component
public class AgentsDemoRunner implements CommandLineRunner {

    /** 所有工作流共享的对话客户端（由 spring-ai-starter-model-openai 自动配置）。 */
    private final ChatClient chatClient;

    public AgentsDemoRunner(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @Override
    public void run(String... args) {
        System.out.println("\n========== 模块18：构建高效智能体（4 种编排模式）==========");

        // —— 模式1：提示链 ——
        System.out.println("\n========== 模式1：提示链 Chain Workflow ==========");
        new ChainWorkflow(chatClient).run("坚持晨跑的好处");

        // —— 模式2：路由 ——
        System.out.println("\n========== 模式2：路由 Routing ==========");
        RoutingWorkflow routing = new RoutingWorkflow(chatClient);
        routing.run("我的 App 一打开就闪退，怎么办？");          // 预期 -> 技术
        routing.run("上个月被多扣了一笔会员费，能退吗？");        // 预期 -> 账单

        // —— 模式3：并行化 ——
        System.out.println("\n========== 模式3：并行化 Parallelization ==========");
        new ParallelizationWorkflow(chatClient)
                .run("做一个面向上班族的 15 分钟健康午餐外卖品牌",
                        List.of("市场前景", "技术/供应链可行性", "主要风险"));

        // —— 模式4：评估-优化 ——
        System.out.println("\n========== 模式4：评估-优化 Evaluator-Optimizer ==========");
        new EvaluatorOptimizerWorkflow(chatClient)
                .run("为一款主打“久坐提醒”的智能手表写一句不超过 20 字的广告语。");

        System.out.println("\n========== 模块18 演示全部结束 ==========\n");
    }
}
