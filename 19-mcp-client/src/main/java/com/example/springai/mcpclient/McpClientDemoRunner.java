package com.example.springai.mcpclient;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * ============================================================================
 * MCP 客户端演示：把远程 MCP 工具交给大模型，让模型自动调用
 * ============================================================================
 *
 * 【这个类是做什么的】
 *   1. 注入 MCP Client Starter 自动装配出来的 ToolCallbackProvider——它里面装着
 *      从远端 MCP Server（模块 14）「拉过来」的工具（getWeather、getCurrentTime）。
 *   2. 构建 ChatClient 时通过 .defaultToolCallbacks(provider) 把这些远程工具注册给模型。
 *   3. 向模型提一个既要查天气又要问时间的问题，模型会自行决定调用哪些远程工具。
 *
 * 【完整调用链（Client ↔ Server 闭环）】
 *
 *   你的代码 / ChatClient                       本进程：MCP Client            远端(8090)：14 的 MCP Server
 *      │  user("北京天气?现在几点?")                    │                                  │
 *      │ ───────────────────────────► 大模型(DeepSeek)  │                                  │
 *      │           模型判断「需要调用工具」              │                                  │
 *      │ ──── tools/call getWeather("北京") ──────────► │ ── (SSE/HTTP) tools/call ──────► │
 *      │                                                │                                  │ 反射执行 WeatherTools.getWeather
 *      │ ◄──────────── "北京：晴 26℃" ───────────────── │ ◄──────── 工具结果原路返回 ───────│
 *      │ ──── tools/call getCurrentTime() ───────────► │ ── (SSE/HTTP) tools/call ──────► │ 执行 getCurrentTime
 *      │ ◄──────────── "2026-06-30 ..." ─────────────── │ ◄──────── 工具结果原路返回 ───────│
 *      │   模型拿到工具结果，生成最终自然语言回答          │                                  │
 *      ▼                                                                                   ▼
 *   打印到控制台
 *
 * 【关键 API】
 *   - ToolCallbackProvider（注入）：由 MCP Client 自动配置，getToolCallbacks() 返回远程工具数组。
 *   - ChatClient.Builder.defaultToolCallbacks(ToolCallbackProvider...)：把远程工具设为默认工具，
 *     之后每次对话模型都能按需调用它们。
 *
 * 【★ 运行前置：必须先启动模块 14 的 MCP Server（cd 14-mcp && mvn spring-boot:run）★】
 * ============================================================================
 */
@Component
public class McpClientDemoRunner implements CommandLineRunner {

    /** 与大模型对话的高级客户端；构建时已把远程 MCP 工具注册为默认工具。 */
    private final ChatClient chatClient;

    /** 保存一份 Provider 引用，演示阶段先把“拉到了哪些远程工具”列出来给初学者看。 */
    private final ToolCallbackProvider mcpToolProvider;

    /**
     * 构造器注入。
     *
     * @param builder        Spring 自动配置的 ChatClient.Builder（底层连 DeepSeek 对话模型）。
     * @param mcpToolProvider ★ 由 MCP Client Starter 自动装配 ★。它把远端 MCP Server 暴露的工具
     *                        包装成统一的 ToolCallbackProvider，注入即用，无需我们手写连接代码。
     */
    public McpClientDemoRunner(ChatClient.Builder builder, ToolCallbackProvider mcpToolProvider) {
        this.mcpToolProvider = mcpToolProvider;
        this.chatClient = builder
                // ★ 把“远程 MCP 工具”设为该 ChatClient 的默认工具 ★
                // 之后每次对话，模型都能在需要时自动调用这些跨进程的远程工具。
                .defaultToolCallbacks(mcpToolProvider)
                .build();
    }

    @Override
    public void run(String... args) {
        System.out.println("\n========== 模块19：MCP 客户端调用远程工具 ==========\n");

        // 先把“从远端服务器拉到了哪些工具”打印出来，直观感受 MCP 的“工具发现”能力。
        ToolCallback[] callbacks = mcpToolProvider.getToolCallbacks();
        System.out.println("【已从远端 MCP Server 发现并拉取到 " + callbacks.length + " 个工具】");
        for (ToolCallback cb : callbacks) {
            // getToolDefinition() 里含工具的名字与描述——正是当初在 14 里用 @Tool 写的那些。
            System.out.println("  - " + cb.getToolDefinition().name()
                    + " : " + cb.getToolDefinition().description());
        }

        // ★★★ 核心：向模型提一个需要查天气 + 问时间的问题 ★★★
        // 模型会自行判断需要调用远程的 getWeather("北京") 和 getCurrentTime()，
        // 拿到工具结果后再组织成一段自然语言回答。整个工具调用过程对我们是透明的。
        String question = "北京今天天气怎么样？现在几点？";
        System.out.println("\n【我问】" + question);

        String answer = chatClient.prompt()
                .user(question)
                .call()        // 同步调用（其间可能触发一到多次远程 MCP 工具调用）
                .content();    // 取出模型最终的自然语言回答

        System.out.println("\n【AI 答（已综合远程工具结果）】" + answer);
        System.out.println("\n========== 演示结束：本地模型成功调用了远程 MCP 工具！ ==========\n");
    }
}
