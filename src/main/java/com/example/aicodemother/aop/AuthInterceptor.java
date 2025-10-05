package com.example.aicodemother.aop;

import com.example.aicodemother.annotation.AuthCheck;
import com.example.aicodemother.exception.BusinessException;
import com.example.aicodemother.exception.ErrorCode;
import com.example.aicodemother.model.entity.User;
import com.example.aicodemother.model.enums.UserRoleEnum;
import com.example.aicodemother.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;


@Aspect
@Component
@RequiredArgsConstructor
public class AuthInterceptor {

    private final UserService userService;

    @Around(value = "@annotation(authCheck)")
    public Object doIntercept(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        // 1、获取所需要的权限
        String mustRole = authCheck.mustRole();
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();

        // 2、获取当前用户的权限
        User loginUser = userService.getLoginUser(request);
        UserRoleEnum mustRoleEnum = UserRoleEnum.getEnumByValue(mustRole);

        // 如果不需要权限，直接执行
        if (mustRoleEnum == null) {
            return joinPoint.proceed();
        }

        // 3、判断用户角色
        UserRoleEnum userRoleEnum = UserRoleEnum.getEnumByValue(loginUser.getUserRole());
        if (userRoleEnum == null) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }

        // 4、检查角色权限
        if (userRoleEnum.getWeight()<mustRoleEnum.getWeight()) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }

        // 5、执行业务逻辑
        return joinPoint.proceed();
    }


}
