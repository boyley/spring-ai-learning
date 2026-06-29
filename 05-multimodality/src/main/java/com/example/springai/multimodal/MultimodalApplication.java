package com.example.springai.multimodal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ============================================================================
 * 模块 05：多模态（Multimodality）—— 启动类
 * ============================================================================
 *
 * 【这个模块是做什么的】
 *   演示"多模态"输入：把【图片 + 文字】一起发给大模型，让它"看懂"图片并用文字回答。
 *   例如：发一张图 + 问"这张图片里有什么？"，模型会用中文描述图片内容。
 *
 * 【怎么做】
 *   用 ChatClient 的 user(...) 链式 API，在同一条用户消息里：
 *     - 用 text(...)  放文字问题；
 *     - 用 media(...) 放图片（可以是图片 URL，也可以是 classpath 本地图片）。
 *   模型同时拿到"文字 + 图片"，于是能结合二者作答。
 *
 * 【达到的目的】
 *   理解"多模态"概念，掌握用 Spring AI 发送图文混合消息的写法。
 *
 * 【★ 重要：为什么本模块要换成 OpenAI】
 *   本项目其它对话模块都用 DeepSeek（便宜、国内直连），但 DeepSeek【不支持视觉】，
 *   无法理解图片。所以本模块在自己的 application.yml 里把 chat 能力覆盖成了
 *   OpenAI 的视觉模型 gpt-4o-mini（详见本模块 application.yml 的注释与 README）。
 * ============================================================================
 */
@SpringBootApplication
public class MultimodalApplication {
    public static void main(String[] args) {
        SpringApplication.run(MultimodalApplication.class, args);
    }
}
