package com.example.springai.memory;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * ============================================================================
 * 对话记忆演示：多轮连续对话 + 会话隔离
 * ============================================================================
 *
 * 【做什么】
 *   演示两件事：
 *     1) 同一个会话 ID 内，AI 能记住前面说过的内容（多轮连续对话）。
 *     2) 换一个不同的会话 ID 问同样的问题，AI 不知道——证明不同会话互相隔离。
 *
 * 【怎么做】
 *   - 用 MessageWindowChatMemory 作为"记忆仓库"（滑动窗口，最多保留最近 N 条消息）。
 *   - 用 MessageChatMemoryAdvisor 把记忆挂到 ChatClient 上：
 *       它会在每次请求前，把该会话的历史消息自动塞进去；
 *       并在收到回答后，把这一轮的问答也存回记忆。
 *   - 每次调用通过 advisors(a -> a.param(ChatMemory.CONVERSATION_ID, "xxx")) 指定会话 ID。
 *
 * 【目的】
 *   理解"模型本身无记忆，记忆由程序维护"，并掌握 Spring AI 的标准实现方式。
 *
 * 【流程图（ASCII）】
 *
 *   会话 user-1：
 *     第1轮: "我叫小明，今年18岁"  ──► 存入记忆[user-1]
 *     第2轮: "我叫什么名字？"      ──► 请求时自动带上[user-1]的历史 ──► AI 答: 小明，18岁 ✓
 *
 *   会话 user-2（另一段独立记忆）：
 *           "我叫什么名字？"      ──► [user-2]里没有历史 ──► AI 答: 不知道 ✗（隔离成功）
 * ============================================================================
 */
@Component
public class ChatMemoryDemoRunner implements CommandLineRunner {

    private final ChatClient chatClient;

    public ChatMemoryDemoRunner(ChatClient.Builder builder) {
        // 1) 创建记忆仓库：滑动窗口记忆，最多保留最近 10 条消息（超出会丢弃最早的）。
        //    builder() 不指定仓库时，默认使用内存仓库 InMemoryChatMemoryRepository（存在内存里）。
        ChatMemory chatMemory = MessageWindowChatMemory.builder()
                .maxMessages(10)   // 记忆窗口大小：最多记住最近 10 条消息
                .build();

        // 2) 把记忆挂到 ChatClient：用 MessageChatMemoryAdvisor 包住 chatMemory。
        //    defaultAdvisors 设的是"默认顾问"，之后每次调用都会自动经过它（自动存/取历史）。
        this.chatClient = builder
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("\n========== 模块06：对话记忆演示 ==========\n");

        // ----------------------------------------------------------------
        // 会话 user-1：连续两轮对话，验证"记住了"。
        // ----------------------------------------------------------------
        System.out.println("----- 会话 user-1：第一轮（告诉它信息）-----");
        String r1 = chatClient.prompt()
                .user("我叫小明，今年18岁")
                // 指定本次调用属于哪个会话：用 CONVERSATION_ID 这个固定的参数名。
                // 同一个会话 ID 共享同一段记忆。
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, "user-1"))
                .call()
                .content();
        System.out.println("我：我叫小明，今年18岁");
        System.out.println("AI：" + r1);

        System.out.println("\n----- 会话 user-1：第二轮（考它记不记得）-----");
        String r2 = chatClient.prompt()
                .user("我叫什么名字？今年多大？")
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, "user-1"))  // 同一个会话
                .call()
                .content();
        System.out.println("我：我叫什么名字？今年多大？");
        System.out.println("AI：" + r2 + "    （能答出小明/18岁，说明记住了 ✓）");

        // ----------------------------------------------------------------
        // 会话 user-2：换一个会话 ID 问同样的问题，验证"互相隔离、它不知道"。
        // ----------------------------------------------------------------
        System.out.println("\n----- 会话 user-2：用不同会话 ID 问同样问题 -----");
        String r3 = chatClient.prompt()
                .user("我叫什么名字？今年多大？")
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, "user-2"))  // 另一个独立会话
                .call()
                .content();
        System.out.println("我：我叫什么名字？今年多大？");
        System.out.println("AI：" + r3 + "    （它不知道，说明不同会话互相隔离 ✓）");

        System.out.println("\n========== 模块06 演示结束 ==========\n");
    }
}
