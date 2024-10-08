package com.powernode.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.powernode.domain.MemberAddr;
import com.powernode.mapper.MemberAddrMapper;
import com.powernode.service.MemberAddrService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
@Service
@CacheConfig(cacheNames = "com.powernode.service.impl.MemberAddrServiceImpl")
public class MemberAddrServiceImpl extends ServiceImpl<MemberAddrMapper, MemberAddr> implements MemberAddrService{

    @Autowired
    private MemberAddrMapper memberAddrMapper;

    @Override
    @Cacheable(key = "#openId")
    public List<MemberAddr> queryMemberAddrListByOpenId(String openId) {
        return memberAddrMapper.selectList(new LambdaQueryWrapper<MemberAddr>()
                        .eq(MemberAddr::getOpenId,openId)
                .eq(MemberAddr::getStatus,1)
                .orderByDesc(MemberAddr::getCommonAddr,MemberAddr::getCreateTime)
        );
    }

    /**
     * 会员收货地址业务：
     * 1.会员必须得有一个默认收货地址
     *  如果会员新增的第1个收货地址应该为默认收货地址
     * @param memberAddr
     * @param openId
     * @return
     */
    @Override
    @CacheEvict(key = "#openId")
    public Boolean saveMemberAddr(MemberAddr memberAddr,String openId) {
        // 补充收货地址信息
        memberAddr.setCommonAddr(0);
        memberAddr.setStatus(1);
        memberAddr.setCreateTime(new Date());
        memberAddr.setUpdateTime(new Date());
        memberAddr.setOpenId(openId);
        // 根据会员openId查询会员收货地址数量
        Long count = memberAddrMapper.selectCount(new LambdaQueryWrapper<MemberAddr>()
                .eq(MemberAddr::getOpenId, openId)
        );
        // 判断会员是否有收货地址
        if (0 == count) {
            // 说明：当前会员新增的收货地址为第1个即默认收货地址
            memberAddr.setCommonAddr(1);
        }
        return memberAddrMapper.insert(memberAddr)>0;
    }

    @Override
    @CacheEvict(key = "#openId")
    public Boolean modifyMemberAddrInfo(MemberAddr memberAddr, String openId) {
        memberAddr.setUpdateTime(new Date());
        return memberAddrMapper.updateById(memberAddr)>0;
    }

    @Override
    @CacheEvict(key = "#openId")
    @Transactional(rollbackFor = Exception.class)
    public Boolean removeMemberAddrById(Long addrId, String openId) {
        // 根据收货地址id查询收货地址对象
        MemberAddr memberAddr = memberAddrMapper.selectById(addrId);
        // 判断是否为默认收货地址
        if (memberAddr.getCommonAddr().equals(1)) {
            // 说明：当前删除的收货地址是会员默认收货地址，重新获取一个新的地址（最近刚刚新增的）作为默认收货地址
            // 根据会员openId查询会员非默认收货地址
            List<MemberAddr> memberAddrs = memberAddrMapper.selectList(new LambdaQueryWrapper<MemberAddr>()
                    .eq(MemberAddr::getOpenId, openId)
                    .eq(MemberAddr::getCommonAddr, 0)
                    .orderByDesc(MemberAddr::getCreateTime)
            );
            // 判断非默认收货地址是否有值
            if (CollectionUtil.isNotEmpty(memberAddrs) && memberAddrs.size() != 0) {
                // 说明：会员有非默认收货地址 -> 获取第1个地址并设置为新的默认收货地址
                MemberAddr newDefaultMemberAddr = memberAddrs.get(0);
                newDefaultMemberAddr.setCommonAddr(1);
                newDefaultMemberAddr.setUpdateTime(new Date());
                memberAddrMapper.updateById(newDefaultMemberAddr);
            }
        }
        // 说明：当前删除的收货地址非默认收货地址
        return memberAddrMapper.deleteById(addrId)>0;
    }

    @Override
    @CacheEvict(key = "#openId")
    @Transactional(rollbackFor = Exception.class)
    public Boolean modifyMemberDefaultAddr(String openId, Long newAddrId) {
        // 根据收货地址标识查询收货地址对象
        MemberAddr newDefaultMemberAddr = memberAddrMapper.selectById(newAddrId);
        // 判断新的默认收货地址是否为原有的默认收货地址
        if (newDefaultMemberAddr.getCommonAddr().equals(1)) {
            // 是：结束
            return true;
        }
        // 不是：将之前的默认收货地址修改为非默认，并更新当前新的默认收货地址
        // 将会员原有的默认收货地址设置为非默认
        MemberAddr oldDefaultMemberAddr = new MemberAddr();
        oldDefaultMemberAddr.setCommonAddr(0);
        oldDefaultMemberAddr.setUpdateTime(new Date());
        memberAddrMapper.update(oldDefaultMemberAddr, new LambdaUpdateWrapper<MemberAddr>()
                .eq(MemberAddr::getOpenId,openId)
        );

        // 将当前收货地址设置的新的默认收货地址
        newDefaultMemberAddr.setCommonAddr(1);
        newDefaultMemberAddr.setUpdateTime(new Date());

        return memberAddrMapper.updateById(newDefaultMemberAddr)>0;
    }
}
