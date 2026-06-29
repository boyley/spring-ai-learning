package com.example.springai.mcpclient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ============================================================================
 * 模块 19：MCP 客户端（MCP Client）—— 启动类
 * ============================================================================
 *
 * 【这个模块是做什么的】
 *   写一个 MCP「客户端（消费方）」，连接到模块 14 构建的 MCP「服务器（提供方）」，
 *   把服务器上远程暴露的工具（getWeather 查天气、getCurrentTime 取时间）「拉到本地」，
 *   再交给本地的大模型（ChatClient）使用——于是大模型在回答问题时，能自动跨进程
 *   调用远程 MCP 工具。本模块与模块 14（Server 侧）配成一个完整闭环。
 *
 * 【先理解几个概念（零基础必读）】
 *   1. MCP（模型上下文协议）：Anthropic 提出的开放标准，给「AI 应用」与「外部工具」
 *      之间定义统一的“插头规格”，让工具的提供方与使用方解耦、即插即用。
 *   2. MCP Server（服务器/提供方）：把一批工具暴露出去。← 模块 14 就是它。
 *   3. MCP Client（客户端/消费方）：连接 Server，列出可用工具，并在大模型需要时调用。← 本模块。
 *   4. ToolCallback / ToolCallbackProvider：Spring AI 对“工具”的统一抽象。MCP 客户端
 *      会把远程工具自动包装成本地的 ToolCallbackProvider，于是本地 ChatClient 用起来
 *      和调用进程内的本地工具毫无区别。
 *
 * 【怎么做】
 *   - 在 application.yml 配好要连接的远端 MCP Server（SSE，地址 http://localhost:8090）。
 *   - spring-ai-starter-mcp-client 自动配置：建立连接 + 把远程工具包装成 ToolCallbackProvider Bean。
 *   - 注入这个 ToolCallbackProvider，构建 ChatClient 时用 .defaultToolCallbacks(provider)
 *     把远程工具交给模型（见 McpClientDemoRunner）。
 *   - 向模型提一个需要查天气和时间的问题，模型会自动决定调用远程 MCP 工具并据此作答。
 *
 * 【达到的目的】
 *   体会 MCP 的核心价值：工具运行在“别处”（14 那个独立进程），本应用无需重新实现，
 *   只要连上去就能让自己的大模型使用这些远程能力。
 *
 * 【★ 运行前置条件 ★】
 *   必须先启动模块 14 的 MCP Server：  cd 14-mcp && mvn spring-boot:run
 *   待其在 8090 端口就绪后，再运行本模块。否则本模块启动时会因连不上 Server 而报错。
 *   （本模块只要求编译通过，实际联调需要先起 14；详见 README。）
 * ============================================================================
 */
@SpringBootApplication
public class McpClientApplication {

    public static void main(String[] args) {
        // 启动 Spring Boot 应用：
        //   1. 创建 Spring 容器；
        //   2. MCP Client Starter 自动配置：根据 yml 连接到远端 MCP Server，
        //      并把远程工具包装成 ToolCallbackProvider Bean；
        //   3. 容器就绪后执行 CommandLineRunner（演示在 McpClientDemoRunner 里）。
        SpringApplication.run(McpClientApplication.class, args);
    }
}
