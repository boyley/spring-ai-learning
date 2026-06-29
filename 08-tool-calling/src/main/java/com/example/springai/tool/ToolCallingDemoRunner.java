package com.example.springai.tool;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * ============================================================================
 * 工具调用演示：让模型自动调用 DateTimeTools 里的方法
 * ============================================================================
 *
 * 【工具调用的完整回合（流程图）】
 *
 *   你的代码                ChatClient            大模型(DeepSeek)        你的工具方法
 *     │  user("现在几点了？      │                      │                      │
 *     │   北京天气怎么样？")     │                      │                      │
 *     │  .tools(工具对象)        │                      │                      │
 *     │ ──────────────────────► │  携带"问题+工具清单"   │                      │
 *     │                         │ ───────────────────► │                      │
 *     │                         │                      │ 判断：我需要调用工具！  │
 *     │                         │  返回"要调用的工具+参数"│                      │
 *     │                         │ ◄─────────────────── │                      │
 *     │                         │  Spring AI 自动执行你的方法                   │
 *     │                         │ ──────────────────────────────────────────► │
 *     │                         │  方法返回结果(时间/天气)                       │
 *     │                         │ ◄────────────────────────────────────────── │
 *     │                         │  把工具结果回传给模型  │                      │
 *     │                         │ ───────────────────► │                      │
 *     │                         │                      │ 基于结果生成最终回答    │
 *     │                         │  最终自然语言回答      │                      │
 *     │                         │ ◄─────────────────── │                      │
 *     │  .call().content()      │                      │                      │
 *     │ ◄────────────────────── │                      │                      │
 *     ▼
 *   打印回答
 *
 *   ★ 注意：上面"模型↔工具"可能来回多轮（本例要查时间又查天气，模型会调用两个工具），
 *     整个过程由 Spring AI 自动编排，你只需写好工具方法并 .tools(...) 注册即可。
 * ============================================================================
 */
@Component
public class ToolCallingDemoRunner implements CommandLineRunner {

    private final ChatClient chatClient;

    /** 直接用自动配置的 Builder 构建一个普通 ChatClient 即可，工具在调用时再传入。 */
    public ToolCallingDemoRunner(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @Override
    public void run(String... args) {
        System.out.println("\n========== 模块08：工具调用(Tool Calling) 演示 ==========\n");

        String question = "现在几点了？北京天气怎么样？";
        System.out.println("【我问】" + question + "\n");

        // ★ 核心：.tools(new DateTimeTools()) 把工具对象交给本次调用。
        //   Spring AI 会扫描其中带 @Tool 的方法，连同问题一起告诉模型；
        //   模型判断需要时会"请求调用"，Spring AI 自动执行方法并把结果回传，最终拿到回答。
        String answer = chatClient.prompt()
                .user(question)
                .tools(new DateTimeTools())   // 注册工具：模型会自动按需调用其中的方法
                .call()
                .content();

        System.out.println("\n【AI 答】" + answer);
        System.out.println("\n========== 模块08 演示结束 ==========\n");
        System.out.println("提示：上方带 🔧 的行说明对应工具方法确实被模型自动调用了。");
    }
}
