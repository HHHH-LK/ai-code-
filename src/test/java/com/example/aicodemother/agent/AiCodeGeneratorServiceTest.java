package com.example.aicodemother.agent;

import com.example.aicodemother.agent.model.HtmlCodeResult;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
class AiCodeGeneratorServiceTest {

    @Resource
    private AiCodeGeneratorService aiCodeGeneratorService;

    @Test
    void generateHtmlCode() {
        HtmlCodeResult htmlCodeResult = aiCodeGeneratorService.generateHtmlCode("帮我创建一个程序员LK的博客，代码不超过50行");
        Assertions.assertNotNull(htmlCodeResult);
        System.out.println(htmlCodeResult);
    }

    @Test
    void testChatMemory() {

    }

}