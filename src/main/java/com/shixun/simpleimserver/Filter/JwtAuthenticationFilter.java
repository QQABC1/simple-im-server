package com.shixun.simpleimserver.Filter;

import com.shixun.simpleimserver.common.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil; // 注入您的 JWT 工具类

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // 1. 从 Header 中获取 Token
        String header = request.getHeader("Authorization");

        // 2. 判断 Header 是否有效 (必须以 "Bearer " 开头)
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            // 截取 Token 部分
            String token = header.substring(7);

            try {
                // 3. 解析 Token (根据您的 JwtUtil 实现调整)
                // 假设解析出用户名
                String username = jwtUtil.extractUsername(token);
                // 4. 如果解析成功，且当前上下文没有认证过
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                    // 加载用户信息
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    // 校验 Token 是否过期/合法
                    if (jwtUtil.validateToken(token, userDetails)) {

                        // 5. 构建认证对象 (手动盖章)
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());

                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                        // 6. 放入 SecurityContext (放行)
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                }
            } catch (Exception e) {
                // Token 解析失败，这里可以记录日志，不抛出异常，让 Security 自动处理未认证情况
                logger.error("Token认证失败: " + e.getMessage());
            }
        }

        // 继续过滤器链
        filterChain.doFilter(request, response);
    }
}