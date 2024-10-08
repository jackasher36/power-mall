package com.powernode.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.powernode.constant.ProductConstants;
import com.powernode.domain.ProdTag;
import com.powernode.mapper.ProdTagMapper;
import com.powernode.service.ProdTagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Service
@CacheConfig(cacheNames = "com.powernode.service.impl.ProdTagServiceImpl")
public class ProdTagServiceImpl extends ServiceImpl<ProdTagMapper, ProdTag> implements ProdTagService{

    @Autowired
    private ProdTagMapper prodTagMapper;


    @Override
    @Caching(evict = {
        @CacheEvict(key = ProductConstants.PROD_TAG_NORMAL_KEY),
        @CacheEvict(key = ProductConstants.WX_PROD_TAG)
    })
    public Boolean saveProdTag(ProdTag prodTag) {
        prodTag.setCreateTime(new Date());
        prodTag.setUpdateTime(new Date());
        return prodTagMapper.insert(prodTag)>0;
    }

    @Override
    @Caching(evict = {
            @CacheEvict(key = ProductConstants.PROD_TAG_NORMAL_KEY),
            @CacheEvict(key = ProductConstants.WX_PROD_TAG)
    })
    public Boolean modifyProdTag(ProdTag prodTag) {
        prodTag.setUpdateTime(new Date());
        return prodTagMapper.updateById(prodTag)>0;
    }

    @Override
    @Caching(evict = {
            @CacheEvict(key = ProductConstants.PROD_TAG_NORMAL_KEY),
            @CacheEvict(key = ProductConstants.WX_PROD_TAG)
    })
    public boolean removeById(Serializable id) {
        return super.removeById(id);
    }

    @Override
    @Cacheable(key = ProductConstants.PROD_TAG_NORMAL_KEY)
    public List<ProdTag> queryProdTagList() {
        return prodTagMapper.selectList(new LambdaQueryWrapper<ProdTag>()
                .eq(ProdTag::getStatus,1)
                .orderByDesc(ProdTag::getSeq)
        );
    }

    @Override
    @Cacheable(key = ProductConstants.WX_PROD_TAG)
    public List<ProdTag> queryWxProdTagList() {
        return prodTagMapper.selectList(new LambdaQueryWrapper<ProdTag>()
                .eq(ProdTag::getStatus,1)
                .orderByDesc(ProdTag::getSeq)
        );
    }
}
