package com.example.aicodemother.ai.createcodingagent.tools;

import com.example.aicodemother.constant.AppConstant;
import com.example.aicodemother.exception.ErrorCode;
import com.example.aicodemother.exception.ThrowUtils;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolMemoryId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.*;

import static java.nio.file.StandardCopyOption.*;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;

/**
 * 文件写入工具
 * 支持 AI 通过工具调用的方式写入文件
 */
@Slf4j
@Component
public class FileWriteTool {

    @Tool("写入文件到指定路径")
    public String writeFile(@P("文件的相对路径") String relativeFilePath, @P("要写入文件的内容") String content, @ToolMemoryId Long appId) {
        Path codeRoot = Paths.get(AppConstant.CODE_OUTPUT_ROOT_DIR);
        try {
            Path target = resolveSafeTarget(codeRoot, appId, relativeFilePath);
            boolean ok = writeFileAtomically(target, content);
            return ok ? "文件写入成功: " + target.toString()
                    : "文件写入失败: " + target.toString();
        } catch (Exception e) {
            log.error("写入失败: rel={}, appId={}, err={}", relativeFilePath, appId, e.toString(), e);
            return "文件写入失败: " + relativeFilePath + ", 错误: " + e.getMessage();
        }
    }

    private static Path resolveSafeTarget(Path codeRoot, Long appId, String relativeFilePath) throws IOException {
        if (appId == null) {
            throw new IllegalArgumentException("appId cannot be null");
        }
        // 统一拒绝绝对路径，避免越权写入
        Path rel = Paths.get(relativeFilePath);
        if (rel.isAbsolute()) {
            throw new IllegalArgumentException("Absolute paths are not allowed: " + relativeFilePath);
        }
        // 工程根
        Path projectRoot = codeRoot.resolve("vue_project_" + appId).normalize();
        // 目标路径（规范化后校验范围）
        Path target = projectRoot.resolve(rel).normalize();

        if (!target.startsWith(projectRoot)) {
            throw new SecurityException("Path traversal detected: " + relativeFilePath);
        }
        // 确保父目录存在
        Files.createDirectories(target.getParent());
        return target;
    }

    // 通过 NIO 实现原子写入
    private boolean writeFileAtomically(Path targetPath, String content) throws IOException {
        // 在同一目录创建临时文件，确保同卷，从而支持 ATOMIC_MOVE
        Path parentDir = targetPath.getParent();
        ThrowUtils.throwIf(parentDir == null, ErrorCode.SYSTEM_ERROR, "Parent dir is null");
        Path tempFile = null;
        try {
            tempFile = Files.createTempFile(parentDir, ".writing-", ".tmp");

            // 写入 UTF-8 文本
            try (FileChannel out = FileChannel.open(tempFile, WRITE, TRUNCATE_EXISTING)) {
                byte[] bytes = content.getBytes(java.nio.charset.StandardCharsets.UTF_8);
                ByteBuffer buf = ByteBuffer.wrap(bytes);
                while (buf.hasRemaining()) {
                    int write = out.write(buf);
                    ThrowUtils.throwIf(write == 0, ErrorCode.SYSTEM_ERROR, "Write failed");
                }
                // 强制落盘（包含元数据）
                out.force(true);
            }
            try {
                Files.move(tempFile, targetPath, ATOMIC_MOVE, REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                // 降级为非原子（同卷失败或文件系统不支持）
                Files.move(tempFile, targetPath, REPLACE_EXISTING);
            }
            return true;
        } catch (IOException ex) {
            log.error("Atomic write failed for: {}", targetPath, ex);
            return false;
        } finally {
            // 兜底清理可能残留的临时文件
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (Exception ignore) {
                }
            }
        }
    }


}
