package com.example.aicodemother.core;

import com.example.aicodemother.exception.ErrorCode;
import com.example.aicodemother.exception.ThrowUtils;
import com.example.aicodemother.model.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

import java.io.File;
import java.util.List;


@SpringBootTest
class AiCodeGeneratorFacadeTest {

    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;


    @Test
    void generate() {

        File file = aiCodeGeneratorFacade.generateAndSaveCode("帮我生成注册网站，代码控制在20行", CodeGenTypeEnum.HTML);
        ThrowUtils.throwIf(file == null, ErrorCode.SYSTEM_ERROR, "生成代码失败");
        System.out.println(file.getAbsolutePath());

    }


    @Test
    void generateAndSaveCode() {

        Flux<String> stringFlux = aiCodeGeneratorFacade.generateAndSaveCodeStream("帮我生成注册网站，代码控制在20行", CodeGenTypeEnum.MULTI_FILE);
        List<String> block = stringFlux.collectList().block();
        Assertions.assertNotNull(block);
        String resul = String.join("", block);
        Assertions.assertNotNull(resul);


    }

    @Test
    void generateAndSaveCodeStream() {
    }
}

