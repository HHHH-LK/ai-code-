package com.example.aicodemother.ai;

import com.example.aicodemother.ai.model.HtmlCodeResult;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;


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