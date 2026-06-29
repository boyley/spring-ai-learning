package com.example.springai.observability;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ============================================================================
 * 模块 16：可观测性与测试（Observability & Testing）—— 启动类
 * ============================================================================
 *
 * 【这个模块是做什么的】
 *   两件事：
 *     1) 可观测性（Observability）：观测每一次 AI 调用的指标、耗时、Token 消耗等。
 *        Spring AI 会自动为 ChatClient/ChatModel 调用产生 Micrometer observation（观测点），
 *        配合 Actuator 暴露的 /actuator/metrics，就能查看 gen_ai 相关指标。
 *     2) 测试：演示如何在【不调用真实大模型】的情况下，测试我们自己的业务逻辑——
 *        用 @MockitoBean 把底层 ChatModel 换成“假模型”，让它返回固定回答，再断言 Service 行为。
 *
 * 【怎么做】
 *   - ChatAssistantService 用 ChatClient 发起一次调用，并做一点业务加工（见该类）。
 *   - application.yml 暴露 actuator 端点；运行时可访问 /actuator/metrics 查看指标。
 *   - 测试类（src/test）用 @MockitoBean 模拟 ChatModel，验证业务逻辑而不走网络。
 *
 * 【达到的目的】
 *   理解“AI 调用如何被观测”，以及“AI 应用如何被低成本、可重复地测试”。
 * ============================================================================
 */
@SpringBootApplication
public class ObservabilityApplication {
    public static void main(String[] args) {
        SpringApplication.run(ObservabilityApplication.class, args);
    }
}
