package com.example.springai.advisors;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;

/**
 * ============================================================================
 * 自定义 Advisor：统计每次对话调用的耗时并打印
 * ============================================================================
 *
 * 【它做什么】
 *   在"请求发给模型之前"记下开始时间，"模型返回之后"算出耗时并打印。
 *   这就是一个最朴素但很实用的"性能监控切面"。
 *
 * 【怎么做 —— Advisor 的责任链(Chain)模型】
 *   多个 Advisor 串成一条链，请求像水流一样依次穿过每个 Advisor：
 *
 *     请求 ─►[Advisor A]─►[Advisor B]─►[... 最终调用大模型 ...]
 *                                            │
 *     响应 ◄─[Advisor A]◄─[Advisor B]◄───────┘
 *
 *   关键就在 adviseCall 里调用 chain.nextCall(request)：
 *     - 调用它之前的代码 = "请求前置处理"（前半段）
 *     - 调用它本身       = "把请求交给链上的下一个 Advisor，直到真正访问模型"
 *     - 调用它之后的代码 = "响应后置处理"（后半段）
 *   我们把计时代码分别放在 nextCall 的前后，就能量出整条链的耗时。
 *
 * 【接口说明】（签名均以 spring-ai 1.1.7 的 javap 查证为准）
 *   实现 org.springframework.ai.chat.client.advisor.api.CallAdvisor 接口，
 *   它继承自 Advisor（而 Advisor 又继承自 Spring 的 Ordered 接口），需要实现 3 个方法：
 *     - ChatClientResponse adviseCall(ChatClientRequest, CallAdvisorChain)：核心拦截逻辑
 *     - String getName()：Advisor 的名字（来自 Advisor 接口）
 *     - int getOrder()：执行顺序（来自 Ordered 接口）
 *   注：CallAdvisor 处理"非流式 call()"。若还想拦截流式 stream()，可实现 StreamAdvisor，
 *       或直接实现 BaseAdvisor（用 before/after 两个钩子，同时覆盖 call 与 stream）。
 * ============================================================================
 */
public class ElapsedTimeAdvisor implements CallAdvisor {

    /**
     * order 值：决定本 Advisor 在责任链中的位置。
     * 数值越小，越靠近"外层/越先执行前置逻辑、越后执行后置逻辑"（类似洋葱圈的最外层）。
     * 我们给一个很小的值，让计时器尽量包在最外层，从而量到包含其它 Advisor 在内的总耗时。
     */
    private final int order;

    public ElapsedTimeAdvisor(int order) {
        this.order = order;
    }

    /**
     * 核心拦截方法：非流式 call() 调用时会走到这里。
     *
     * @param request 本次对话请求（包含 Prompt、上下文 context 等，是一个不可变 Record）
     * @param chain   责任链：调用 chain.nextCall(request) 才会继续往下走（最终访问大模型）
     * @return 模型返回的响应（ChatClientResponse，包含 ChatResponse 与上下文）
     */
    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        // —— 前置处理：记录开始时间 ——
        long start = System.currentTimeMillis();
        System.out.println("⏱️ [ElapsedTimeAdvisor] 开始调用大模型……");

        // —— 把请求交给链上的下一个环节（最终会真正请求大模型）——
        ChatClientResponse response = chain.nextCall(request);

        // —— 后置处理：计算并打印耗时 ——
        long cost = System.currentTimeMillis() - start;
        System.out.println("⏱️ [ElapsedTimeAdvisor] 调用结束，耗时 " + cost + " ms");

        // 必须把响应原样（或加工后）返回给链上的上一个环节
        return response;
    }

    /** Advisor 的名字，方便在日志/调试中识别。 */
    @Override
    public String getName() {
        return "ElapsedTimeAdvisor";
    }

    /** 执行顺序：值越小越靠外层。 */
    @Override
    public int getOrder() {
        return order;
    }
}
