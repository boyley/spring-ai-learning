package com.example.springai.tool;

import java.time.LocalDateTime;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

/**
 * ============================================================================
 * 工具类：把普通 Java 方法暴露给大模型调用
 * ============================================================================
 *
 * 【它做什么】
 *   定义了两个"工具方法"，模型在需要时可以自动调用它们获取实时信息：
 *     - getCurrentDateTime()：返回当前日期时间。
 *     - getWeather(city)：返回某城市的天气（这里用假数据演示，真实项目里会去调天气 API）。
 *
 * 【关键注解】（包路径与属性均以 spring-ai 1.1.7 的 javap 查证为准）
 *   org.springframework.ai.tool.annotation.Tool
 *     - description：必填，向模型描述"这个工具能干什么"。模型靠它判断何时调用，描述要清晰！
 *     - name：可选，工具名（默认用方法名）。
 *     - returnDirect：可选，是否把工具结果直接返回给用户（跳过模型再加工）。
 *   org.springframework.ai.tool.annotation.ToolParam
 *     - description：向模型描述"这个参数是什么"，模型据此从用户问题里抽取参数值。
 *     - required：可选，参数是否必填（默认 true）。
 *
 * 【小白须知】
 *   你不需要自己解析模型的意图、也不用手动调用这些方法——
 *   只要把工具交给 ChatClient（.tools(...)），剩下的"判断+调用+回传"都由 Spring AI 自动完成。
 * ============================================================================
 */
public class DateTimeTools {

    /**
     * 工具1：获取当前日期和时间。
     * 当用户问"现在几点了""今天几号"等，模型会自动调用本方法。
     */
    @Tool(description = "获取当前的日期和时间")
    String getCurrentDateTime() {
        // 真实运行时这里返回服务器当前时间；模型拿到后会用自然语言转述给用户。
        String now = LocalDateTime.now().toString();
        System.out.println("🔧 [工具被调用] getCurrentDateTime() -> " + now);
        return now;
    }

    /**
     * 工具2：获取某城市天气。
     * 参数 city 用 @ToolParam 描述，模型会从用户问题里抽取出城市名传进来。
     *
     * @param city 城市名（如"北京"）
     */
    @Tool(description = "获取指定城市的当前天气情况")
    String getWeather(@ToolParam(description = "要查询天气的城市名称，例如：北京") String city) {
        // 演示用假数据；真实项目里这里会去调用天气服务的 HTTP 接口。
        String result = city + "今天晴，25 摄氏度";
        System.out.println("🔧 [工具被调用] getWeather(\"" + city + "\") -> " + result);
        return result;
    }
}
