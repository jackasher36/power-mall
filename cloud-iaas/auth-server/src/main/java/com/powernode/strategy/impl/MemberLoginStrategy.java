package com.powernode.strategy.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.powernode.config.WxParamConfig;
import com.powernode.constant.AuthConstants;
import com.powernode.domain.LoginMember;
import com.powernode.mapper.LoginMemberMapper;
import com.powernode.model.SecurityUser;
import com.powernode.strategy.LoginStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

/**
 * 商城购物系统登录具体实现策略
 */
@Service(AuthConstants.MEMBER_LOGIN)
public class MemberLoginStrategy implements LoginStrategy {

    @Autowired
    private WxParamConfig wxParamConfig;

    @Autowired
    private LoginMemberMapper loginMemberMapper;

    @Override
    public UserDetails realLogin(String username) {

        // 调用微信接口服务器中的：登录凭证校验接口（appid,appsecret,code）
//        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code";
        // 获取登录凭证校验接口url
        String realUrl = String.format(wxParamConfig.getUrl(), wxParamConfig.getAppid(), wxParamConfig.getSecret(), username);
        // 使用get方法调用登录凭证校验接口
        String jsonStr = HttpUtil.get(realUrl);
        // 判断响应是否有值
        if (!StringUtils.hasText(jsonStr)) {
            throw new InternalAuthenticationServiceException("登录异常，请重试");
        }
        // 使用fastjson将登录凭证校验接口响应的json格式的字符串转换为json对象
        JSONObject jsonObject = JSONObject.parseObject(jsonStr);
        // 获取openid
        String openid = jsonObject.getString("openid");
        // 判断是否有值
        if (!StringUtils.hasText(openid)) {
            throw new InternalAuthenticationServiceException("登录异常，请重试");
        }
        // 根据会员openid查询会员对象
        LoginMember loginMember = loginMemberMapper.selectOne(new LambdaQueryWrapper<LoginMember>()
                .eq(LoginMember::getOpenId, openid)
        );
        // 判断会员是否存在
        if (ObjectUtil.isNull(loginMember)) {
            // 会员不存在
            // 注册：创建会员对象到我们的微信小程序的用户体系内
            loginMember = registerMember(openid);
        }
        // 判断会员帐号的状态
        if (!loginMember.getStatus().equals(1)) {
            throw new InternalAuthenticationServiceException("帐号异常，请联系平台工作人员");
        }
        // 说明会员帐号的状态正常
        // 会员存在：返回security框架能够认识的安全用户对象SecurityUser
        SecurityUser securityUser = new SecurityUser();
        securityUser.setUserId(Long.valueOf(loginMember.getId()));
        securityUser.setLoginType(AuthConstants.MEMBER_LOGIN);
        securityUser.setUsername(openid);
        securityUser.setStatus(loginMember.getStatus());
        securityUser.setPassword(wxParamConfig.getPwd());
        securityUser.setOpenid(openid);

        return securityUser;
    }

    private LoginMember registerMember(String openid) {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();
        String ip = request.getRemoteAddr();
        LoginMember loginMember = new LoginMember();
        loginMember.setOpenId(openid);
        loginMember.setStatus(1);
        loginMember.setCreateTime(new Date());
        loginMember.setUpdateTime(new Date());
        loginMember.setUserLasttime(new Date());
        loginMember.setUserRegip(ip);
        loginMember.setUserLastip(ip);
        // 如果有积分业务
        loginMember.setScore(0);

        // 新增会员
        loginMemberMapper.insert(loginMember);
        return loginMember;
    }


    /*public static void main(String[] args) {
        System.out.println(new BCryptPasswordEncoder().encode("WECHAT"));
    }*/
}
