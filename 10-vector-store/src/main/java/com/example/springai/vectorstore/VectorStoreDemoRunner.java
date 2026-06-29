package com.example.springai.vectorstore;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * ============================================================================
 * 向量数据库（SimpleVectorStore）语义检索演示
 * ============================================================================
 *
 * 【核心思想】
 *   传统数据库按“关键字精确匹配”查（like '%编程%'）。向量库按“语义相似度”查：
 *   先把每条文档转成向量存起来，查询时把问题也转成向量，再找“离得最近”的文档。
 *   这样即使问题里没有出现文档里的原词，只要意思相关也能命中。
 *
 * 【流程图】
 *
 *   入库阶段：
 *     文档列表 ──► EmbeddingModel(转向量) ──► 存进 SimpleVectorStore(内存Map)
 *
 *   检索阶段：
 *     "有哪些编程相关的内容？"
 *        │ 转成向量
 *        ▼
 *     在库里按余弦相似度排序 ──► 返回最相关的 topK 条文档
 *
 * 【为什么用 SimpleVectorStore】
 *   它是 Spring AI 内置的“内存版”向量库，数据存在一个 Map 里，无需安装数据库，
 *   最适合学习与小规模演示。生产环境会换成 PgVector、Redis、Milvus 等持久化向量库，
 *   但 API（add / similaritySearch）完全一致，学会它就会用别的。
 * ============================================================================
 */
@Component
public class VectorStoreDemoRunner implements CommandLineRunner {

    /**
     * SimpleVectorStore 的构建需要一个 EmbeddingModel，
     * 因为“添加文档”和“检索”时都要把文字转成向量。
     */
    private final SimpleVectorStore vectorStore;

    public VectorStoreDemoRunner(EmbeddingModel embeddingModel) {
        // SimpleVectorStore.builder(embeddingModel) 是 1.1.7 的标准创建方式（经 javap 查证）。
        // build() 后得到一个空的内存向量库。
        this.vectorStore = SimpleVectorStore.builder(embeddingModel).build();
    }

    @Override
    public void run(String... args) {
        demo1_addDocuments();
        demo2_similaritySearch();
        System.out.println("\n========== 模块10 演示全部结束 ==========\n");
    }

    /**
     * 演示1：往向量库里添加文档。
     * Document 是 Spring AI 对“一段文档”的封装；new Document("文本") 即可。
     * add(List) 入库时，向量库会自动调用 EmbeddingModel 把每条文本转成向量再保存。
     */
    private void demo1_addDocuments() {
        System.out.println("\n===== 演示1：添加文档（入库时自动向量化）=====");

        vectorStore.add(List.of(
                new Document("Spring AI 是 Java 的 AI 框架"),
                new Document("北京是中国的首都"),
                new Document("Python 是一门编程语言")
        ));

        System.out.println("已添加 3 条文档到内存向量库（每条都已被转成向量）。");
    }

    /**
     * 演示2：语义相似度检索。
     * SearchRequest.builder().query(问题).topK(N) 构造一次检索请求：
     *   - query：要检索的问题（会被转成向量）
     *   - topK ：最多返回几条最相关的文档
     * similaritySearch(...) 返回 List<Document>，已按相似度从高到低排好序。
     */
    private void demo2_similaritySearch() {
        System.out.println("\n===== 演示2：语义检索 similaritySearch() =====");

        String question = "有哪些编程相关的内容？";
        System.out.println("提问：" + question);

        List<Document> results = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(question)   // 检索问题
                        .topK(2)           // 取最相关的 2 条
                        .build()
        );

        System.out.println("命中文档（按相似度排序）：");
        for (Document d : results) {
            // getText() 取文档原文；getScore() 是相似度得分（越大越相关）。
            System.out.printf("  - %s （相似度=%.4f）%n", d.getText(), d.getScore());
        }
        System.out.println("观察：尽管问题里没有出现「Java」「Python」，"
                + "但语义相关的两条编程文档被准确命中，而「北京是首都」未被选中。");
    }
}
