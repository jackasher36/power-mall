package com.powernode.feign;

import com.powernode.domain.MemberAddr;
import com.powernode.feign.sentinel.OrderMemberFeignSentinel;
import com.powernode.model.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 订单业务模块调用会员业务模块feign接口
 */
@FeignClient(value = "member-service",fallback = OrderMemberFeignSentinel.class)
public interface OrderMemberFeign {

    @GetMapping("p/address/getMemberAddrById")
    public Result<MemberAddr> getMemberAddrById(@RequestParam Long addrId);

    @GetMapping("p/address/getMemberDefaultAddrByOpenId")
    public Result<MemberAddr> getMemberDefaultAddrByOpenId(@RequestParam String openId);

    @GetMapping("admin/user/getNickNameByOpenId")
    public Result<String> getNickNameByOpenId(@RequestParam String openId);
}
