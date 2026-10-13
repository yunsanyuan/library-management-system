package com.library.security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

/**
 * JWT认证过滤器
 * 从请求头中提取Token，校验并设置Spring Security上下文
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = getTokenFromRequest(request);

        if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
            Long userId = jwtTokenProvider.getUserIdFromToken(token);
            String username = jwtTokenProvider.getUsernameFromToken(token);
            String role = jwtTokenProvider.getRoleFromToken(token);

            // 构造认证对象，角色加上ROLE_前缀
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userId, null,
                            Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
                    );

            // 将用户信息存入Security上下文
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 将用户信息存入请求属性，方便Controller获取
            request.setAttribute("userId", userId);
            request.setAttribute("username", username);
            request.setAttribute("role", role);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 从请求头Authorization中提取Bearer Token
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
