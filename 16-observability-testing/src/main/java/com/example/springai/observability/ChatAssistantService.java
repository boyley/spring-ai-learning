package com.example.springai.observability;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

/**
 * ============================================================================
 * 业务服务：封装一次 AI 调用 + 一点业务加工
 * ============================================================================
 *
 * 【为什么单独抽一个 Service】
 *   - 真实项目里，调用大模型往往只是业务的一环，外面还包着我们自己的逻辑
 *     （参数校验、空结果兜底、结果二次加工等）。把它抽成 Service 便于：
 *       1) 复用；2) 单元测试（测试时可把底层模型换成“假模型”，只验证我们的逻辑）。
 *
 * 【可观测性从哪来】
 *   - 我们用注入的 ChatClient 发起调用时，Spring AI 会自动在调用前后创建 Micrometer observation，
 *     记录耗时、模型名、Token 用量等。无需我们写任何埋点代码。
 * ============================================================================
 */
@Service
public class ChatAssistantService {

    private final ChatClient chatClient;

    /** 构造器注入：用自动配置的 ChatClient.Builder 构建 ChatClient。 */
    public ChatAssistantService(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    /**
     * 向 AI 提问，并对回答做一点业务加工：
     *   - 去掉首尾空白；
     *   - 空结果兜底；
     *   - 统一加上“AI 助手：”前缀（代表我们自己的业务逻辑）。
     *
     * 测试时我们正是要验证“这段业务加工”是否正确，而不需要真的访问大模型。
     *
     * @param question 用户问题
     * @return 加工后的回答
     */
    public String askQuestion(String question) {
        // ① 调用大模型拿到原始回答（这一行会被 Spring AI 自动观测）
        String raw = chatClient.prompt()
                .user(question)
                .call()
                .content();

        // ② 业务加工：空结果兜底
        if (raw == null || raw.isBlank()) {
            return "AI 助手：抱歉，我暂时无法回答这个问题。";
        }
        // ③ 业务加工：去空白 + 统一前缀
        return "AI 助手：" + raw.trim();
    }
}
