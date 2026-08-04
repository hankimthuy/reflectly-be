package org.mentorship.reflectly.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    /** Backs @Async memory-extraction calls off the end-conversation endpoint — small, bounded pool is plenty at this scale. */
    @Bean(name = "memoryExtractionExecutor")
    public Executor memoryExtractionExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("memory-extraction-");
        executor.initialize();
        return executor;
    }
}
