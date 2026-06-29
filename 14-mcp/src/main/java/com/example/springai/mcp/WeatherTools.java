package com.example.springai.mcp;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * ============================================================================
 * MCP 工具集合：天气 + 时间
 * ============================================================================
 *
 * 【这个类是做什么的】
 *   用 @Tool 注解把普通 Java 方法声明成「工具」。这些方法之后会被包装成 MCP 工具，
 *   通过 MCP Server 暴露给外部客户端调用。
 *
 * 【@Tool / @ToolParam 是什么】
 *   - @Tool：标记某个方法是一个工具。description 非常重要——大模型正是靠这段自然语言描述
 *            来判断“什么时候该调用这个工具”，请写清楚工具的用途。
 *   - @ToolParam：描述工具的某个入参（是否必填 + 含义说明），同样会变成给模型看的 schema。
 *
 * 【它和模块 08 工具调用有什么关系】
 *   写法完全一样（都是 @Tool 注解的方法）。区别只是“谁来调用”：
 *     - 模块 08：本应用内的 ChatClient 直接调用这些工具（进程内）。
 *     - 本模块：把这些工具通过 MCP 协议暴露出去，给“别的”AI 应用跨进程调用。
 *
 * 【流程示意（工具被 MCP 客户端调用时）】
 *
 *   MCP 客户端的大模型  ──"我需要查北京天气"──►  MCP Client
 *        │                                          │  按 MCP 协议发起 tools/call
 *        │                                          ▼
 *        │                                     本 MCP Server
 *        │                                          │  反射调用 getWeather("北京")
 *        │                                          ▼
 *        │                                     WeatherTools.getWeather()
 *        │ ◄────────────"北京 晴 26℃"──────────────┘
 *        ▼
 *   模型据此生成最终回答
 * ============================================================================
 */
@Component // 注册为 Spring Bean，方便在 McpServerConfig 里把它交给 MethodToolCallbackProvider
public class WeatherTools {

    /**
     * 工具1：根据城市名查询天气。
     * 这里用写死的假数据演示（真实场景应调用气象 API）。
     *
     * @param city 城市名，由调用方（大模型）从用户问题里抽取后传入
     * @return 该城市的天气描述
     */
    @Tool(description = "查询指定城市的当前天气情况，输入城市名（如“北京”），返回天气与温度。")
    public String getWeather(
            @ToolParam(required = true, description = "要查询天气的城市名称，例如：北京、上海") String city) {
        // 演示用假数据：真实项目里这里应改成调用第三方天气 API。
        return String.format("%s：晴，气温 26℃，东南风 2 级，空气质量良。", city);
    }

    /**
     * 工具2：获取服务器当前时间。
     * 这种“无参工具”也很常见（如取当前时间、取随机数等）。
     *
     * @return 当前日期时间字符串
     */
    @Tool(description = "获取服务器当前的日期和时间，无需任何参数。")
    public String getCurrentTime() {
        // 取系统当前时间并格式化成易读字符串
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
