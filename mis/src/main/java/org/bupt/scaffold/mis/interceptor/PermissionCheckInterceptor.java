package org.bupt.scaffold.mis.interceptor;

import org.bupt.common.constant.ErrorConsts;
import org.bupt.common.util.token.Identity;
import org.bupt.scaffold.mis.annotation.RequiredPermission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 能力权限拦截器 1:1
 */
public class PermissionCheckInterceptor extends HandlerInterceptorAdapter {

    private static final Logger logger = LoggerFactory.getLogger(PermissionCheckInterceptor.class);

    // 在调用方法之前执行拦截
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        logger.info("进入PermissionCheckInterceptor");

        // 将handler强转为HandlerMethod, 前面已经证实这个handler就是HandlerMethod
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        // 从方法处理器中获取出要调用的方法
        Method method = handlerMethod.getMethod();
        // 获取出方法上的Access注解
        RequiredPermission permissionCheck = method.getAnnotation(RequiredPermission.class);
        if (permissionCheck == null) {
            // 如果注解为null, 说明不需要拦截, 直接放过
            return true;
        }

        String permission = permissionCheck.permission();
        if (!"".equals(permission)) {

            logger.info("该方法对应能力要求的权限是{}", permission);


            // 这里我为了方便是直接参数传入权限, 在实际操作中应该是从参数中获取用户Id
            // 到数据库权限表中查询用户拥有的权限集合, 与set集合中的权限进行对比完成权限校验

            String[] userPermisssions = ((Identity) request.getSession().getAttribute("identity")).getPermission().split(",");
            Set<String> userPermissionSet = new HashSet<>();
            userPermissionSet.addAll(Arrays.asList(userPermisssions));

            logger.info("用户的能力权限是 {}", userPermissionSet.toString());

            if (!userPermissionSet.isEmpty()) {
                if (userPermissionSet.contains(permission)) {
                    // 校验通过返回true, 否则拦截请求
                    logger.info("权限校验通过");
                    return true;
                }
            }
        }

        logger.info("权限拒绝");
        response.sendRedirect("/api/error/oauth/" + ErrorConsts.OAUTH_CODE_PERMISSION_DENIED);
        return false;
    }
}
