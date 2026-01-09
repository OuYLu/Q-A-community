package com.community.common.aop;

import com.community.common.annotation.RequirePermission;
import com.community.common.context.CurrentUserContext;
import com.community.common.exception.BizException;
import com.community.common.result.ErrorCode;
import com.community.service.PermissionService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class PermissionAspect {

    @Autowired
    private PermissionService permissionService;

    @Around("@annotation(requirePermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint, RequirePermission requirePermission) throws Throwable {
        CurrentUserContext.UserInfo userInfo = CurrentUserContext.get();
        if (userInfo == null) {
            throw new BizException(ErrorCode.UNAUTHORIZED.getCode(), "未登录或登录已过期");
        }
        permissionService.require(userInfo.getUserId(), requirePermission.value());
        return joinPoint.proceed();
    }
}
