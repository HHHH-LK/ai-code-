package com.example.aicodemother.model.enums;

import lombok.Getter;

@Getter
public enum TheadExecutorTypeEnum {

    VUE_PROJECT_EXECUTOR("vue_project_thead_executor");

    private final String value;

    TheadExecutorTypeEnum(String value) {
        this.value = value;
    }

    public static TheadExecutorTypeEnum getEnumByValue(String value) {
        for (TheadExecutorTypeEnum typeEnum : values()) {
            if (typeEnum.getValue().equals(value)) {
                return typeEnum;
            }
        }
        return null;
    }


}
