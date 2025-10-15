package com.example.aicodemother.core;

import cn.hutool.json.JSONUtil;
import com.example.aicodemother.ai.AiCodeGeneratorService;
import com.example.aicodemother.ai.AiCodeGeneratorServiceFactory;
import com.example.aicodemother.ai.model.HtmlCodeResult;
import com.example.aicodemother.ai.model.MultiFileCodeResult;
import com.example.aicodemother.ai.model.message.AiResponseMessage;
import com.example.aicodemother.ai.model.message.ToolExecutedMessage;
import com.example.aicodemother.ai.model.message.ToolRequestMessage;
import com.example.aicodemother.core.filesaver.CodeFileSaverExecutor;
import com.example.aicodemother.core.parser.CodeParserExecutor;
import com.example.aicodemother.exception.BusinessException;
import com.example.aicodemother.exception.ErrorCode;
import com.example.aicodemother.model.enums.CodeGenTypeEnum;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.tool.BeforeToolExecution;
import dev.langchain4j.service.tool.ToolExecution;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;

/**
 * @program: ai-code-mother
 * @description: AI 代码生成门面类
 * @author: lk_hhh
 * @create: 2025-10-09 14:50
 **/

@Service
@Slf4j
@RequiredArgsConstructor
public class AiCodeGeneratorFacade {

    private final AiCodeGeneratorServiceFactory aiCodeGeneratorServiceFactory;

    public Flux<String> processCodeStream(Flux<String> codeStream, CodeGenTypeEnum codeGenType, Long appId) {
        StringBuilder finallyResultBuilder = new StringBuilder();

        return codeStream.doOnNext(finallyResultBuilder::append).doOnComplete(() -> {
            try {
                String finallyResult = finallyResultBuilder.toString();
                //解析文件
                Object result = CodeParserExecutor.executeParser(finallyResult, codeGenType);
                //保存文件
                File file = CodeFileSaverExecutor.executeSaver(result, codeGenType, appId);
                log.info("文件保存成功，文件路径为: {}", file.getAbsolutePath());
            } catch (Exception e) {
                log.error("文件保存失败", e);
            }
        });

    }

