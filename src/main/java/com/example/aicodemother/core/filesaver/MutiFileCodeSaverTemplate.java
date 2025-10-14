package com.example.aicodemother.core.filesaver;

import com.example.aicodemother.ai.model.MultiFileCodeResult;
import com.example.aicodemother.model.enums.CodeGenTypeEnum;

/**
 * @program: ai-code-mother
 * @description: 多文件生成模版实现类
 * @author: lk_hhh
 * @create: 2025-10-09 20:26
 **/
public class MutiFileCodeSaverTemplate extends CodeFileSaverTemplate<MultiFileCodeResult> {

    @Override
    protected String writeToFile(MultiFileCodeResult result, String createFilePath) {
        String jsCode = result.getJsCode();
        String htmlCode = result.getHtmlCode();
        String cssCode = result.getCssCode();
        writeToFileUtil(createFilePath, "index.html", htmlCode);
        writeToFileUtil(createFilePath, "style.css", cssCode);
        writeToFileUtil(createFilePath, "script.js", jsCode);
        return createFilePath;

    }

    @Override
    protected CodeGenTypeEnum getResultType(MultiFileCodeResult result) {
        return CodeGenTypeEnum.MULTI_FILE;
    }
}