package com.example.springai.rag;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * ============================================================================
 * RAG（检索增强生成）+ 文档 ETL 管道 演示
 * ============================================================================
 *
 * 【RAG 整体流程图】
 *
 *   ① 离线准备阶段（ETL：把知识灌进向量库）
 *      company.txt
 *        │ TextReader 读取 (Extract)
 *        ▼
 *      List<Document>（整篇）
 *        │ TokenTextSplitter 切分 (Transform)
 *        ▼
 *      List<Document>（很多小片段）
 *        │ SimpleVectorStore.add 向量化入库 (Load)
 *        ▼
 *      [ 向量库 ]
 *
 *   ② 在线问答阶段（Retrieval-Augmented Generation）
 *      用户问题 ──► QuestionAnswerAdvisor
 *                     │ 1.去向量库检索最相关的几段
 *                     │ 2.把这些片段拼进提示词
 *                     ▼
 *                  大模型(DeepSeek) ──► 基于资料的准确回答
 *
 * 【一句话总结】
 *   先“查资料”，再“看着资料答”。这样模型就能回答它本来不知道的私有知识，且不易胡编。
 * ============================================================================
 */
@Component
public class RagEtlDemoRunner implements CommandLineRunner {

    private final ChatClient chatClient;
    private final SimpleVectorStore vectorStore;

    /**
     * 构造器里完成两件事：
     *   1) 用 ChatClient.Builder 构建对话客户端（chat 走 DeepSeek）。
     *   2) 用 EmbeddingModel 创建内存向量库（向量化走 OpenAI）。
     */
    public RagEtlDemoRunner(ChatClient.Builder builder, EmbeddingModel embeddingModel) {
        this.chatClient = builder.build();
        this.vectorStore = SimpleVectorStore.builder(embeddingModel).build();
    }

    @Override
    public void run(String... args) {
        // 先把知识库灌进向量库（ETL），再做问答对比。
        etlLoadDocuments();
        demo_withoutRag();
        demo_withRag();
        System.out.println("\n========== 模块11 演示全部结束 ==========\n");
    }

    /**
     * 演示1（ETL 管道）：读取 -> 切分 -> 向量化入库。
     */
    private void etlLoadDocuments() {
        System.out.println("\n===== 演示1：ETL 文档管道（读取→切分→入库）=====");

        // Extract（读取）：TextReader 读取 classpath 下的纯文本资源，得到 List<Document>。
        // ClassPathResource 指向 src/main/resources/docs/company.txt。
        TextReader reader = new TextReader(new ClassPathResource("docs/company.txt"));
        List<Document> rawDocs = reader.get();   // get() 返回读到的文档（通常整篇是 1 个 Document）
        System.out.println("① 读取完成，原始文档数 = " + rawDocs.size());

        // Transform（切分）：TokenTextSplitter 按 token 把长文切成若干小片段。
        // 切小的原因：检索时能更精准定位到相关段落，也避免一次塞太多内容给模型。
        TokenTextSplitter splitter = new TokenTextSplitter();
        List<Document> chunks = splitter.apply(rawDocs);   // apply(List) 返回切分后的片段列表
        System.out.println("② 切分完成，得到片段数 = " + chunks.size());

        // Load（入库）：add 时向量库会自动把每个片段转成向量并保存。
        vectorStore.add(chunks);
        System.out.println("③ 入库完成，向量库已就绪。");
    }

    /**
     * 演示2：不挂 advisor，直接问。
     * 模型对“星辰科技”这家虚构公司一无所知，只能回答“不知道”或瞎猜。
     */
    private void demo_withoutRag() {
        System.out.println("\n===== 演示2：不用 RAG 直接问（模型不知道虚构信息）=====");
        String question = "我们公司每周几远程办公？年假多少天？";
        System.out.println("提问：" + question);

        String answer = chatClient.prompt()
                .user(question)
                .call()
                .content();
        System.out.println("AI（无知识库）：" + answer);
    }

    /**
     * 演示3：挂上 QuestionAnswerAdvisor 做 RAG。
     * advisors(...) 给本次/本客户端加上一个“顾问”：
     *   QuestionAnswerAdvisor.builder(vectorStore).build() 会在请求发给模型前，
     *   自动用用户问题去 vectorStore 检索最相关的片段，并把它们拼进提示词。
     * 于是模型“看着 company.txt 的内容”作答，能答得准确。
     */
    private void demo_withRag() {
        System.out.println("\n===== 演示3：使用 RAG 问答（先检索资料再回答）=====");
        String question = "我们公司每周几远程办公？年假多少天？";
        System.out.println("提问：" + question);

        String answer = chatClient.prompt()
                // 挂上 RAG 顾问：它负责“检索 + 把资料塞进提示词”这一步
                .advisors(QuestionAnswerAdvisor.builder(vectorStore).build())
                .user(question)
                .call()
                .content();
        System.out.println("AI（带知识库）：" + answer);
        System.out.println("对比可见：挂上 RAG 后，模型基于 company.txt 答出"
                + "「每周三远程办公、年假 20 天」，而不挂时答不出。");
    }
}
