package com.example.springai.agents;

import org.springframework.ai.chat.client.ChatClient;

/**
 * ============================================================================
 * 模式1：提示链 Chain Workflow（顺序链）
 * ============================================================================
 *
 * 【是什么】把一个大任务拆成几个有先后顺序的小步骤，每一步是一次 LLM 调用，
 *          前一步的输出直接作为后一步的输入。像流水线一样层层加工。
 *
 * 【何时用】任务可以清晰拆成"先做A、再做B、最后做C"的固定步骤时（如：先列提纲→再扩写→再润色）。
 *
 * 【数据流动 ASCII 图】
 *
 *   主题 ──► [步骤1 列要点] ──► 要点列表 ──► [步骤2 扩写成段落] ──► 段落 ──► [步骤3 润色] ──► 成品
 *              (LLM调用)                      (LLM调用)                      (LLM调用)
 *
 *   注意：每个箭头上的"中间产物"都是上一步 .content() 的返回值，被拼进下一步的 user 提示。
 * ============================================================================
 */
public class ChainWorkflow {

    private final ChatClient chatClient;

    public ChainWorkflow(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    /**
     * 用三步链式加工，把一个"主题"逐步变成一段成品文案。
     *
     * @param topic 写作主题
     * @return 最终润色好的文案
     */
    public String run(String topic) {
        // —— 步骤1：让模型先针对主题列出几个要点（结构化的中间产物）——
        String points = chatClient.prompt()
                .user("围绕主题《" + topic + "》，用 3 个简短的要点列出核心内容，每行一个要点，不要展开。")
                .call()
                .content();
        System.out.println("【步骤1·要点】\n" + points);

        // —— 步骤2：把上一步的"要点"作为输入，扩写成一段连贯文字 ——
        String paragraph = chatClient.prompt()
                .user("请把下面这些要点扩写成一段连贯、通顺的中文段落：\n" + points)
                .call()
                .content();
        System.out.println("\n【步骤2·扩写】\n" + paragraph);

        // —— 步骤3：把上一步的"段落"作为输入，做最后的润色 ——
        String polished = chatClient.prompt()
                .user("请润色下面这段文字，使其更生动、更有感染力，保持原意，直接输出润色后的结果：\n" + paragraph)
                .call()
                .content();
        System.out.println("\n【步骤3·润色（成品）】\n" + polished);

        return polished;
    }
}
