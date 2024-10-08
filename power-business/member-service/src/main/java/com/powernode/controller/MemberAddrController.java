package com.powernode.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.powernode.domain.MemberAddr;
import com.powernode.model.Result;
import com.powernode.service.MemberAddrService;
import com.powernode.util.AuthUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 会员收货地址管理控制层
 */
@Api(tags = "会员收货地址接口管理")
@RequestMapping("p/address")
@RestController
public class MemberAddrController {

    @Autowired
    private MemberAddrService memberAddrService;

    /**
     * 查询会员所有收货地址
     * @return
     */
//    p/address/list
    @ApiOperation("查询会员所有收货地址")
    @GetMapping("list")
    public Result<List<MemberAddr>> loadMemberAddrList() {
        String openId = AuthUtils.getMemberOpenId();
        List<MemberAddr> memberAddrs = memberAddrService.queryMemberAddrListByOpenId(openId);
        return Result.success(memberAddrs);
    }

    /**
     * 新增会员收货地址
     * @param memberAddr    会员收货地址对象
     * @return
     */
    @ApiOperation("新增会员收货地址")
    @PostMapping
    public Result<String> saveMemberAddr(@RequestBody MemberAddr memberAddr) {
        String openId = AuthUtils.getMemberOpenId();
        Boolean saved = memberAddrService.saveMemberAddr(memberAddr,openId);
        return Result.handle(saved);
    }

    /**
     * 查询会员收货地址详情
     * @param addrId    会员地址id
     * @return
     */
//    p/address/addrInfo/12
    @ApiOperation("查询会员收货地址详情")
    @GetMapping("addrInfo/{addrId}")
    public Result<MemberAddr> loadMemberAddrInfo(@PathVariable Long addrId) {
        MemberAddr memberAddr = memberAddrService.getById(addrId);
        return Result.success(memberAddr);
    }

    /**
     * 修改会员收货地址信息
     * @param memberAddr 收货地址对象
     * @return
     */
    @ApiOperation("修改会员收货地址信息")
    @PutMapping
    public Result<String> modifyMemberAddrInfo(@RequestBody MemberAddr memberAddr) {
        String openId = AuthUtils.getMemberOpenId();
        Boolean modified = memberAddrService.modifyMemberAddrInfo(memberAddr,openId);
        return Result.handle(modified);
    }

    /**
     * 删除会员收货地址
     * @param addrId    收货地址id
     * @return
     */
    @ApiOperation("删除会员收货地址")
    @DeleteMapping("deleteAddr/{addrId}")
    public Result<String> removeMemberAddr(@PathVariable Long addrId) {
        String openId = AuthUtils.getMemberOpenId();
        Boolean removed = memberAddrService.removeMemberAddrById(addrId,openId);
        return Result.handle(removed);
    }

    /**
     * 会员设置默认收货地址
     * @param newAddrId 新默认收货地址对象
     * @return
     */
//    p/address/defaultAddr/12
    @ApiOperation("会员设置默认收货地址")
    @PutMapping("defaultAddr/{newAddrId}")
    public Result<String> modifyMemberDefaultAddr(@PathVariable Long newAddrId) {
        String openId = AuthUtils.getMemberOpenId();
        Boolean modified = memberAddrService.modifyMemberDefaultAddr(openId,newAddrId);
        return Result.handle(modified);
    }


    ////////////////////// feign 接口 /////////////////////////////
    @GetMapping("getMemberAddrById")
    public Result<MemberAddr> getMemberAddrById(@RequestParam Long addrId) {
        MemberAddr memberAddr = memberAddrService.getById(addrId);
        return Result.success(memberAddr);
    }

    @GetMapping("getMemberDefaultAddrByOpenId")
    public Result<MemberAddr> getMemberDefaultAddrByOpenId(@RequestParam String openId) {
        MemberAddr memberAddr = memberAddrService.getOne(new LambdaQueryWrapper<MemberAddr>()
                .eq(MemberAddr::getOpenId, openId)
                .eq(MemberAddr::getCommonAddr, 1)
        );
        return Result.success(memberAddr);
    }
}
