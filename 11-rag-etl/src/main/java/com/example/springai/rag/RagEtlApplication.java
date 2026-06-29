package com.example.springai.rag;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ============================================================================
 * 模块 11：RAG（检索增强生成）+ 文档 ETL 管道 —— 启动类
 * ============================================================================
 *
 * 【这个模块是做什么的】
 *   RAG = Retrieval-Augmented Generation（检索增强生成）。
 *   大模型只知道训练时见过的公开知识，对“你公司内部的规章”一无所知，硬问它只会瞎编。
 *   RAG 的思路：先从你自己的知识库里检索出相关资料，再把资料塞进提示词，
 *   让大模型“看着资料回答”，从而答得准、不胡编。
 *
 * 【怎么做（两条主线）】
 *   1) ETL 文档处理管道：把原始文档加工成可检索的小片段。
 *        读取(Extract) -> 切分(Transform) -> 向量化入库(Load)
 *        - TextReader      读取 classpath 下的 company.txt，得到 List<Document>
 *        - TokenTextSplitter 把长文按 token 切成若干小块（便于精确检索）
 *        - SimpleVectorStore 把切好的块向量化后存入内存向量库
 *   2) RAG 问答：给 ChatClient 挂上 QuestionAnswerAdvisor。
 *        每次提问时，它会自动去向量库检索相关片段，拼进提示词再发给大模型。
 *
 * 【达到的目的】
 *   对比演示：同一个问题“我们公司每周几远程办公？年假多少天？”
 *     - 不挂 advisor：模型不知道“星辰科技”这家虚构公司，答不出/瞎编。
 *     - 挂 advisor：先检索到 company.txt 里的相关段落，模型据此准确回答。
 *
 * 【运行前提：需要两个 Key】
 *   - chat 走 DeepSeek            -> 需要 DEEPSEEK_API_KEY
 *   - embedding（向量化）走 OpenAI -> 需要 OPENAI_API_KEY
 * ============================================================================
 */
@SpringBootApplication
public class RagEtlApplication {
    public static void main(String[] args) {
        SpringApplication.run(RagEtlApplication.class, args);
    }
}
