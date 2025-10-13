package com.example.aicodemother;

import dev.langchain4j.community.store.embedding.redis.RedisEmbeddingStore;
import dev.langchain4j.community.store.embedding.redis.spring.RedisEmbeddingStoreAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication(exclude = {RedisEmbeddingStoreAutoConfiguration.class})
@MapperScan("com.example.aicodemother.mapper")
@EnableTransactionManagement
public class AiCodeMotherApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiCodeMotherApplication.class, args);
    }

}
