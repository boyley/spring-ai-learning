package com.example.springai.agents;

import org.springframework.ai.chat.client.ChatClient;

/**
 * ============================================================================
 * 模式4：评估-优化 Evaluator-Optimizer
 * ============================================================================
 *
 * 【是什么】用两个"角色"配合干活：一个负责"生成"，一个负责"评估并提改进意见"。
 *          先出初稿 → 评估给意见 → 据意见生成改进版，如此可循环若干轮，质量层层提升。
 *
 * 【何时用】对输出质量要求较高、且"好不好"有明确评判标准时（如写文案、写代码、翻译润色）。
 *          相当于给模型加了一个"自我审稿 + 返工"的闭环。
 *
 * 【数据流动 ASCII 图】
 *
 *   任务 ──► [生成器: 写初稿] ──► 初稿 ──► [评估器: 找问题给意见] ──► 改进意见
 *                                                                        │
 *                            ┌───────────────────────────────────────────┘
 *                            ▼
 *                      [生成器: 按意见改] ──► 改进版（可再回到评估器，形成循环）
 *
 *   本例演示一轮"生成→评估→改进"。把循环次数加大即可多轮迭代。
 * ============================================================================
 */
public class EvaluatorOptimizerWorkflow {

    private final ChatClient chatClient;

    public EvaluatorOptimizerWorkflow(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    /**
     * 执行一轮"生成初稿 → 评估 → 据意见改进"。
     *
     * @param task 写作/创作任务
     * @return 改进后的最终版本
     */
    public String run(String task) {
        // —— 第1步：生成器产出初稿 ——
        String draft = chatClient.prompt()
                .system("你是文案写手，请完成用户的写作任务。")
                .user(task)
                .call()
                .content();
        System.out.println("【初稿】\n" + draft);

        // —— 第2步：评估器审稿，针对初稿给出具体、可执行的改进意见（不直接改写）——
        String feedback = chatClient.prompt()
                .system("你是严格的编辑，请挑出文案的不足，给出 2~3 条具体、可执行的改进意见。只列意见，不要改写。")
                .user("请评估下面这份文案，任务要求是：" + task + "\n文案：\n" + draft)
                .call()
                .content();
        System.out.println("\n【评估意见】\n" + feedback);

        // —— 第3步：生成器根据评估意见，产出改进版（把初稿 + 意见一起喂回去）——
        String improved = chatClient.prompt()
                .system("你是文案写手，请严格依据编辑的改进意见修改文案，直接输出修改后的最终版本。")
                .user("原任务：" + task
                        + "\n初稿：\n" + draft
                        + "\n编辑的改进意见：\n" + feedback)
                .call()
                .content();
        System.out.println("\n【改进版（最终）】\n" + improved);

        return improved;
    }
}
