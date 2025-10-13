package com.example.aicodemother.core;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import com.example.aicodemother.agent.model.HtmlCodeResult;
import com.example.aicodemother.agent.model.MultiFileCodeResult;
import com.example.aicodemother.constant.AppConstant;
import com.example.aicodemother.model.enums.CodeGenTypeEnum;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * @program: ai-code-mother
 * @description: 将生成的代码文件保存到对应目录中
 * @author: lk_hhh
 * @create: 2025-10-06 18:23
 **/

@Deprecated
public class CodeFileSaver {

    //设置根目录
    public static final String FILE_SAVE_ROOT_PATH = AppConstant.CODE_OUTPUT_ROOT_DIR;

    /**
     * 构建并创建唯一的文件目录路径
     * 该方法根据业务类型和雪花算法生成的唯一ID创建一个目录
     *
     * @param bizType 业务类型，用于区分不同业务的文件存储目录
     * @return 返回创建好的完整目录路径字符串
     */
    private static String buildUniqueFileDir(String bizType) {
        // 拼接目录路径，格式为：根路径/业务类型_雪花算法生成的唯一ID字符串
        String uniqueFileDirName = FILE_SAVE_ROOT_PATH + File.separator + bizType + "_" + IdUtil.getSnowflakeNextIdStr();
        // 创建目录，如果目录已存在则不创建
        FileUtil.mkdir(uniqueFileDirName);
        // 返回创建好的目录路径
        return uniqueFileDirName;
    }

    /**
     * 保存文件的方法
     *
     * @param dirPath     文件所在的目录路径
     * @param fileName    文件名
     * @param fileContent 文件内容
     */
    private static void writeToFile(String dirPath, String fileName, String fileContent) {
        //编写文件路径，将目录路径和文件名通过系统分隔符连接
        String filePath = dirPath + File.separator + fileName;
        // 使用FileUtil工具类将文件内容以UTF-8编码写入指定路径
        FileUtil.writeString(fileContent, filePath, StandardCharsets.UTF_8);

    }

    //保存HTML代码文件
    public static File saveHtmlCodeResult(HtmlCodeResult htmlCodeResult) {
        String htmlCodingPath = buildUniqueFileDir(CodeGenTypeEnum.HTML.getValue());
        writeToFile(htmlCodingPath, "index.html", htmlCodeResult.getHtmlCode());
        return new File(htmlCodingPath);
    }

    //保存多文件的代码
    public static File saveMultiFileCodeResult(MultiFileCodeResult multiFileCodeResult) {
        //获取相关代码生成结果
        String cssCode = multiFileCodeResult.getCssCode();
        String htmlCode = multiFileCodeResult.getHtmlCode();
        String jsCode = multiFileCodeResult.getJsCode();
        //设置相关路径
        String multiFileCodingPath = buildUniqueFileDir(CodeGenTypeEnum.MULTI_FILE.getValue());
        //写入文件
        writeToFile(multiFileCodingPath, "index.html", htmlCode);
        writeToFile(multiFileCodingPath, "style.css", cssCode);
        writeToFile(multiFileCodingPath, "script.js", jsCode);
        //返回相对应的文件目录地址
        return new File(multiFileCodingPath);
    }

}