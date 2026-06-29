package com.example.springai.observability;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * ============================================================================
 * 可观测性演示：调用 Service + 提示如何查看指标
 * ============================================================================
 *
 * 【observation（观测点）如何贯穿调用链】
 *
 *   ChatAssistantService.askQuestion()
 *            │
 *            ▼
 *   ChatClient.prompt()....call()
 *            │  ← Spring AI 在这里自动开启一个 observation（记录开始时间、模型名等）
 *            ▼
 *   ChatModel 真正发 HTTP 请求给大模型
 *            │
 *            ▼  ← 响应回来后 observation 结束：记录耗时、Token 用量、是否异常
 *   指标汇总到 Micrometer
 *            │
 *            ▼
 *   通过 Actuator 暴露：GET /actuator/metrics  → 可查看 gen_ai.* 相关指标
 *
 * 【怎么查看指标（运行后）】
 *   1. 指标列表：           GET http://localhost:8080/actuator/metrics
 *   2. 具体某个指标详情：   GET http://localhost:8080/actuator/metrics/gen_ai.client.operation
 *      （指标名以实际 Spring AI 版本为准，通常以 gen_ai 开头）
 *   3. 健康检查：           GET http://localhost:8080/actuator/health
 * ============================================================================
 */
@Component
// @Profile("!test")：在名为 test 的 profile 下不创建本 Runner，
// 这样跑测试时不会触发真实 AI 调用（测试类用 @ActiveProfiles("test") 激活该 profile）。
@Profile("!test")
public class ObservabilityRunner implements CommandLineRunner {

    private final ChatAssistantService assistantService;

    public ObservabilityRunner(ChatAssistantService assistantService) {
        this.assistantService = assistantService;
    }

    @Override
    public void run(String... args) {
        System.out.println("\n========== 模块16：可观测性演示 ==========\n");

        // 发起一次会被自动观测的 AI 调用
        String answer = assistantService.askQuestion("用一句话介绍一下 Micrometer 是什么？");
        System.out.println(answer);

        System.out.println("\n----- 如何查看指标 -----");
        System.out.println("应用启动后访问： http://localhost:8080/actuator/metrics");
        System.out.println("查看 AI 调用指标（gen_ai 开头）、耗时、Token 用量等。");
        System.out.println("\n========== 模块16 演示结束 ==========\n");
    }
}
