package com.example.springai.tool;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ============================================================================
 * 模块 08：Tool Calling（工具调用 / 函数调用）—— 启动类
 * ============================================================================
 *
 * 【这个模块是做什么的】
 *   大模型本身不知道"现在几点""某地天气"这类实时/外部信息。
 *   工具调用(Tool Calling，也叫 Function Calling)让模型在回答时，
 *   能"主动请求"调用你写好的 Java 方法去拿这些数据，然后基于结果再回答。
 *
 * 【怎么做】
 *   1) 写一个普通 Java 类 DateTimeTools，在方法上加 @Tool 注解（参数加 @ToolParam）描述用途。
 *   2) 调用时用 .tools(new DateTimeTools()) 把工具交给 ChatClient。
 *   3) 提一个需要实时数据的问题，模型会自动决定调用哪个方法、Spring AI 自动执行并回传结果。
 *
 * 【达到的目的】
 *   理解工具调用的"完整回合"：模型判断需要工具 → 返回调用请求 →
 *   Spring AI 执行你的方法 → 结果回传模型 → 模型生成最终回答。
 *   这是构建"能查数据库、调 API、查天气"等智能体(Agent)应用的基础。
 * ============================================================================
 */
@SpringBootApplication
public class ToolCallingApplication {
    public static void main(String[] args) {
        SpringApplication.run(ToolCallingApplication.class, args);
    }
}
