package com.example.springai.observability;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

/**
 * ============================================================================
 * 演示：如何在【不调用真实大模型】的情况下测试 AI 业务逻辑
 * ============================================================================
 *
 * 【核心思路】
 *   - 真实调用大模型既花钱、又慢、还不稳定（每次回答都不一样），不适合写进单元测试。
 *   - 解决办法：把底层的 ChatModel 换成一个“假模型（mock）”，让它返回我们【写死的固定回答】，
 *     然后只验证“我们自己的业务逻辑”（ChatAssistantService 里的加工逻辑）是否正确。
 *
 * 【关键注解】
 *   - @SpringBootTest：启动 Spring 容器加载真实的 Bean（包括 ChatAssistantService）。
 *   - @MockitoBean：Spring Boot 3.4+ / Spring Framework 6.2 推荐的写法
 *     （取代旧的、已废弃的 @MockBean）。它会用一个 Mockito mock 替换容器里的 ChatModel Bean。
 *     于是自动配置出来的 ChatClient.Builder 会基于这个“假模型”构建，调用时不会走网络。
 *   - @ActiveProfiles("test")：激活 test profile，使 ObservabilityRunner（标了 @Profile("!test")）
 *     不在测试期间运行，避免启动时触发真实调用。
 * ============================================================================
 */
@SpringBootTest
@ActiveProfiles("test")
class ChatAssistantServiceTest {

    // 用假的 ChatModel 替换容器里真实的 ChatModel。ChatClient 底层就调它。
    @MockitoBean
    private ChatModel chatModel;

    // 被测对象：真实的业务 Service（它内部用 ChatClient，而 ChatClient 用上面的假模型）。
    @Autowired
    private ChatAssistantService assistantService;

    @Test
    void askQuestion_应给回答加上前缀并去除首尾空白() {
        // ① 准备“假模型”的固定回答：注意首尾故意留空白，用来验证 Service 会 trim。
        ChatResponse fakeResponse = new ChatResponse(List.of(
                new Generation(new AssistantMessage("  你好，我是助手  "))));

        // ② 打桩(stub)：任何 Prompt 进来，都返回上面这个固定回答（不走网络）。
        given(chatModel.call(any(Prompt.class))).willReturn(fakeResponse);

        // ③ 执行被测方法
        String result = assistantService.askQuestion("随便问一句");

        // ④ 断言业务逻辑：加了“AI 助手：”前缀，且去掉了首尾空白。
        assertThat(result).isEqualTo("AI 助手：你好，我是助手");
    }

    @Test
    void askQuestion_当模型返回空时应走兜底回答() {
        // 模型返回空字符串 → 期望 Service 返回兜底文案
        ChatResponse emptyResponse = new ChatResponse(List.of(
                new Generation(new AssistantMessage(""))));
        given(chatModel.call(any(Prompt.class))).willReturn(emptyResponse);

        String result = assistantService.askQuestion("随便问一句");

        assertThat(result).isEqualTo("AI 助手：抱歉，我暂时无法回答这个问题。");
    }
}
