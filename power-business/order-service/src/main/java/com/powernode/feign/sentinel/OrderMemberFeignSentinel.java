package com.powernode.feign.sentinel;

import com.powernode.domain.MemberAddr;
import com.powernode.feign.OrderMemberFeign;
import com.powernode.model.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
@Slf4j
public class OrderMemberFeignSentinel implements OrderMemberFeign {
    @Override
    public Result<MemberAddr> getMemberAddrById(Long addrId) {
        log.error("远程接口调用失败：根据收货地址标识查询收货地址信息");
        return null;
    }

    @Override
    public Result<String> getNickNameByOpenId(String openId) {
        log.error("远程接口调用失败：根据会员openid查询会员昵称");
        return null;
    }

    @Override
    public Result<MemberAddr> getMemberDefaultAddrByOpenId(String openId) {
        log.error("远程接口调用失败：根据会员openId查询会员默认收货地址");
        return null;
    }
}
