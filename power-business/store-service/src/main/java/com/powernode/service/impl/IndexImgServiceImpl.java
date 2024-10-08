package com.powernode.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.powernode.constant.BusinessEnum;
import com.powernode.constant.StoreConstants;
import com.powernode.domain.IndexImg;
import com.powernode.domain.Prod;
import com.powernode.ex.handler.BusinessException;
import com.powernode.feign.StoreProdFeign;
import com.powernode.mapper.IndexImgMapper;
import com.powernode.model.Result;
import com.powernode.service.IndexImgService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Service
@CacheConfig(cacheNames = "com.powernode.service.impl.IndexImgServiceImpl")
public class IndexImgServiceImpl extends ServiceImpl<IndexImgMapper, IndexImg> implements IndexImgService{

    @Autowired
    private IndexImgMapper indexImgMapper;

    @Autowired
    private StoreProdFeign storeProdFeign;

    @Override
    @CacheEvict(key = StoreConstants.WX_INDEX_IMG_KEY)
    public Boolean saveIndexImg(IndexImg indexImg) {
        indexImg.setShopId(1L);
        indexImg.setCreateTime(new Date());
        // 获取关联类型
        Integer type = indexImg.getType();
        // 判断关联类型
        if (-1 == type) {
            // 说明:轮播图未关联商品
            indexImg.setProdId(null);
        }
        return indexImgMapper.insert(indexImg)>0;
    }

    @Override
    public IndexImg queryIndexImgInfoById(Long imgId) {
        // 根据标识查询轮播图信息
        IndexImg indexImg = indexImgMapper.selectById(imgId);
        // 获取轮播图关联类型
        Integer type = indexImg.getType();
        // 判断关联商品
        if (0 == type) {
            // 说明：当前轮播图已关联商品
            // 获取关联商品的id
            Long prodId = indexImg.getProdId();
            // 远程调用：根据商品id查询商品图片和名称
            Result<List<Prod>> result = storeProdFeign.getProdListByIds(Arrays.asList(prodId));
            // 判断是否正确
            if (BusinessEnum.OPERATION_FAIL.getCode().equals(result.getCode())) {
                // 即：操作失败
                throw new BusinessException(result.getMsg());
            }
            // 获取数据
            List<Prod> prods = result.getData();
            // 判断集合是否有值
            if (CollectionUtil.isNotEmpty(prods) && prods.size() != 0) {
                // 获取商品对象
                Prod prod = prods.get(0);
                indexImg.setPic(prod.getPic());
                indexImg.setProdName(prod.getProdName());
            }
        }

        return indexImg;
    }

    @Override
    @CacheEvict(key = StoreConstants.WX_INDEX_IMG_KEY)
    public Boolean modifyIndexImg(IndexImg indexImg) {
        return indexImgMapper.updateById(indexImg)>0;
    }

    @Override
    @CacheEvict(key = StoreConstants.WX_INDEX_IMG_KEY)
    public Boolean removeIndexImgByIds(List<Long> imgIds) {
        return indexImgMapper.deleteBatchIds(imgIds)==imgIds.size();
    }

    @Override
    @Cacheable(key = StoreConstants.WX_INDEX_IMG_KEY)
    public List<IndexImg> queryWxIndexImgList() {
        return indexImgMapper.selectList(new LambdaQueryWrapper<IndexImg>()
                .eq(IndexImg::getStatus,1)
                .orderByDesc(IndexImg::getSeq)
        );
    }
}
