package com.example.springai.prompteng;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * ============================================================================
 * 提示工程 5 大核心模式演示
 * ============================================================================
 *
 * 【整体流程图】
 *
 *   同一个 ChatClient
 *        │
 *        ├─► 模式1 Zero-shot   ：user(问题)                         ──► 模型直接答
 *        ├─► 模式2 Few-shot    ：user(几个示例 + 新问题)            ──► 模型仿照示例答
 *        ├─► 模式3 Role/Persona：system(专家人设) + user(问题)      ──► 模型以专家口吻答
 *        ├─► 模式4 CoT         ：user(问题 + "请一步步推理")        ──► 模型先推理后给答案
 *        └─► 模式5 参数控制    ：options(temperature/topP) + user   ──► 控制确定性/创造性
 *
 * 【核心思想】
 *   提示词不是随便写的。通过"给不给例子、设不设角色、要不要推理、调什么参数"
 *   这几个旋钮，我们可以系统化地把模型的表现往期望方向调。
 * ============================================================================
 */
@Component
public class PromptPatternsRunner implements CommandLineRunner {

    /** 与大模型对话的高级客户端（由 spring-ai-starter-model-openai 自动配置出 Builder）。 */
    private final ChatClient chatClient;

    public PromptPatternsRunner(ChatClient.Builder builder) {
        // 这里不设默认人设，保持"干净"的客户端，方便每个演示自己控制提示。
        this.chatClient = builder.build();
    }

    @Override
    public void run(String... args) {
        System.out.println("\n========== 模块17：提示工程模式演示 ==========");
        demo1_zeroShot();
        demo2_fewShot();
        demo3_rolePersona();
        demo4_chainOfThought();
        demo5_parameterControl();
        System.out.println("\n========== 模块17 演示全部结束 ==========\n");
    }

    /**
     * 模式1：零样本 Zero-shot。
     * 【是什么】直接把问题丢给模型，不提供任何示例。
     * 【何时用】任务对模型来说很常见、很直白（如常识问答、简单翻译），最省事。
     */
    private void demo1_zeroShot() {
        System.out.println("\n----- 模式1：零样本 Zero-shot（直接问，不给例子）-----");
        String answer = chatClient.prompt()
                // 不给任何示例，直接提出分类任务，全凭模型自身能力。
                .user("判断这句话的情感（正面/负面/中性）：这家餐厅的菜又贵又难吃。")
                .call()
                .content();
        System.out.println("AI：" + answer);
    }

    /**
     * 模式2：少样本 Few-shot。
     * 【是什么】在提示里先给几个"输入→输出"的示范，再给新输入，让模型照着格式与口径回答。
     * 【何时用】希望输出遵循特定格式/标签体系，或任务较"小众"、零样本不稳定时。
     * 这里用"情感分类"任务：先给 3 个示例，再让它判断新句子。
     */
    private void demo2_fewShot() {
        System.out.println("\n----- 模式2：少样本 Few-shot（给示例照葫芦画瓢）-----");
        // 把"示例 + 新问题"一起放进 user 提示。模型会模仿示例的标签体系与输出格式。
        String prompt = """
                请像下面的示例一样，只输出情感标签（正面 / 负面 / 中性），不要解释：
                句子：今天阳光明媚，心情真好。 => 正面
                句子：快递又丢件了，气死我了。 => 负面
                句子：会议定在下午三点。       => 中性
                句子：这部电影剧情拖沓，看睡着了。 =>""";
        String answer = chatClient.prompt()
                .user(prompt)
                .call()
                .content();
        System.out.println("AI：" + answer);
    }

    /**
     * 模式3：角色/人设 Role/Persona。
     * 【是什么】用 system 指令给模型设定一个专家身份/语气/受众，让回答更专业、更对味。
     * 【何时用】需要特定领域专业度、固定语气或面向特定读者（如"给小学生讲"）时。
     */
    private void demo3_rolePersona() {
        System.out.println("\n----- 模式3：角色/人设 Role/Persona（设定专家身份）-----");
        String answer = chatClient.prompt()
                // system 设定"资深网络安全专家"人设，模型会用更专业的视角与术语回答。
                .system("你是一位有 20 年经验的资深网络安全专家，回答严谨、给出可落地的建议。")
                .user("普通人如何保护自己的账号安全？")
                .call()
                .content();
        System.out.println("AI（专家人设）：" + answer);
    }

    /**
     * 模式4：思维链 Chain-of-Thought（CoT）。
     * 【是什么】要求模型"一步步推理"再给最终答案，而不是直接蹦出结论。
     * 【何时用】数学题、逻辑题、多步推理等复杂任务。让模型显式思考往往能显著提升正确率。
     * 这里对同一道题做"不加 CoT" vs "加 CoT"的对比。
     */
    private void demo4_chainOfThought() {
        System.out.println("\n----- 模式4：思维链 Chain-of-Thought（一步步推理）-----");
        String question = "果园里有 3 筐苹果，每筐 24 个；卖掉了 17 个后，又新摘了 2 筐（每筐 24 个）。现在一共有多少个苹果？";

        System.out.println("【对比A】不加思维链（直接要答案）：");
        String direct = chatClient.prompt()
                .user(question + " 请直接给出最终数字。")
                .call()
                .content();
        System.out.println("AI：" + direct);

        System.out.println("\n【对比B】加思维链（要求一步步推理）：");
        String cot = chatClient.prompt()
                // 关键就是这句"请一步步推理"，引导模型显式写出中间步骤，减少计算失误。
                .user(question + " 请一步步思考、写出计算过程，最后再给出最终答案。")
                .call()
                .content();
        System.out.println("AI：" + cot);
    }

    /**
     * 模式5：参数控制（temperature / topP）。
     * 【是什么】通过采样参数控制输出的"确定性 vs 创造性"。
     *   - temperature 采样温度（0~2）：越低越稳定保守，越高越发散有创意。
     *   - topP 核采样（0~1）：只从累计概率前 P 的候选词里采样，越小越保守。
     * 【何时用】需要稳定可复现答案时用低温；需要头脑风暴/创意文案时用高温。
     * 这里用 OpenAiChatOptions（厂商专属参数对象）对同一个创意任务做低温/高温对比。
     */
    private void demo5_parameterControl() {
        System.out.println("\n----- 模式5：参数控制 temperature / topP（确定性 vs 创造性）-----");
        String task = "为一家新开的猫咪咖啡馆起一个有创意的店名。";

        // 低温 + 低 topP：输出更保守、确定，多次运行结果更接近。
        OpenAiChatOptions conservative = OpenAiChatOptions.builder()
                .temperature(0.0)   // 温度 0：尽量确定，几乎不随机
                .topP(0.1)          // 只在最可能的极少数候选里挑词
                .build();
        String stable = chatClient.prompt()
                .user(task)
                .options(conservative)   // 应用本次的运行时参数
                .call()
                .content();
        System.out.println("AI（低温 0.0，更稳定）：" + stable);

        // 高温 + 高 topP：输出更发散、更有创意，但稳定性下降。
        OpenAiChatOptions creative = OpenAiChatOptions.builder()
                .temperature(1.5)   // 高温：更有创造性/更跳脱
                .topP(1.0)          // 允许从更广的候选词里采样
                .build();
        String wild = chatClient.prompt()
                .user(task)
                .options(creative)
                .call()
                .content();
        System.out.println("AI（高温 1.5，更发散）：" + wild);
    }
}
