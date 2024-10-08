package com.powernode.config;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.powernode.constant.AuthConstants;
import com.powernode.constant.BusinessEnum;
import com.powernode.constant.HttpConstants;
import com.powernode.impl.UserDetailsServiceImpl;
import com.powernode.model.LoginResult;
import com.powernode.model.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import java.io.PrintWriter;
import java.time.Duration;
import java.util.UUID;

/**
 * Security安全框架配置类
 */
@Configuration
public class AuthSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 设置Security安全框架走自己的认证流程
     * @param auth
     * @throws Exception
     */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        System.out.println("进入configure方法");
        // 关闭跨站请求伪造
        http.cors().disable();
        // 关闭跨域请求
        http.csrf().disable();
        // 关闭session使用策略
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        // 配置登录信息
        http.formLogin()
                .loginProcessingUrl(AuthConstants.LOGIN_URL)// 设置登录URL
                .successHandler(authenticationSuccessHandler())   // 设置登录成功处理器
                .failureHandler(authenticationFailureHandler());  // 调协登录失败处理器

        // 配置登出信息
        http.logout()
                .logoutUrl(AuthConstants.LOGOUT_URL)// 设置登出URL
                .logoutSuccessHandler(logoutSuccessHandler());// 设置登出成功处理器

        // 要求所有请求都需要进行身份的认证
        http.authorizeHttpRequests().anyRequest().authenticated();
    }

    /**
     * 登录成功处理器
     * @return
     */
    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return (request, response, authentication) -> {
            // 设置响应头信息
            response.setContentType(HttpConstants.APPLICATION_JSON);
            response.setCharacterEncoding(HttpConstants.UTF_8);

            // 使用UUID来当作TOKEN
            String token = UUID.randomUUID().toString();
            // 从security框架中获取认证用户对象(SecurityUser)并转换为json格式的字符串
            String userJsonStr = JSONObject.toJSONString(authentication.getPrincipal());
            // 将token当作key，认证用户对象的json格式的字符串当前value存放到redis中
            stringRedisTemplate.opsForValue().set(AuthConstants.LOGIN_TOKEN_PREFIX+token,userJsonStr, Duration.ofSeconds(AuthConstants.TOKEN_TIME));

            // 封装一个登录统一结果对象
            LoginResult loginResult = new LoginResult(token,AuthConstants.TOKEN_TIME);
            // 创建一个响应结果对象
            Result<Object> result = Result.success(loginResult);

            // 返回结果
            ObjectMapper objectMapper = new ObjectMapper();
            String s = objectMapper.writeValueAsString(result);
            PrintWriter writer = response.getWriter();
            writer.write(s);
            writer.flush();
            writer.close();
        };
    }

    /**
     * 登录失败处理器
     * @return
     */
    @Bean
    public AuthenticationFailureHandler authenticationFailureHandler() {
        return (request, response, exception) -> {
            // 设置响应头信息
            response.setContentType(HttpConstants.APPLICATION_JSON);
            response.setCharacterEncoding(HttpConstants.UTF_8);

            // 创建统一响应结果对象
            Result<Object> result = new Result<>();
            result.setCode(BusinessEnum.OPERATION_FAIL.getCode());
            if (exception instanceof BadCredentialsException) {
                result.setMsg("用户名或密码有误");
            } else if (exception instanceof UsernameNotFoundException) {
                result.setMsg("用户不存在");
            } else if (exception instanceof AccountExpiredException) {
                result.setMsg("帐号异常，请联系管理员");
            } else if (exception instanceof AccountStatusException) {
                result.setMsg("帐号异常，请联系管理员");
            } else if (exception instanceof InternalAuthenticationServiceException) {
                result.setMsg(exception.getMessage());
            } else {
                result.setMsg(BusinessEnum.OPERATION_FAIL.getDesc());
            }

            // 返回结果
            ObjectMapper objectMapper = new ObjectMapper();
            String s = objectMapper.writeValueAsString(result);
            PrintWriter writer = response.getWriter();
            writer.write(s);
            writer.flush();
            writer.close();

        };
    }

    /**
     * 登出成功处理器
     * @return
     */
    @Bean
    public LogoutSuccessHandler logoutSuccessHandler() {
        return (request, response, authentication) -> {
            // 设置响应头信息
            response.setContentType(HttpConstants.APPLICATION_JSON);
            response.setCharacterEncoding(HttpConstants.UTF_8);

            // 从请求头中获取token
            String authorization = request.getHeader(AuthConstants.AUTHORIZATION);
            String token = authorization.replaceFirst(AuthConstants.BEARER, "");
            // 将当前token从redis中删除
            stringRedisTemplate.delete(AuthConstants.LOGIN_TOKEN_PREFIX+token);

            // 创建统一响应结果对象
            Result<Object> result = Result.success(null);
            // 返回结果
            ObjectMapper objectMapper = new ObjectMapper();
            String s = objectMapper.writeValueAsString(result);
            PrintWriter writer = response.getWriter();
            writer.write(s);
            writer.flush();
            writer.close();
        };
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
