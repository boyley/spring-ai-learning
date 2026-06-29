package com.example.springai.advisors;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * ============================================================================
 * Advisor 演示：内置日志 Advisor + 自定义计时 Advisor
 * ============================================================================
 *
 * 【流程图：一次 call() 在责任链中的流动】
 *
 *   你的代码
 *      │  prompt().user("...").call()
 *      ▼
 *   ┌──────────────────────────────────────────────────────────┐
 *   │ ElapsedTimeAdvisor(order=0)  ── 记开始时间                 │  ← 最外层(order 最小)
 *   │   ┌──────────────────────────────────────────────────┐    │
 *   │   │ SimpleLoggerAdvisor(order=0) ── 打印请求(DEBUG)    │    │
 *   │   │        │                                          │    │
 *   │   │        ▼   真正 HTTP 调用 DeepSeek 大模型           │    │
 *   │   │        ▲                                          │    │
 *   │   │ SimpleLoggerAdvisor          ── 打印响应(DEBUG)    │    │
 *   │   └──────────────────────────────────────────────────┘    │
 *   │ ElapsedTimeAdvisor            ── 算耗时并打印              │
 *   └──────────────────────────────────────────────────────────┘
 *      │  返回 String 回答
 *      ▼
 *   打印到控制台
 *
 * 【为什么看不到 SimpleLoggerAdvisor 的输出？】
 *   它用 DEBUG 级别打印。本模块 application.yml 已把
 *   logging.level.org.springframework.ai.chat.client.advisor 调成 DEBUG，
 *   所以能在控制台看到它打印的请求/响应明细。
 * ============================================================================
 */
@Component
public class AdvisorsDemoRunner implements CommandLineRunner {

    private final ChatClient chatClient;

    /**
     * 在构建 ChatClient 时，用 defaultAdvisors(...) 注册两个 Advisor：
     *   1) SimpleLoggerAdvisor —— Spring AI 内置，自动打印请求/响应（DEBUG 级别）。
     *   2) ElapsedTimeAdvisor  —— 我们自定义的计时 Advisor，order=0 让它尽量在最外层。
     *
     * defaultAdvisors(Advisor...) 的参数类型就是 Advisor 接口，
     * SimpleLoggerAdvisor 与我们的 ElapsedTimeAdvisor 都是它的实现，可直接传入。
     */
    public AdvisorsDemoRunner(ChatClient.Builder builder) {
        this.chatClient = builder
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),       // 内置日志 Advisor
                        new ElapsedTimeAdvisor(0)        // 自定义计时 Advisor（order=0，最外层）
                )
                .build();
    }

    @Override
    public void run(String... args) {
        System.out.println("\n========== 模块07：Advisor（顾问/拦截器）演示 ==========\n");

        // 发起一次普通的非流式调用。两个 Advisor 会自动在请求前后插入逻辑。
        // 观察控制台输出顺序：
        //   ⏱️ 开始 → (DEBUG)请求明细 → (DEBUG)响应明细 → ⏱️ 耗时 → AI 回答
        String answer = chatClient.prompt()
                .user("用一句话介绍一下你自己。")
                .call()
                .content();

        System.out.println("\n【AI 答】" + answer);
        System.out.println("\n========== 模块07 演示结束 ==========\n");
        System.out.println("提示：若想看到 SimpleLoggerAdvisor 打印的请求/响应明细，");
        System.out.println("     请确认日志级别 org.springframework.ai.chat.client.advisor=DEBUG（已在 application.yml 配好）。");
    }
}
