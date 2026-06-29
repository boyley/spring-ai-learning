package com.example.springai.chatclient;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * ============================================================================
 * ChatClient 四大核心用法演示
 * ============================================================================
 *
 * 【流式 vs 非流式 流程图】
 *
 *   非流式 call():
 *     提问 ──► 模型 ──(等全部生成完)──► 一次性返回整段文字
 *
 *   流式 stream():
 *     提问 ──► 模型 ──► "你" ─► "好" ─► "，" ─► "我" ─► ...  (一块块实时返回)
 *              （像 ChatGPT 网页打字机效果，体验更好，适合前端实时展示）
 * ============================================================================
 */
@Component
public class ChatClientDemoRunner implements CommandLineRunner {

    private final ChatClient chatClient;

    /**
     * 这里演示在 build 之前可以用 defaultSystem(...) 给客户端设置一个"默认系统人设"。
     * 之后这个 ChatClient 发出的每次请求都会自动带上这个系统指令。
     */
    public ChatClientDemoRunner(ChatClient.Builder builder) {
        this.chatClient = builder
                // 默认系统指令：给 AI 设定身份。后续每次调用都生效。
                .defaultSystem("你是一位耐心的 Java 编程老师，回答要简洁、面向初学者。")
                .build();
    }

    @Override
    public void run(String... args) throws Exception {
        demo1_simpleCall();
        demo2_systemRole();
        demo3_streaming();
        demo4_runtimeOptions();
    }

    /** 演示1：最基础的非流式调用，一次性拿到完整回答。 */
    private void demo1_simpleCall() {
        System.out.println("\n===== 演示1：非流式调用 call() =====");
        String answer = chatClient.prompt()
                .user("用一句话解释什么是 JVM？")
                .call()        // 同步阻塞，等待完整结果
                .content();    // 取纯文本
        System.out.println("AI：" + answer);
    }

    /**
     * 演示2：在单次请求里临时指定 system 指令（覆盖/补充默认人设）。
     * system(系统指令) 用来设定 AI 的角色、风格、约束；user(用户消息) 是具体问题。
     */
    private void demo2_systemRole() {
        System.out.println("\n===== 演示2：System 角色（设定回答风格）=====");
        String answer = chatClient.prompt()
                .system("请用'喵~'结尾，扮演一只猫娘老师。")  // 本次临时系统指令
                .user("什么是变量？")
                .call()
                .content();
        System.out.println("AI：" + answer);
    }

    /**
     * 演示3：流式调用 stream()。
     * 返回的是 Flux<String>（响应式流），每个元素是模型吐出的一小段文字。
     * 我们用 toStream() 把它转成 Java 的 Stream，边到达边打印，模拟打字机效果。
     */
    private void demo3_streaming() {
        System.out.println("\n===== 演示3：流式调用 stream()（打字机效果）=====");
        System.out.print("AI：");
        Flux<String> stream = chatClient.prompt()
                .user("请分三点说明学习编程的好处。")
                .stream()       // 流式调用
                .content();     // Flux<String>：一块块的文本

        // toStream() 把响应式 Flux 转为可遍历的 Java Stream（会阻塞直到流结束）。
        // 实际 Web 项目中通常直接把 Flux 返回给前端（SSE），无需阻塞。
        stream.toStream().forEach(System.out::print);
        System.out.println(); // 换行
    }

    /**
     * 演示4：运行时参数覆盖。
     * 不修改配置文件，临时指定本次调用使用的模型 / temperature 等。
     * ChatOptions 是"与厂商无关"的通用参数对象。
     */
    private void demo4_runtimeOptions() {
        System.out.println("\n===== 演示4：运行时参数覆盖（临时换模型/调温度）=====");
        ChatOptions options = ChatOptions.builder()
                .model("deepseek-chat")   // 本次使用的模型（可临时换成 deepseek-reasoner 等）
                .temperature(1.3)         // 调高温度 => 回答更有创造性/更发散
                .build();

        String answer = chatClient.prompt()
                .user("给我的宠物程序起一个有创意的名字。")
                .options(options)         // 应用本次运行时参数
                .call()
                .content();
        System.out.println("AI（高温度更发散）：" + answer);
        System.out.println("\n========== 模块02 演示全部结束 ==========\n");
    }
}
