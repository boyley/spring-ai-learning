package com.example.springai.mcp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ============================================================================
 * 模块 14：模型上下文协议 MCP（Model Context Protocol）—— 启动类
 * ============================================================================
 *
 * 【这个模块是做什么的】
 *   构建一个最小可用的 MCP Server（服务器），对外暴露若干「工具（Tool）」，
 *   让任何 MCP 客户端（比如 Claude Desktop、另一个 Spring AI 应用）都能以
 *   统一、标准的方式发现并调用这些工具。
 *
 * 【先理解几个概念（零基础必读）】
 *   1. MCP（模型上下文协议）：一套由 Anthropic 提出的开放标准协议。它解决的问题是——
 *      不同 AI 应用接入「外部工具/数据源」时，过去各写各的、互不兼容；
 *      MCP 定义了统一的“插头规格”，于是「工具的提供方」和「工具的使用方」只要都说 MCP，就能即插即用。
 *   2. MCP Server（服务器/提供方）：把一批工具、资源、提示词按 MCP 规范暴露出去。← 本模块就是它。
 *   3. MCP Client（客户端/消费方）：连接 Server，列出可用工具，并在大模型需要时调用它们。
 *   4. 工具（Tool）：一个可被调用的函数（如“查天气”“取当前时间”），有名字、描述、入参 schema。
 *
 * 【怎么做】
 *   - 用 @Tool 注解在普通 Java 方法上声明工具（见 WeatherTools）。
 *   - 声明一个 ToolCallbackProvider Bean，用 MethodToolCallbackProvider 把工具对象包装进去
 *     （见 McpServerConfig）。MCP Server Starter 会自动把它们注册为 MCP 工具。
 *   - 在 application.yml 里配置服务器名字、版本、传输方式等。
 *
 * 【达到的目的】
 *   启动后即得到一个标准 MCP Server（webmvc 变体通过 /sse 端点对外服务），
 *   外部 MCP 客户端连上来就能看到 getWeather、getCurrentTime 这些工具并调用。
 *
 * 注意：本模块重在“能编译通过 + 概念讲清”，不要求实际联调运行。
 *       由于这是个 Web 服务器应用，运行后会常驻不退出（这是正常现象）。
 * ============================================================================
 */
@SpringBootApplication
public class McpServerApplication {

    public static void main(String[] args) {
        // 启动 Spring Boot 应用：
        //   1. 创建 Spring 容器；
        //   2. MCP Server Starter 自动配置出 MCP Server，并扫描 ToolCallbackProvider，把工具注册为 MCP 工具；
        //   3. webmvc 变体启动内嵌 Web 容器，对外暴露 /sse 端点等待客户端连接。
        SpringApplication.run(McpServerApplication.class, args);
    }
}
