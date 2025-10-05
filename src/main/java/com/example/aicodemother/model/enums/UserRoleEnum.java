package com.example.aicodemother.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

@Getter
public enum UserRoleEnum {
    USER("用户", "user", 1),       // 普通用户，权重 1
    ADMIN("管理员", "admin", 3),   // 管理员，权重 3
    MODERATOR("版主", "moderator", 2), // 版主，权重 2
    GUEST("访客", "guest", 0);     // 访客，权重 0

    // 获取角色文本
    private final String text;     // 显示名称
    // 获取角色的值
    private final String value;    // 角色值
    // 获取角色权重
    private final int weight;      // 权重，表示权限级别

    // 枚举构造方法
    UserRoleEnum(String text, String value, int weight) {
        this.text = text;
        this.value = value;
        this.weight = weight;
    }

    // 根据角色值获取对应的枚举
    public static UserRoleEnum getEnumByValue(String value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (UserRoleEnum anEnum : UserRoleEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }

    // 判断当前角色是否具有某个权限
    public boolean hasPermission(UserRoleEnum requiredRole) {
        return this.weight >= requiredRole.getWeight();
    }
}
