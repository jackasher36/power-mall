package com.powernode;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 系统管理模块启动类
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableCaching  // 开启注解式缓存（默认使用的缓存中间件是redis）
public class ManagerServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ManagerServiceApplication.class,args);
    }

    /**
     * Spring Security框架中的密码加密器
     * @return
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
