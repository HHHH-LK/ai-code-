package com.example.aicodemother.config;

import com.example.aicodemother.model.enums.TheadExecutorTypeEnum;
import org.apache.tomcat.util.threads.ThreadPoolExecutor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.*;

/**
 * @program: ai-code-mother
 * @description: 线程池创建工厂
 * @author: lk_hhh
 * @create: 2025-10-15 22:25
 **/

@Component
public class TheadExecutorFactory {

    Map<TheadExecutorTypeEnum, ExecutorService> theadExecutorMap = new ConcurrentHashMap<>();

    public ExecutorService getTheadExecutor(TheadExecutorTypeEnum typeEnum) {
        ExecutorService executors = theadExecutorMap.get(typeEnum);
        if (executors == null) {
            return createTheadExecutor(typeEnum);
        }
        return executors;
    }


    private ExecutorService createTheadExecutor(TheadExecutorTypeEnum typeEnum) {

        try (ExecutorService threadPoolExecutor = Executors.newVirtualThreadPerTaskExecutor()) {
            theadExecutorMap.put(typeEnum, threadPoolExecutor);
            return threadPoolExecutor;
        }

    }


}