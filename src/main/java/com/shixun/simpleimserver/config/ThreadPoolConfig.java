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
}
