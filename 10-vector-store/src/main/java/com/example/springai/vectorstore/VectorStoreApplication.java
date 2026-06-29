package com.example.springai.vectorstore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ============================================================================
 * 模块 10：向量数据库（VectorStore）—— 启动类
 * ============================================================================
 *
 * 【这个模块是做什么的】
 *   学习“向量数据库”：把一批文档先用 Embedding 转成向量存起来，之后给一个问题，
 *   它能按“语义相似度”帮你找出最相关的几条文档（而不是像 SQL 那样按关键字精确匹配）。
 *
 * 【怎么做】
 *   本模块用最简单的内存版 SimpleVectorStore（无需安装任何数据库）：
 *     1) 用注入的 EmbeddingModel 创建 SimpleVectorStore。
 *     2) add(...) 添加几条文档：入库时它会自动调用 embedding 把每条文档转成向量。
 *     3) similaritySearch(...) 传入一个问题，按语义相似度返回最相关的 topK 条文档。
 *
 * 【达到的目的】
 *   理解“语义检索”：搜“有哪些编程相关的内容？”能命中“Spring AI 是 Java 的 AI 框架”
 *   和“Python 是一门编程语言”，即使问题里根本没出现“Java/Python”这些词。
 *   这正是 RAG（模块11）的检索环节。
 *
 * 【运行前提】
 *   入库要把文档转成向量，用的是 embedding 能力（真正的 OpenAI），
 *   所以需要设置环境变量：export OPENAI_API_KEY=sk-你的key
 * ============================================================================
 */
@SpringBootApplication
public class VectorStoreApplication {
    public static void main(String[] args) {
        SpringApplication.run(VectorStoreApplication.class, args);
    }
}
