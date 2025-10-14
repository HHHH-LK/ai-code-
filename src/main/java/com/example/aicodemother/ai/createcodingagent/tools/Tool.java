package com.example.aicodemother.ai.createcodingagent.tools;

import java.util.Map;

public interface Tool extends Runnable {

    void run();

    default String name() {
        return this.getClass().getSimpleName();
    }

    default String description() {
        return "";
    }

    default String execute(Map<String, Object> args) throws Exception {
        run();
        return "OK";
    }

}
