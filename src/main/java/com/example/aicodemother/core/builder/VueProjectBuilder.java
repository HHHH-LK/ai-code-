package com.example.aicodemother.core.builder;

import cn.hutool.core.util.RuntimeUtil;
import com.example.aicodemother.config.TheadExecutorFactory;
import com.example.aicodemother.exception.ThrowUtils;
import com.example.aicodemother.model.enums.TheadExecutorTypeEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.threads.ThreadPoolExecutor;
import org.springframework.stereotype.Component;

import java.io.File;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @program: ai-code-mother
 * @description: vue项目的构建工具
 * @author: lk_hhh
 * @create: 2025-10-15 21:47
 **/

@Slf4j
@Component
public class VueProjectBuilder {

    @Resource
    private TheadExecutorFactory theadExecutorFactory;

    public void buildProjectAsync(String projectPath) {
        theadExecutorFactory.getTheadExecutor(TheadExecutorTypeEnum.VUE_PROJECT_EXECUTOR).execute(() -> {
            try {
                buildProject(projectPath);
            } catch (Exception e) {
                log.error("构建Vue项目失败，出现异常{}", e.getMessage(), e);
            }
        });
    }


    public boolean buildProject(String projectPath) {
        File projectDir = new File(projectPath);
        if (!projectDir.exists() || !projectDir.isDirectory()) {
            log.error("项目目录不存在: {}", projectDir.getAbsolutePath());
            return false;
        }
        File packageJsonPath = new File(projectDir, "package.json");
        if (!packageJsonPath.exists()) {
            log.error("package.json 文件不存在: {}", packageJsonPath.getAbsolutePath());
            return false;
        }
        log.info("开始构建Vue项目");
        //执行 npm install
        boolean b = executeNpmInstall(projectDir);
        if (!b) {
            log.error("npm install 执行失败");
            return false;
        }
        //执行 npm run build
        boolean b1 = executeNpmBuild(projectDir);
        if (!b1) {
            log.error("npm run build 执行失败");
            return false;
        }

        File distDir = new File(projectDir, "dist");
        if (!distDir.exists() || !distDir.isDirectory()) {
            log.error("dist 目录不存在: {}", distDir.getAbsolutePath());
            return false;
        }

        return true;

    }


    /**
     * 执行 npm install 命令
     */
    private boolean executeNpmInstall(File projectDir) {
        log.info("执行 npm install...");
        String npmCommand = String.format("%s install", buildCommand());
        return executeCommand(projectDir, npmCommand, 300); // 5分钟超时
    }

    /**
     * 执行 npm run build 命令
     */
    private boolean executeNpmBuild(File projectDir) {
        log.info("执行 npm run build...");
        String npmCommand = String.format("%s run build", buildCommand());
        return executeCommand(projectDir, npmCommand, 180); // 3分钟超时
    }

    private boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("windows");
    }

    private String buildCommand() {
        if (isWindows()) {
            return "npm" + ".cmd";
        }
        return "npm";
    }


    /**
     * 执行命令
     *
     * @param workingDir     工作目录
     * @param command        命令字符串
     * @param timeoutSeconds 超时时间（秒）
     * @return 是否执行成功
     */
    private boolean executeCommand(File workingDir, String command, int timeoutSeconds) {
        try {
            log.info("在目录 {} 中执行命令: {}", workingDir.getAbsolutePath(), command);
            Process process = RuntimeUtil.exec(
                    null, workingDir, command.split("\\s+") // 命令分割为数组
            );
            // 等待进程完成，设置超时
            boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
            if (!finished) {
                log.error("命令执行超时（{}秒），强制终止进程", timeoutSeconds);
                process.destroyForcibly();
                return false;
            }
            int exitCode = process.exitValue();
            if (exitCode == 0) {
                log.info("命令执行成功: {}", command);
                return true;
            } else {
                log.error("命令执行失败，退出码: {}", exitCode);
                return false;
            }
        } catch (Exception e) {
            log.error("执行命令失败: {}, 错误信息: {}", command, e.getMessage());
            return false;
        }
    }


}