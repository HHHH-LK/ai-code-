package com.example.aicodemother.core.filesaver;

import com.example.aicodemother.model.enums.CodeGenTypeEnum;

/**
 * @program: ai-code-mother
 * @description: Vue保存文件模版类
 * @author: lk_hhh
 * @create: 2025-10-15 08:32
 **/
public class VueProjectFileSaverTemplate extends CodeFileSaverTemplate {
    @Override
    protected String writeToFile(Object result, String createFilePath) {
        return "写入成功";
    }

    @Override
    protected CodeGenTypeEnum getResultType(Object result) {
        return CodeGenTypeEnum.VUE_PROJECT;
    }
}