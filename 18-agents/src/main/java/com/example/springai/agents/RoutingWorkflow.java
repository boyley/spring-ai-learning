package com.example.springai.agents;

import org.springframework.ai.chat.client.ChatClient;

import java.util.Map;

/**
 * ============================================================================
 * 模式2：路由 Routing
 * ============================================================================
 *
 * 【是什么】先用一次 LLM 调用对输入做"分类/分流"，再根据分类结果，
 *          把请求交给最合适的那套"专门提示词"去处理。
 *
 * 【何时用】不同类型的请求需要不同处理方式时（如客服系统：技术问题、账单问题、其它，
 *          各自需要不同语气、不同知识背景的回答）。先分类再分流，比"一套提示走天下"更好。
 *
 * 【数据流动 ASCII 图】
 *
 *                              ┌──► 命中"技术"  ──► [技术专家 system 提示] ──► 回答
 *   用户问题 ──► [分类LLM调用] ─┼──► 命中"账单"  ──► [账单专员 system 提示] ──► 回答
 *                  得到类别     └──► 命中"其它"  ──► [通用客服 system 提示] ──► 回答
 *
 *   第一次调用只负责"判类别"，第二次调用才真正"解决问题"。
 * ============================================================================
 */
public class RoutingWorkflow {

    private final ChatClient chatClient;

    /**
     * 每个类别对应一段专门的 system 人设提示词。
     * 路由命中哪个类别，就用对应的 system 去回答用户问题。
     */
    private final Map<String, String> routes = Map.of(
            "技术", "你是资深技术支持工程师，请给出清晰的排查步骤和解决方案，语气专业耐心。",
            "账单", "你是账单客服专员，请围绕费用、退款、发票等问题给出准确、合规的答复，语气礼貌。",
            "其它", "你是通用客服，请友好地接待用户，必要时引导用户提供更多信息。"
    );

    public RoutingWorkflow(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    /**
     * 先分类，再路由到对应 system 提示处理。
     *
     * @param question 用户问题
     * @return 由对应专门人设给出的回答
     */
    public String run(String question) {
        // —— 第1次调用：分类。强约束只输出三个词之一，方便后续用作 Map 的 key ——
        String category = chatClient.prompt()
                .user("把下面的用户问题归类，只能回答以下三个词之一：技术、账单、其它。"
                        + "只输出这一个词，不要任何标点或解释。\n用户问题：" + question)
                .call()
                .content()
                .trim();

        // 容错：万一模型多输出了字，做个兜底匹配；匹配不到就归到"其它"。
        String matched = routes.keySet().stream()
                .filter(category::contains)
                .findFirst()
                .orElse("其它");
        System.out.println("【路由判定】问题被分类为：" + matched + "（模型原始输出：" + category + "）");

        // —— 第2次调用：用命中类别对应的专门 system 提示，真正回答用户 ——
        String systemPrompt = routes.get(matched);
        String answer = chatClient.prompt()
                .system(systemPrompt)   // 不同类别用不同人设
                .user(question)
                .call()
                .content();
        System.out.println("【" + matched + "专线回答】" + answer);
        return answer;
    }
}
