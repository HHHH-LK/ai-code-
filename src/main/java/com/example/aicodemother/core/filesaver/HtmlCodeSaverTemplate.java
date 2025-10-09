package com.example.aicodemother.core.filesaver;

import com.example.aicodemother.agent.model.HtmlCodeResult;
import com.example.aicodemother.model.enums.CodeGenTypeEnum;


/**
 * @program: ai-code-mother
 * @description: html生成模版实现类
 * @author: lk_hhh
 * @create: 2025-10-09 20:26
 **/
public class HtmlCodeSaverTemplate extends CodeFileSaverTemplate<HtmlCodeResult> {
    @Override
    protected String writeToFile(HtmlCodeResult result, String createFilePath) {
        writeToFileUtil(createFilePath, "index.html", result.getHtmlCode());
        return createFilePath;
    }

    @Override
    protected CodeGenTypeEnum getResultType(HtmlCodeResult result) {
        return CodeGenTypeEnum.HTML;
    }
}