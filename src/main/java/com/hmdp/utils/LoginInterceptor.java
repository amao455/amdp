package com.hmdp.utils;


import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * ClassName: LoginInterceptor
 * Description:
 *
 * @author MQW
 * @date 2023/3/28 19:26
 */

// TODO 拦截器是在spring容器初始化之前执行的（有待查证）
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 判断是否需要拦截（ThreadLocal中是否有用户）
        if (UserHolder.getUser() == null) {
            // 通过ThreadLocal来判断用户是否登录
            // 没有，需要拦截，设置状态码
            response.setStatus(401);
        }

        // 有用户，则放行
        return true;
    }
}
