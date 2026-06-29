package com.example.springai.embedding;

import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * ============================================================================
 * Embedding（文本向量化）三大演示
 * ============================================================================
 *
 * 【核心思想：什么是向量？】
 *   把“文字”交给 embedding 模型，它会输出一串浮点数（float[]），例如：
 *       "猫"  -> [0.012, -0.083, 0.250, ... ]   （text-embedding-3-small 是 1536 维）
 *   这串数字是文字“语义坐标”。语义相近的词，坐标也相近。
 *
 * 【流程图】
 *
 *      "你好世界"                EmbeddingModel               OpenAI
 *          │                          │                        │
 *          │  embed("你好世界")        │                        │
 *          │ ───────────────────────► │  HTTP 请求(文本)        │
 *          │                          │ ─────────────────────► │
 *          │                          │  HTTP 响应(向量数组)     │
 *          │                          │ ◄───────────────────── │
 *          │  返回 float[1536]         │                        │
 *          │ ◄─────────────────────── │                        │
 *          ▼
 *      打印维度 + 前几个数值
 *
 * 【为什么要学它】
 *   有了向量，就能用“余弦相似度”衡量两段文字有多像，从而做语义检索、推荐、
 *   去重、聚类，以及 RAG（先检索资料再让大模型回答）。
 * ============================================================================
 */
@Component
public class EmbeddingDemoRunner implements CommandLineRunner {

    /**
     * EmbeddingModel 是 Spring AI 对“向量化模型”的统一抽象。
     * 它由 spring-ai-starter-model-openai 自动配置：根据共享配置里的 embedding 段
     * （base-url 指向 OpenAI、model=text-embedding-3-small）建好连接，直接注入即可用。
     */
    private final EmbeddingModel embeddingModel;

    public EmbeddingDemoRunner(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    @Override
    public void run(String... args) {
        demo1_singleEmbed();
        demo2_batchEmbed();
        demo3_cosineSimilarity();
        System.out.println("\n========== 模块09 演示全部结束 ==========\n");
    }

    /** 演示1：单条文本向量化。embed(String) 返回 float[]，就是这条文字的语义向量。 */
    private void demo1_singleEmbed() {
        System.out.println("\n===== 演示1：单条文本向量化 embed(String) =====");

        // embed(...) 把一句话转成向量数组。text-embedding-3-small 默认输出 1536 维。
        float[] v = embeddingModel.embed("你好世界");

        // v.length 就是向量的“维度”：这串数字有多少个。
        System.out.println("文本「你好世界」的向量维度 = " + v.length);

        // 向量太长，只看前 8 个数值感受一下它长什么样（一堆小数）。
        System.out.print("前 8 个数值 = [");
        for (int i = 0; i < Math.min(8, v.length); i++) {
            System.out.printf("%.4f%s", v[i], i < 7 ? ", " : "");
        }
        System.out.println(" ...]");
    }

    /**
     * 演示2：批量向量化。
     * embedForResponse(List<String>) 一次把多条文本送去向量化，返回 EmbeddingResponse，
     * 比逐条 embed 更省网络往返。EmbeddingResponse 里装着每条文本对应的结果。
     */
    private void demo2_batchEmbed() {
        System.out.println("\n===== 演示2：批量向量化 embedForResponse(List) =====");

        // 一次性把「猫」「狗」「汽车」三条文本都转成向量。
        EmbeddingResponse resp = embeddingModel.embedForResponse(List.of("猫", "狗", "汽车"));

        // getResults() 返回 List<Embedding>，顺序与输入一致；每个 Embedding 里有 getOutput()=float[]。
        List<Embedding> results = resp.getResults();
        System.out.println("一共得到 " + results.size() + " 条向量：");
        for (int i = 0; i < results.size(); i++) {
            Embedding e = results.get(i);
            System.out.printf("  第 %d 条：index=%d，维度=%d%n",
                    i + 1, e.getIndex(), e.getOutput().length);
        }
    }

    /**
     * 演示3：余弦相似度（cosine similarity）。
     * 它衡量两个向量“方向”有多接近：结果在 -1 ~ 1 之间，越接近 1 表示语义越相近。
     * 我们比较「猫 vs 狗」（都是动物，应较相似）和「猫 vs 汽车」（差很远，应较低）。
     */
    private void demo3_cosineSimilarity() {
        System.out.println("\n===== 演示3：余弦相似度对比（语义越近，分越高）=====");

        float[] cat = embeddingModel.embed("猫");
        float[] dog = embeddingModel.embed("狗");
        float[] car = embeddingModel.embed("汽车");

        double catDog = cosineSimilarity(cat, dog);
        double catCar = cosineSimilarity(cat, car);

        System.out.printf("「猫」 vs 「狗」  相似度 = %.4f%n", catDog);
        System.out.printf("「猫」 vs 「汽车」相似度 = %.4f%n", catCar);
        System.out.println("结论：猫和狗都是动物，语义更接近，所以相似度更高；"
                + "猫和汽车几乎不相关，相似度更低。");
    }

    /**
     * 余弦相似度公式：cos = (A·B) / (|A| * |B|)
     *   - A·B   ：两个向量的“点积”（对应位置相乘再求和）
     *   - |A|   ：向量的“模长”（各元素平方和再开方）
     * 结果越接近 1，两向量方向越一致，代表两段文字语义越相近。
     */
    private double cosineSimilarity(float[] a, float[] b) {
        if (a.length != b.length) {
            throw new IllegalArgumentException("两个向量维度必须相同");
        }
        double dot = 0.0;     // 点积 A·B
        double normA = 0.0;   // |A|^2
        double normB = 0.0;   // |B|^2
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        // 防止除以 0（理论上向量不会全 0）
        if (normA == 0 || normB == 0) {
            return 0.0;
        }
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}
