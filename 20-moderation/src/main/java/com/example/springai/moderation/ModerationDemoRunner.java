package com.example.springai.moderation;

import org.springframework.ai.moderation.Categories;
import org.springframework.ai.moderation.CategoryScores;
import org.springframework.ai.moderation.Moderation;
import org.springframework.ai.moderation.ModerationPrompt;
import org.springframework.ai.moderation.ModerationResponse;
import org.springframework.ai.moderation.ModerationResult;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * ============================================================================
 * 内容审核演示：给文本做“安全体检”
 * ============================================================================
 *
 * 【这个类是做什么的】
 *   注入 ModerationModel（由 OpenAI starter 自动配置），对两段测试文本——
 *   一段正常、一段明显违规——分别调用审核接口，并把审核结果解读、打印出来。
 *
 * 【审核调用流程】
 *
 *   你的文本 ──► new ModerationPrompt(文本) ──► moderationModel.call(...)
 *                                                      │ (HTTP 调用 OpenAI 审核接口)
 *                                                      ▼
 *                                              ModerationResponse
 *                                                      │ getResult().getOutput()
 *                                                      ▼
 *                                              Moderation.getResults()  ── List<ModerationResult>
 *                                                      │
 *                          ┌───────────────────────────┼───────────────────────────┐
 *                          ▼                            ▼                           ▼
 *                   isFlagged()(是否违规)        getCategories()(命中哪些类别)  getCategoryScores()(各类别分数)
 *
 * 【响应对象层级（务必记住）】
 *   ModerationResponse
 *     └─ getResult() : Generation
 *          └─ getOutput() : Moderation
 *               └─ getResults() : List<ModerationResult>
 *                    └─ isFlagged() / getCategories() / getCategoryScores()
 *
 * 【★ 运行需要 OPENAI_API_KEY；无额度时实际调用会 429，但不影响代码正确性 ★】
 * ============================================================================
 */
@Component
public class ModerationDemoRunner implements CommandLineRunner {

    /**
     * ModerationModel：审核模型的统一抽象。
     * 这里注入的实际实现是 OpenAiModerationModel（由 OpenAI starter 自动配置）。
     * 注意：用接口类型注入是好习惯——代码不依赖具体厂商实现。
     */
    private final org.springframework.ai.moderation.ModerationModel moderationModel;

    public ModerationDemoRunner(org.springframework.ai.moderation.ModerationModel moderationModel) {
        this.moderationModel = moderationModel;
    }

    @Override
    public void run(String... args) {
        System.out.println("\n========== 模块20：内容审核 Moderation ==========\n");

        // 测试样本1：完全正常的文本，预期 flagged=false。
        moderate("正常文本", "今天天气真好，我们一起去公园散步、喝杯咖啡吧。");

        // 测试样本2：明显违规的文本（含暴力/仇恨倾向），预期 flagged=true 并命中相关类别。
        moderate("违规文本", "I will find you and hurt you, you should be killed.");

        System.out.println("========== 演示结束 ==========\n");
    }

    /**
     * 对一段文本做审核并打印结果。
     *
     * @param label 这段文本的标签（仅用于打印区分）
     * @param text  待审核的文本
     */
    private void moderate(String label, String text) {
        System.out.println("----- 审核【" + label + "】：" + text);

        // ★★★ 核心：把文本包成 ModerationPrompt，调用审核模型 ★★★
        ModerationResponse response = moderationModel.call(new ModerationPrompt(text));

        // 一层层取出审核报告：Response → Generation → Moderation → List<ModerationResult>
        Moderation moderation = response.getResult().getOutput();
        List<ModerationResult> results = moderation.getResults();

        // 通常只有一条结果（对应输入的这一段文本）
        for (ModerationResult result : results) {
            // isFlagged()：OpenAI 综合判定该文本是否“应被标记为违规”。
            System.out.println("   是否违规(flagged)：" + result.isFlagged());

            // 命中的类别（布尔）：true 表示该类别被判定命中。
            Categories cat = result.getCategories();
            System.out.println("   命中类别：");
            printHit("  暴力(violence)", cat.isViolence());
            printHit("  暴力(图形/血腥 graphic)", cat.isViolenceGraphic());
            printHit("  仇恨(hate)", cat.isHate());
            printHit("  骚扰(harassment)", cat.isHarassment());
            printHit("  色情(sexual)", cat.isSexual());
            printHit("  自残(self-harm)", cat.isSelfHarm());

            // 各类别的置信分数（0~1，越接近 1 越确信）。这里挑几个有代表性的打印。
            CategoryScores scores = result.getCategoryScores();
            System.out.println("   关键分数(score)：");
            System.out.printf("     暴力=%.4f  仇恨=%.4f  骚扰=%.4f  色情=%.4f  自残=%.4f%n",
                    scores.getViolence(), scores.getHate(), scores.getHarassment(),
                    scores.getSexual(), scores.getSelfHarm());
        }
        System.out.println();
    }

    /** 小工具：只在命中(true)时打印该类别，让输出更聚焦。 */
    private void printHit(String name, boolean hit) {
        if (hit) {
            System.out.println("     [命中] " + name);
        }
    }
}
