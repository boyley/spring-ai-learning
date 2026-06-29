package com.example.springai.overview;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * ============================================================================
 * 第一个 Spring AI 演示：调用大模型并打印回答
 * ============================================================================
 *
 * 【核心流程图】
 *
 *   你的代码                ChatClient              大模型(DeepSeek)
 *     │                        │                         │
 *     │  prompt().user("问题")  │                         │
 *     │ ─────────────────────► │                         │
 *     │                        │   HTTP 请求(带你的问题)   │
 *     │                        │ ──────────────────────► │
 *     │                        │                         │ 思考中...
 *     │                        │   HTTP 响应(AI 的回答)    │
 *     │                        │ ◄────────────────────── │
 *     │   .call().content()    │                         │
 *     │  返回 String 回答       │                         │
 *     │ ◄───────────────────── │                         │
 *     ▼                        ▼                         ▼
 *   打印到控制台
 *
 * 【关键 API 链路解读】
 *   chatClient.prompt()            -> 开始构建一次对话请求
 *             .user("...")         -> 设置"用户说的话"（即你的问题）
 *             .call()              -> 同步发起调用（阻塞等待，直到模型返回完整结果）
 *             .content()           -> 取出回答的纯文本内容（String）
 * ============================================================================
 */
@Component // 标记为 Spring 组件，容器启动时会自动创建它的实例并执行 run()
public class QuickStartRunner implements CommandLineRunner {

    /**
     * ChatClient 是我们与大模型对话的"高级客户端"。
     * 我们不直接 new 它，而是用 Spring 自动配置好的 ChatClient.Builder 来构建。
     */
    private final ChatClient chatClient;

    /**
     * 构造器注入（Spring 推荐的依赖注入方式）。
     *
     * 参数 ChatClient.Builder 是谁提供的？
     *   —— 是 spring-ai-starter-model-openai 自动配置出来的 Bean。
     *   它内部已经根据共享配置文件里的 base-url / api-key / model 配好了 DeepSeek 连接。
     *
     * 我们在这里用 builder.build() 得到一个可复用的 ChatClient 实例。
     */
    public QuickStartRunner(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    /**
     * CommandLineRunner 的 run() 方法会在 Spring 容器启动完成后自动执行。
     * 我们把演示代码写在这里。
     */
    @Override
    public void run(String... args) {
        System.out.println("\n========== 模块01：第一个 Spring AI 调用 ==========\n");

        // 我们要问大模型的问题
        String question = "请用一句话向 Java 初学者解释什么是 Spring AI？";
        System.out.println("【我问】" + question);

        // ★★★ 核心：一行链式调用完成"提问 -> 调用 -> 取回答" ★★★
        String answer = chatClient   // 对话客户端
                .prompt()            // 开始一次提问
                .user(question)      // 设置用户问题
                .call()              // 同步调用模型（等待完整返回）
                .content();          // 取出回答文本

        System.out.println("\n【AI 答】" + answer);
        System.out.println("\n========== 演示结束：恭喜，你已成功调用大模型！ ==========\n");
    }
}
