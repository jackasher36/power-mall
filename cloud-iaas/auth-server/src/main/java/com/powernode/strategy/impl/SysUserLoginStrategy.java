package com.powernode.strategy.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.powernode.constant.AuthConstants;
import com.powernode.domain.LoginSysUser;
import com.powernode.mapper.LoginSysUserMapper;
import com.powernode.model.SecurityUser;
import com.powernode.strategy.LoginStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * 商城后台管理系统登录策略的具体实现
 */
@Slf4j
@Service(AuthConstants.SYS_USER_LOGIN)
public class SysUserLoginStrategy implements LoginStrategy {

    @Autowired
    private LoginSysUserMapper loginSysUserMapper;

    @Override
    public UserDetails realLogin(String username) {
        System.out.println("进入realLogin" + username);
        // 根据用户名称查询用户对象
        LoginSysUser loginSysUser = loginSysUserMapper.selectOne(new LambdaQueryWrapper<LoginSysUser>()
                .eq(LoginSysUser::getUsername, username)
        );
        /*LoginSysUser loginSysUser = loginSysUserMapper.selectOne(new QueryWrapper<LoginSysUser>()
                .eq("username", username)
        );*/

        System.out.println("loginSysUser: " + loginSysUser);
        if (ObjectUtil.isNotNull(loginSysUser)) {
            // 根据用户标识查询用户的权限集合
            Set<String> perms = loginSysUserMapper.selectPermsByUserId(loginSysUser.getUserId());
            // 创建安全用户对象SecurityUser
            SecurityUser securityUser = new SecurityUser();
            securityUser.setUserId(loginSysUser.getUserId());
            securityUser.setPassword(loginSysUser.getPassword());
            securityUser.setShopId(loginSysUser.getShopId());
            securityUser.setStatus(loginSysUser.getStatus());
            securityUser.setLoginType(AuthConstants.SYS_USER_LOGIN);
            // 判断用户权限是否有值
            if (CollectionUtil.isNotEmpty(perms) && perms.size() != 0) {
                securityUser.setPerms(perms);
            }
            return securityUser;
        }

        return null;
    }
}
