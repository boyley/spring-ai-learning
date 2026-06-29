package com.example.springai.structured;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ============================================================================
 * 模块 04：结构化输出 —— 启动类
 * ============================================================================
 *
 * 【这个模块是做什么的】
 *   大模型默认返回的是"一段文字"。但在真实程序里，我们往往需要的是"结构化数据"——
 *   一个 Java 对象、一个 List、一个 Map，这样才能直接 set 到字段、存数据库、做计算。
 *   本模块演示如何让 Spring AI 把 AI 的文本回答"自动转换"成 Java 类型：
 *     1) 转单个对象：把回答转成一个 record（ActorFilms）。
 *     2) 转 List：把回答转成 List<ActorFilms>。
 *     3) 转 Map：把回答转成 Map<String, Object>。
 *     4) 揭示底层原理：BeanOutputConverter 如何生成"JSON 格式说明"。
 *
 * 【怎么做】
 *   在调用链末尾用 .entity(类型) 代替 .content()。Spring AI 会：
 *     ① 用 BeanOutputConverter 根据目标类型生成一段"请按这个 JSON 格式回答"的说明，
 *        并悄悄追加到我们的提示词后面；
 *     ② 拿到模型返回的 JSON 文本后，自动反序列化成目标 Java 对象。
 *
 * 【达到的目的】
 *   学会用一行 .entity(...) 把 AI 回答变成强类型 Java 数据，
 *   让大模型的输出能无缝接入后续业务代码。
 * ============================================================================
 */
@SpringBootApplication
public class StructuredOutputApplication {
    public static void main(String[] args) {
        SpringApplication.run(StructuredOutputApplication.class, args);
    }
}
