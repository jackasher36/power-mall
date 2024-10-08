package com.powernode.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.powernode.config.AliyunDxConfig;
import com.powernode.constant.MemberConstants;
import com.powernode.domain.Member;
import com.powernode.ex.handler.BusinessException;
import com.powernode.mapper.MemberMapper;
import com.powernode.service.SendService;
import com.powernode.util.AuthUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;

/**
 *
 */
@Service
public class SendServiceImpl implements SendService {
    @Autowired
    private AliyunDxConfig aliyunDxConfig;

    @Autowired
    private MemberMapper memberMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Override
    public void sendPhoneMsg(Map<String, Object> map) {
        // 准备配置对象
        com.aliyun.teaopenapi.models.Config config = new com.aliyun.teaopenapi.models.Config()
                // 必填，请确保代码运行环境设置了环境变量 ALIBABA_CLOUD_ACCESS_KEY_ID。
                .setAccessKeyId(aliyunDxConfig.getAccessKeyID())
                // 必填，请确保代码运行环境设置了环境变量 ALIBABA_CLOUD_ACCESS_KEY_SECRET。
                .setAccessKeySecret(aliyunDxConfig.getAccessKeySecret());
        // Endpoint 请参考 https://api.aliyun.com/product/Dysmsapi
        config.endpoint = aliyunDxConfig.getEndpoint();
        try {
            // 创建客户端对象
            com.aliyun.dysmsapi20170525.Client client = new com.aliyun.dysmsapi20170525.Client(config);
            // 获取手机号码
            String phonenum = (String) map.get("phonenum");
            // 生成一个随机数字
            String randomNumber = RandomUtil.randomNumbers(4);
            // 将生成的随机数字存放到redis中
            stringRedisTemplate.opsForValue().set(MemberConstants.MSG_PHONE_PREFIX+phonenum, randomNumber, Duration.ofMinutes(30));
            // 创建模版参数
            String templateParam = "{\"code\":\""+randomNumber+"\"}";
            // 创建请求参数对象
            com.aliyun.dysmsapi20170525.models.SendSmsRequest sendSmsRequest = new com.aliyun.dysmsapi20170525.models.SendSmsRequest()
                    .setPhoneNumbers(phonenum)
                    .setSignName(aliyunDxConfig.getSignName())
                    .setTemplateCode(aliyunDxConfig.getTemplateCode())
                    .setTemplateParam(templateParam);
            // 发送请求
            client.sendSmsWithOptions(sendSmsRequest, new com.aliyun.teautil.models.RuntimeOptions());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Boolean saveMsgPhone(Map<String, Object> map) {
        // 获取会员输入的短信验证码
        String code = (String) map.get("code");
        // 获取会员手机号码
        String phonenum = (String) map.get("phonenum");
        // 从redis中获取当前手机号码对应验证码
        String redisCode = stringRedisTemplate.opsForValue().get(MemberConstants.MSG_PHONE_PREFIX + phonenum);
        // 判断验证码是否正确
        if (!code.equals(redisCode)) {
            throw new BusinessException("请输入正确的短信验证码");
        }
        // 将会员手机号码更新到会员信息中
        Member member = new Member();
        member.setUserMobile(phonenum);
        // 获取当前会员的openId
        String openId = AuthUtils.getMemberOpenId();
        return memberMapper.update(member,new LambdaUpdateWrapper<Member>()
                .eq(Member::getOpenId,openId)
        )>0;
    }
}
