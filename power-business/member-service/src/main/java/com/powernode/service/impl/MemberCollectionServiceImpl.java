package com.powernode.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.powernode.constant.BusinessEnum;
import com.powernode.domain.MemberCollection;
import com.powernode.domain.Prod;
import com.powernode.ex.handler.BusinessException;
import com.powernode.feign.MemberProdFeign;
import com.powernode.mapper.MemberCollectionMapper;
import com.powernode.model.Result;
import com.powernode.service.MemberCollectionService;
import com.powernode.util.AuthUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MemberCollectionServiceImpl extends ServiceImpl<MemberCollectionMapper, MemberCollection> implements MemberCollectionService{

    @Autowired
    private MemberCollectionMapper memberCollectionMapper;

    @Autowired
    private MemberProdFeign memberProdFeign;

    @Override
    public Long queryMemberCollectionProdCount() {
        // 获取会员openid
        String openId = AuthUtils.getMemberOpenId();
        // 根据会员openid查询会员收藏商品的数量
        Long count = memberCollectionMapper.selectCount(new LambdaQueryWrapper<MemberCollection>()
                .eq(MemberCollection::getOpenId, openId)
        );
        return count;
    }

    @Override
    public Page<Prod> queryMemberCollectionProdPageByOpenId(String openId, Long current, Long size) {
        // 创建商品分页对象
        Page<Prod> prodPage = new Page<>(current,size);
        // 创建会员与商品收藏关系分页对象
        Page<MemberCollection> memberCollectionPage = new Page<>(current,size);
        // 根据会员openId分页查询会员与商品的收藏关系记录
        memberCollectionPage = memberCollectionMapper.selectPage(memberCollectionPage,new LambdaQueryWrapper<MemberCollection>()
                .eq(MemberCollection::getOpenId, openId)
                .orderByDesc(MemberCollection::getCreateTime)
        );
        // 从会员与商品收藏关系分页对象中获取收藏记录
        List<MemberCollection> memberCollectionList = memberCollectionPage.getRecords();
        // 判断是否有值
        if (CollectionUtils.isEmpty(memberCollectionList) || memberCollectionList.size() == 0) {
            return prodPage;
        }
        // 从会员与商品收藏关系集合中获取收藏商品的id集合
        List<Long> prodIdList = memberCollectionList.stream().map(MemberCollection::getProdId).collect(Collectors.toList());
        // 远程调用：根据商品id集合查询商品对象集合
        Result<List<Prod>> result = memberProdFeign.getProdListByIds(prodIdList);
        if (BusinessEnum.OPERATION_FAIL.getCode().equals(result.getCode())) {
            throw new BusinessException("远程调用：根据商品id集合查询商品对象集合 失败");
        }
        // 获取数据
        List<Prod> prodList = result.getData();
        // 将商品对象集合赋值给商品分页对象
        prodPage.setRecords(prodList);
        prodPage.setTotal(memberCollectionPage.getTotal());
        prodPage.setPages(memberCollectionPage.getPages());
        return prodPage;
    }

    @Override
    public Boolean addOrCancelMemberCollection(String openId, Long prodId) {
        // 根据会员openId和商品id查询收藏记录
        MemberCollection memberCollection = memberCollectionMapper.selectOne(new LambdaQueryWrapper<MemberCollection>()
                .eq(MemberCollection::getOpenId, openId)
                .eq(MemberCollection::getProdId, prodId)
        );
        // 判断收藏记录是否存在
        if (ObjectUtil.isNull(memberCollection)) {
            // 为空，说明当前商品没有被收藏 -> 将当前商品添加到收藏记录
            memberCollection = new MemberCollection();
            memberCollection.setCreateTime(new Date());
            memberCollection.setOpenId(openId);
            memberCollection.setProdId(prodId);
            return memberCollectionMapper.insert(memberCollection)>0;
        }

        // 不为空，说明当前商品已被会员收藏 -> 将当前商品取消收藏记录
        return memberCollectionMapper.deleteById(memberCollection.getId())>0;
    }
}
