package com.example.springai.evaluation;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.evaluation.FactCheckingEvaluator;
import org.springframework.ai.chat.evaluation.RelevancyEvaluator;
import org.springframework.ai.document.Document;
import org.springframework.ai.evaluation.EvaluationRequest;
import org.springframework.ai.evaluation.EvaluationResponse;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * ============================================================================
 * 模型评估演示：相关性（Relevancy）+ 事实性（FactChecking）
 * ============================================================================
 *
 * 【“LLM 当裁判”的整体流程图】
 *
 *   ┌─────────────┐   ①问问题    ┌──────────────┐
 *   │  用户问题    │ ───────────► │ ChatClient   │ ──► 大模型(DeepSeek)
 *   └─────────────┘              └──────────────┘            │
 *          │                                                 │ ②生成回答
 *          │                                                 ▼
 *          │                                          ┌────────────┐
 *          │                                          │  AI 回答    │
 *          │                                          └────────────┘
 *          │            ③把【问题+上下文+回答】打包                 │
 *          └────────────────────┬──────────────────────────────────┘
 *                               ▼
 *                     EvaluationRequest
 *                               │  ④交给“裁判”大模型
 *                               ▼
 *               RelevancyEvaluator / FactCheckingEvaluator
 *                               │  ⑤裁判判定
 *                               ▼
 *                     EvaluationResponse
 *                       isPass() / getScore() / getFeedback()
 *
 * 【两类评估器】
 *   - RelevancyEvaluator（相关性）：判断“回答是否切合用户问题（结合给定上下文）”。
 *   - FactCheckingEvaluator（事实性）：判断“回答是否被给定的上下文/资料所支持”，
 *     即有没有出现上下文里没有依据的内容（“幻觉”）。
 * ============================================================================
 */
@Component
public class EvaluationDemoRunner implements CommandLineRunner {

    /** 用来真正“答题”的对话客户端。 */
    private final ChatClient chatClient;
    /** 相关性裁判：判断回答是否切题。 */
    private final RelevancyEvaluator relevancyEvaluator;
    /** 事实性裁判：判断回答是否被上下文支持。 */
    private final FactCheckingEvaluator factCheckingEvaluator;

    /**
     * 构造器注入。
     * 注意：评估器内部也是“调用大模型来当裁判”，所以它需要一个 ChatClient.Builder。
     * 这里裁判模型直接复用同一个 DeepSeek（纯演示；真实项目可换成更强的模型当裁判）。
     */
    public EvaluationDemoRunner(ChatClient.Builder builder) {
        this.chatClient = builder.build();

        // RelevancyEvaluator：用 builder 模式构造，传入裁判用的 ChatClient.Builder。
        this.relevancyEvaluator = RelevancyEvaluator.builder()
                .chatClientBuilder(builder)   // 裁判模型的客户端构建器
                .build();

        // FactCheckingEvaluator：同样需要一个裁判用的 ChatClient.Builder。
        this.factCheckingEvaluator = FactCheckingEvaluator.builder(builder).build();
    }

    @Override
    public void run(String... args) {
        System.out.println("\n========== 模块15：模型评估（LLM 当裁判）==========\n");
        demo1_relevancy();
        demo2_factChecking();
        System.out.println("\n========== 模块15 演示全部结束 ==========\n");
    }

    /**
     * 演示1：相关性评估。
     * 流程：先让 chatClient 回答一个问题 → 再把【问题 + 上下文 + 回答】交给相关性裁判 → 看是否通过。
     */
    private void demo1_relevancy() {
        System.out.println("===== 演示1：相关性评估 Relevancy =====");

        // 用户问题
        String question = "Spring AI 的 ChatClient 是用来做什么的？";

        // ① 先用对话模型拿到一段真实回答
        String answer = chatClient.prompt().user(question).call().content();
        System.out.println("【问题】" + question);
        System.out.println("【AI 回答】" + answer);

        // ② 准备“上下文资料”（相关性评估会参考它；这里给一段相关说明）
        List<Document> context = List.of(
                new Document("ChatClient 是 Spring AI 提供的高级对话客户端，用链式 API 调用大模型。"));

        // ③ 构造评估请求：EvaluationRequest(用户问题, 上下文资料列表, 待评回答)
        EvaluationRequest request = new EvaluationRequest(question, context, answer);

        // ④ 调用裁判评估，得到结果
        EvaluationResponse response = relevancyEvaluator.evaluate(request);

        // ⑤ 打印结果：isPass()=是否通过；getScore()=分数；getFeedback()=裁判给的理由
        printResult(response);
    }

    /**
     * 演示2：事实性评估。
     * 思路：给定一段“事实上下文”，再给一句“待核查的陈述”，让裁判判断陈述是否被上下文支持。
     * 这里故意构造一句【不被上下文支持】的陈述，期望裁判判定为“不通过”。
     */
    private void demo2_factChecking() {
        System.out.println("\n===== 演示2：事实性评估 FactChecking =====");

        // 事实上下文（被认为是“真相”的资料）
        List<Document> facts = List.of(
                new Document("地球到太阳的平均距离约为 1.5 亿公里。"));

        // 待核查的 AI 陈述（与上下文矛盾，应被判为不通过）
        String claim = "地球到太阳的平均距离约为 100 公里。";

        System.out.println("【上下文事实】地球到太阳约 1.5 亿公里");
        System.out.println("【待核查陈述】" + claim);

        // 事实性评估：dataList=事实上下文，responseContent=待核查陈述
        EvaluationRequest request = new EvaluationRequest(facts, claim);
        EvaluationResponse response = factCheckingEvaluator.evaluate(request);

        printResult(response);
    }

    /** 统一打印评估结果。 */
    private void printResult(EvaluationResponse response) {
        System.out.println("【评估结果】"
                + (response.isPass() ? "✅ 通过" : "❌ 不通过")
                + "  | 分数=" + response.getScore());
        // 部分评估器会在 feedback 里给出判定理由（可能为空）
        String feedback = response.getFeedback();
        if (feedback != null && !feedback.isBlank()) {
            System.out.println("【裁判反馈】" + feedback);
        }
    }
}
