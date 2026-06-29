package com.example.springai.embedding;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ============================================================================
 * 模块 09：文本向量化（Embedding）—— 启动类
 * ============================================================================
 *
 * 【这个模块是做什么的】
 *   学习“文本向量化”（Embedding）：把一段文字交给模型，模型会返回一串数字
 *   （叫做“向量”，本质是 float[] 数组）。这串数字是文字“语义”的数学表示：
 *   含义相近的两段文字，得到的向量在空间里也“离得近”。
 *
 * 【怎么做】
 *   通过注入 Spring AI 自动配置好的 EmbeddingModel，演示三件事：
 *     1) 单条文本向量化：embed("你好世界") -> float[]，看看维度和前几个数值。
 *     2) 批量向量化：embedForResponse(List.of("猫","狗","汽车")) 一次转多条。
 *     3) 余弦相似度：自己写公式比较“猫 vs 狗”“猫 vs 汽车”，验证语义相近的更相似。
 *
 * 【达到的目的】
 *   理解 Embedding 是“语义检索 / 向量数据库 / RAG（检索增强生成）”的基石。
 *   只有先会把文字变成向量，后面（模块10向量库、模块11 RAG）才能做语义检索。
 *
 * 【运行前提】
 *   embedding 能力在共享配置里指向真正的 OpenAI（text-embedding-3-small），
 *   所以需要设置环境变量：export OPENAI_API_KEY=sk-你的key
 * ============================================================================
 */
@SpringBootApplication
public class EmbeddingApplication {
    public static void main(String[] args) {
        SpringApplication.run(EmbeddingApplication.class, args);
    }
}
