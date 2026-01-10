package com.shixun.simpleimserver.config;

import com.shixun.simpleimserver.service.impl.UserDetailsServiceImpl;
import com.shixun.simpleimserver.Filter.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    // 1. 配置密码加密器 (注册时也要用这个 Bean 加密)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 2. 暴露 AuthenticationManager Bean (在 LoginController 里用来验证账号密码)
    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    // 3. 配置认证管理器 (关联我们的 UserDetailsServiceImpl)
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder());
    }

    // 4. 核心安全规则配置
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                // 4.1 开启 CORS (使用下面的 corsConfigurationSource Bean)
                .cors().and()

                // 4.2 关闭 CSRF (前后端分离不需要)
                .csrf().disable()

                // 4.3 禁用 Session (使用 JWT)
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()

                // 4.4 请求权限控制
                .authorizeRequests()
                // === 【Knife4j 放行列表】 ===
                .antMatchers(
                        "/doc.html",              // Knife4j 主页
                        "/webjars/**",            // 静态资源
                        "/swagger-resources/**",  // Swagger 资源
                        "/v3/api-docs",           // 接口定义 Json
                        "/favicon.ico"            // 图标
                ).permitAll()
                // 放行登录、注册接口
                .antMatchers("/api/auth/**").permitAll()
                // 放行静态资源 (文件上传后的访问路径)
                .antMatchers("/files/**", "/static/**").permitAll()
                // 放行 WebSocket 握手地址 (如果是 HTTP 握手阶段)
                .antMatchers("/im/**").permitAll()
                // 其他所有请求必须认证
                .anyRequest().authenticated();

        // 4.5 将 JWT 过滤器添加到 UsernamePasswordAuthenticationFilter 之前
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        // 防止 iframe 嵌入问题 (可选)
        http.headers().frameOptions().disable();
    }

    // 5. CORS 详细配置 ("门卫"逻辑)
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 允许 React 前端地址
        configuration.setAllowedOrigins(Collections.singletonList("http://localhost:3000"));

        // 允许的方法
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // 允许的头信息 (Authorization 很重要)
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With"));

        // 允许携带凭证 (Cookie 等，虽然 JWT 不强制，但 React 有时需要)
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 对所有路径生效
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}