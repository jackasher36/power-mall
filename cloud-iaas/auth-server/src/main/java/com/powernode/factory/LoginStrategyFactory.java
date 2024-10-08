package com.powernode.factory;

import com.powernode.strategy.LoginStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 登录策略工厂类
 */
@Component
public class LoginStrategyFactory {

    @Autowired
    private Map<String,LoginStrategy> loginStrategyMap = new HashMap<>();

    /**
     * 根据用户登录类型获取具体的登录策略
     * @param loginType
     * @return
     */
    public LoginStrategy getInstance(String loginType) {
        return loginStrategyMap.get(loginType);
    }
}
