package com.library.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.common.Result;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * JWT认证入口点，处理未认证请求
 * 返回统一的JSON格式错误响应
 */
@Component
public class JwtAuthEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        Result<?> result = Result.error(401, "未登录或Token已过期");
        response.getWriter().write(new ObjectMapper().writeValueAsString(result));
    }
}
