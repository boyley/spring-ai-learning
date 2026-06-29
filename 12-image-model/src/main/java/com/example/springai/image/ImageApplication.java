package com.example.springai.image;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ============================================================================
 * 模块 12：文生图（Image Model）—— 启动类
 * ============================================================================
 *
 * 【这个模块是做什么的】
 *   演示 Spring AI 的【文生图】能力：给 AI 一段文字描述（提示词 prompt），
 *   它就帮你"画"出一张对应的图片。底层用的是 OpenAI 的 DALL·E 模型。
 *   例如输入"一只戴眼镜在敲代码的橘猫，卡通风格"，就能得到一张这样的图。
 *
 * 【怎么做】
 *   1) 引入 spring-ai-starter-model-openai，它会自动配置好一个 ImageModel Bean。
 *   2) 注入 org.springframework.ai.image.ImageModel，构造 ImagePrompt（描述 + 参数）。
 *   3) 调用 imageModel.call(...) 拿到 ImageResponse，从中取出图片的 URL。
 *
 * 【达到的目的】
 *   理解 Spring AI 中"图片模型"这一类能力的统一抽象（ImageModel / ImagePrompt /
 *   ImageResponse），学会用文字生成图片并拿到结果链接。
 *
 * 【重要】文生图属于 OpenAI 真正的能力，DeepSeek 不支持。
 *   所以本模块必须使用真实的 OpenAI Key（环境变量 OPENAI_API_KEY）。
 * ============================================================================
 */
@SpringBootApplication
public class ImageApplication {
    public static void main(String[] args) {
        SpringApplication.run(ImageApplication.class, args);
    }
}
