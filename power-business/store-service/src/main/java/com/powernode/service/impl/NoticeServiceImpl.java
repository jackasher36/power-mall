package com.powernode.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.powernode.constant.StoreConstants;
import com.powernode.domain.Notice;
import com.powernode.mapper.NoticeMapper;
import com.powernode.service.NoticeService;
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
@CacheConfig(cacheNames = "com.powernode.service.impl.NoticeServiceImpl")
public class NoticeServiceImpl extends ServiceImpl<NoticeMapper, Notice> implements NoticeService{

    @Autowired
    private NoticeMapper noticeMapper;

    @Override
    @Caching(evict = {
        @CacheEvict(key = StoreConstants.WX_TOP_NOTICE),
        @CacheEvict(key = StoreConstants.WX_ALL_NOTICE)
    })
    public Boolean saveNotice(Notice notice) {
        notice.setShopId(1L);
        notice.setCreateTime(new Date());
        notice.setUpdateTime(new Date());
        return noticeMapper.insert(notice)>0;
    }

    @Override
    @Caching(evict = {
            @CacheEvict(key = StoreConstants.WX_TOP_NOTICE),
            @CacheEvict(key = StoreConstants.WX_ALL_NOTICE)
    })
    public Boolean modifyNotice(Notice notice) {
        notice.setUpdateTime(new Date());
        return noticeMapper.updateById(notice)>0;
    }

    @Override
    @Caching(evict = {
            @CacheEvict(key = StoreConstants.WX_TOP_NOTICE),
            @CacheEvict(key = StoreConstants.WX_ALL_NOTICE)
    })
    public boolean removeById(Serializable id) {
        return super.removeById(id);
    }

    @Override
    @Cacheable(key = StoreConstants.WX_TOP_NOTICE)
    public List<Notice> queryWxTopNoticeList() {
        return noticeMapper.selectList(new LambdaQueryWrapper<Notice>()
                .eq(Notice::getStatus,1)
                .eq(Notice::getIsTop,1)
                .orderByDesc(Notice::getCreateTime)
        );
    }

    @Override
    @Cacheable(key = StoreConstants.WX_ALL_NOTICE)
    public List<Notice> queryWxAllNoticeList() {
        return noticeMapper.selectList(new LambdaQueryWrapper<Notice>()
                .eq(Notice::getStatus,1)
                .orderByDesc(Notice::getCreateTime)
        );
    }
}
