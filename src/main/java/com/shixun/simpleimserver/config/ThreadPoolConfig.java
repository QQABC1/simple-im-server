package com.shixun.simpleimserver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync // 开启异步支持
public class ThreadPoolConfig {

    // 定义一个专门用于"状态通知"的线程池Bean
    @Bean("statusNotifyExecutor")
    public Executor statusNotifyExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 核心线程数：根据业务繁忙程度设定，建议 CPU 核心数
        executor.setCorePoolSize(8);
        // 最大线程数
        executor.setMaxPoolSize(10);
        // 队列容量：防止突发流量堆积
        executor.setQueueCapacity(200);
        // 线程名前缀：方便排查日志
        executor.setThreadNamePrefix("notify-status-");
        // 拒绝策略：主线程调用，保证不丢消息
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

    @Bean("taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 1. 核心参数配置
        // 核心线程数：服务器核数 * 2 (IO密集型推荐值)
        int core = Runtime.getRuntime().availableProcessors() * 2;
        executor.setCorePoolSize(core); // 例如 4核机器就是 8

        // 最大线程数：核心数 * 2 ~ 4
        executor.setMaxPoolSize(core * 4); // 例如 32

        // 队列容量：不要太大，防止 OOM
        executor.setQueueCapacity(500);

        // 线程名前缀：方便在日志里排查问题 (例如: Async-Thread-1)
        executor.setThreadNamePrefix("Global-Async-");

        // 2. 拒绝策略 (CallerRunsPolicy)
        // 重点：如果队列满了，线程也忙不过来了，不要抛异常，而是由“调用者所在的线程”自己去执行。
        // 这是一种“背压”保护机制，防止系统被冲垮。
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        executor.initialize();
        return executor;
    }
}
