package com.example.springai.agents;

import org.springframework.ai.chat.client.ChatClient;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * ============================================================================
 * 模式3：并行化 Parallelization
 * ============================================================================
 *
 * 【是什么】把同一个输入，用多个不同视角/侧重点的提示，同时（并行）发起多次 LLM 调用，
 *          最后把各路结果汇总。利用并发缩短总耗时，并获得更全面的多角度结果。
 *
 * 【何时用】一个问题可以从多个独立维度分析、且各维度互不依赖时
 *          （如评估一个创业点子：分别从"市场""技术""风险"三个角度看）。
 *
 * 【数据流动 ASCII 图】
 *
 *                    ┌─(并行)─► [视角1: 市场] ──► 结果1 ┐
 *   同一个输入 ──────┼─(并行)─► [视角2: 技术] ──► 结果2 ┼──► [汇总] ──► 综合结论
 *                    └─(并行)─► [视角3: 风险] ──► 结果3 ┘
 *
 *   三次调用同时进行（CompletableFuture），不必互相等待，最后再 join 收集。
 * ============================================================================
 */
public class ParallelizationWorkflow {

    private final ChatClient chatClient;

    public ParallelizationWorkflow(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    /**
     * 对同一输入，用多个视角并行各调用一次，再把结果汇总。
     *
     * @param idea       要分析的对象（如一个创业点子）
     * @param viewpoints 多个分析视角
     * @return 汇总后的多视角分析文本
     */
    public String run(String idea, List<String> viewpoints) {
        // 为每个视角创建一个异步任务，supplyAsync 会把任务丢到公共线程池里并行执行。
        List<CompletableFuture<String>> futures = viewpoints.stream()
                .map(view -> CompletableFuture.supplyAsync(() ->
                        chatClient.prompt()
                                // 每个任务用不同视角约束模型，只聚焦自己负责的那个维度。
                                .system("你是分析专家，请只从【" + view + "】这一个角度，用 2~3 句话简要分析。")
                                .user("请分析这个点子：" + idea)
                                .call()
                                .content()))
                .toList();

        // 等所有并行任务都完成，再按"视角：结果"的形式拼起来。
        // join() 会阻塞直到该任务完成；因为是并行启动的，总耗时≈最慢的那一个，而非求和。
        String merged = futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.joining("\n----\n"));

        System.out.println("【并行多视角分析结果】\n" + merged);
        return merged;
    }
}
