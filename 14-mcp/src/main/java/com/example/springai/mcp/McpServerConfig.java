package com.example.springai.mcp;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ============================================================================
 * MCP Server 配置：把工具对象注册成 MCP 工具
 * ============================================================================
 *
 * 【这个类是做什么的】
 *   声明一个 ToolCallbackProvider 类型的 Bean。
 *   MCP Server Starter 在启动时会自动查找容器里所有 ToolCallbackProvider，
 *   把它们提供的工具（ToolCallback[]）逐个注册为「MCP 工具」对外暴露。
 *
 * 【关键 API】
 *   - MethodToolCallbackProvider.builder()
 *        .toolObjects(对象...)  -> 传入“持有 @Tool 方法的对象”，框架会用反射扫描这些对象上的
 *                                  @Tool 方法，自动生成对应的 ToolCallback（含名字、描述、参数 schema）。
 *        .build()              -> 得到一个 ToolCallbackProvider。
 *
 * 【为什么这样就能暴露成 MCP 工具】
 *   Spring AI 把“工具”抽象成统一的 ToolCallback；不论是给本进程 ChatClient 用，
 *   还是通过 MCP Server 暴露给外部，底层都是同一套 ToolCallback。
 *   所以我们只需把工具方法包装成 ToolCallbackProvider，剩下的“按 MCP 协议暴露”由 Starter 自动完成。
 * ============================================================================
 */
@Configuration
public class McpServerConfig {

    /**
     * 把 WeatherTools 里的 @Tool 方法包装成 MCP 工具。
     *
     * @param weatherTools 由 Spring 注入的工具对象（@Component）
     * @return ToolCallbackProvider —— 会被 MCP Server Starter 自动发现并暴露
     */
    @Bean
    public ToolCallbackProvider weatherToolCallbackProvider(WeatherTools weatherTools) {
        return MethodToolCallbackProvider.builder()
                // toolObjects 可传多个对象；这里把天气/时间工具对象交进去
                .toolObjects(weatherTools)
                .build();
    }
}
