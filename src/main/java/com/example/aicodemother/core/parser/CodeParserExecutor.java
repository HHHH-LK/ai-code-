package com.example.aicodemother.core.parser;

import com.example.aicodemother.exception.BusinessException;
import com.example.aicodemother.exception.ErrorCode;
import com.example.aicodemother.model.enums.CodeGenTypeEnum;

/**
 * @program: ai-code-mother
 * @description: 代码解析器执行器
 * @author: lk_hhh
 * @create: 2025-10-09 19:42
 **/
public class CodeParserExecutor {

    private final static HtmlCodeParser htmlCodeParser = new HtmlCodeParser();
    private final static MultiFileCodeParser multiFileCodeParser = new MultiFileCodeParser();


    /**
     * 执行代码解析器，根据不同的代码生成类型解析代码内容
     *
     * @param codeContent     需要解析的代码内容字符串
     * @param codeGenTypeEnum 代码生成类型枚举，指定使用哪种解析器
     * @return 解析后的结果对象，具体类型取决于解析器实现
     * @throws BusinessException 当传入不支持的代码生成类型时抛出系统异常
     */
    public static Object executeParser(String codeContent, CodeGenTypeEnum codeGenTypeEnum) {
        return switch (codeGenTypeEnum) {
            case HTML -> htmlCodeParser.parseCode(codeContent);
            case MULTI_FILE -> multiFileCodeParser.parseCode(codeContent);
            default -> throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的代码生成类型");
        };


    }


}