    /**
     * 生成并保存代码的方法
     * 根据不同的代码生成类型，调用相应的生成服务并保存结果
     *
     * @param userMessage     用户输入的消息，用于生成代码
     * @param codeGenTypeEnum 代码生成类型枚举，决定生成何种类型的代码
     * @return 生成的代码字符串
     * @throws BusinessException 当遇到不支持的代码生成类型时抛出业务异常
     */
    public File generateAndSaveCode(String userMessage, CodeGenTypeEnum codeGenTypeEnum, Long appId) {
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "代码生成类型不能为空");
        }
        return switch (codeGenTypeEnum) {
            case HTML -> generateAndSaveHtmlCode(userMessage, appId);

            case MULTI_FILE -> generateAndSaveMultiFileCode(userMessage, appId);

            default -> throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的代码生成类型");
        };
    }


    public Flux<String> generateAndSaveCodeStream(String userMessage, CodeGenTypeEnum codeGenTypeEnum, Long appId) {
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "代码生成类型不能为空");
        }
        return switch (codeGenTypeEnum) {
            case HTML -> generateAndSaveHtmlCodeStream(userMessage, appId);

            case MULTI_FILE -> generateAndSaveMultiFileCodeStream(userMessage, appId);

            case VUE_PROJECT -> generateAndSaveVueProjectStream(userMessage, appId);

            default -> throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的代码生成类型");
        };
    }


    private Flux<String> generateAndSaveVueProjectStream(String userMessage, Long appId) {
        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId, CodeGenTypeEnum.VUE_PROJECT);
        TokenStream tokenStream = aiCodeGeneratorService.generateVueCodeStream(userMessage, appId);
        return processTokenStream(tokenStream);
    }

    /**
     * 将 TokenStream 转换为 Flux<String>，并传递工具调用信息
     *
     * @param tokenStream TokenStream 对象
     * @return Flux<String> 流式响应
     */
    private Flux<String> processTokenStream(TokenStream tokenStream) {
        return Flux.create(sink -> {
            try {
                tokenStream.onPartialResponse((String partialResponse) -> {
                            // 处理 AI 的部分响应（文本内容）
                            try {
                                AiResponseMessage aiResponseMessage = new AiResponseMessage(partialResponse);
                                sink.next(JSONUtil.toJsonStr(aiResponseMessage));
                            } catch (Exception e) {
                                log.error("处理部分响应时出错: {}", partialResponse, e);
                            }
                        })
                        .beforeToolExecution((BeforeToolExecution beforeToolExecution) -> {
                            // 处理工具调用请求（在工具执行前）
                            try {
                                ToolRequestMessage toolRequestMessage = new ToolRequestMessage(beforeToolExecution.request());
                                sink.next(JSONUtil.toJsonStr(toolRequestMessage));
                                log.info("即将执行工具: {}, 参数: {}",
                                        beforeToolExecution.request().name(),
                                        beforeToolExecution.request().arguments());
                            } catch (Exception e) {
                                log.error("处理工具执行前回调时出错", e);
                            }
                        })
                        .onToolExecuted((ToolExecution toolExecution) -> {
                            // 处理工具执行结果（在工具执行后）
                            try {
                                ToolExecutedMessage toolExecutedMessage = new ToolExecutedMessage(toolExecution);
                                sink.next(JSONUtil.toJsonStr(toolExecutedMessage));
                                log.info("工具执行完成: {}, 结果: {}",
                                        toolExecution.request().name(),
                                        toolExecution.result());
                            } catch (Exception e) {
                                log.error("处理工具执行结果时出错", e);
                            }
                        })
                        .onCompleteResponse((ChatResponse response) -> {
                            // 流式响应完成
                            log.info("Vue项目代码生成完成，响应: {}", response);
                            sink.complete();
                        })
                        .onError((Throwable error) -> {
                            // 处理错误
                            String errorMessage = String.format("Vue项目代码生成出错: %s", error.getMessage());
                            log.error(errorMessage, error);
                            sink.error(new BusinessException(ErrorCode.SYSTEM_ERROR, errorMessage));
                        })
                        .start();
            } catch (Exception e) {
                log.error("启动 TokenStream 时出错", e);
                sink.error(new BusinessException(ErrorCode.SYSTEM_ERROR, e.getMessage()));
            }
        });
    }

    /**
     * 生成并保存多文件代码流
     * 该方法使用AI代码生成器生成多文件代码流，然后将完整内容保存到文件中
     *
     * @param userMessage 用户输入的消息，用于生成代码
     * @return 返回一个Flux<String>流，包含生成的代码内容
     */
    private Flux<String> generateAndSaveMultiFileCodeStream(String userMessage, Long appId) {

        // 使用AI代码生成器服务生成多文件代码流
        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId);
        Flux<String> stringFlux = aiCodeGeneratorService.generateMultiFileCodeStream(userMessage);

        return processCodeStream(stringFlux, CodeGenTypeEnum.MULTI_FILE, appId);
    }

    /**
     * 生成并保存HTML代码流
     * 该方法通过AI服务生成HTML代码流，并将完整内容保存为文件
     *
     * @param userMessage 用户输入的消息，用于生成HTML代码
     * @return 返回一个Flux<String>类型的字符串流，包含生成的HTML代码片段
     */
    private Flux<String> generateAndSaveHtmlCodeStream(String userMessage, Long appId) {

        // 使用AI代码生成服务生成HTML代码流
        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId);
        Flux<String> stringFlux = aiCodeGeneratorService.generateHtmlCodeStream(userMessage);

        return processCodeStream(stringFlux, CodeGenTypeEnum.HTML, appId);
    }


    /**
     * 生成并保存HTML代码
     * 该方法根据用户输入的消息生成HTML内容，并将其保存为文件
     *
     * @param userMessage 用户输入的消息内容，将用于生成HTML
     * @return 返回生成的HTML文件对象，如果生成失败则返回null
     */
    private File generateAndSaveHtmlCode(String userMessage, Long appId) {

        //生成代码
        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId);
        HtmlCodeResult htmlCodeResult = aiCodeGeneratorService.generateHtmlCode(userMessage);
        if (htmlCodeResult == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成HTML代码失败");
        }
        //保存代码
        return CodeFileSaverExecutor.executeSaver(htmlCodeResult, CodeGenTypeEnum.HTML, appId);
    }

    /**
     * 生成并保存多文件代码
     * 该方法根据用户输入的消息生成多文件代码并保存到指定位置
     *
     * @param userMessage 用户输入的消息，用于生成代码的内容
     * @return 返回生成的文件对象，如果生成失败则返回null
     */
    private File generateAndSaveMultiFileCode(String userMessage, Long appId) {

        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId);
        MultiFileCodeResult multiFileCodeResult = aiCodeGeneratorService.generateMultiFileCode(userMessage);
        if (multiFileCodeResult == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成多文件代码失败");
        }
        return CodeFileSaverExecutor.executeSaver(multiFileCodeResult, CodeGenTypeEnum.MULTI_FILE, appId);

    }


}