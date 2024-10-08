package com.powernode.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.powernode.constant.ProductConstants;
import com.powernode.domain.ProdProp;
import com.powernode.domain.ProdPropValue;
import com.powernode.mapper.ProdPropMapper;
import com.powernode.mapper.ProdPropValueMapper;
import com.powernode.service.ProdPropService;
import com.powernode.service.ProdPropValueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@CacheConfig(cacheNames = "com.powernode.service.impl.ProdPropServiceImpl")
public class ProdPropServiceImpl extends ServiceImpl<ProdPropMapper, ProdProp> implements ProdPropService{

    @Autowired
    private ProdPropMapper prodPropMapper;

    @Autowired
    private ProdPropValueMapper prodPropValueMapper;

    @Autowired
    private ProdPropValueService prodPropValueService;

    @Override
    public Page<ProdProp> queryProdSpecPage(Long current, Long size, String propName) {
        // 创建分页对象
        Page<ProdProp> page = new Page<>(current,size);
        // 多条件分页查询商品属性
        page = prodPropMapper.selectPage(page,new LambdaQueryWrapper<ProdProp>()
                .like(StringUtils.hasText(propName),ProdProp::getPropName,propName)
        );
        // 从分页对象中获取属性记录
        List<ProdProp> prodPropList = page.getRecords();
        // 判断是否有值
        if (CollectionUtil.isEmpty(prodPropList) || prodPropList.size() == 0) {
            // 如果属性对象集合没有值，说明属性值也为空
            return page;
        }
        // 从属性对象集合中获取属性id集合
        List<Long> propIdList = prodPropList.stream().map(ProdProp::getPropId).collect(Collectors.toList());

        // 属性id集合查询属性值对象集合
        List<ProdPropValue> prodPropValueList = prodPropValueMapper.selectList(new LambdaQueryWrapper<ProdPropValue>()
                .in(ProdPropValue::getPropId, propIdList)
        );
        // 循环遍历属性对象集合
        prodPropList.forEach(prodProp -> {
            // 从属性值对象集合中过滤出与当前属性对象的属性id一致的属性对象集合
            List<ProdPropValue> propValues = prodPropValueList.stream()
                    .filter(prodPropValue -> prodPropValue.getPropId().equals(prodProp.getPropId()))
                    .collect(Collectors.toList());
            prodProp.setProdPropValues(propValues);
        });
        return page;
    }

    /**
     * 新增商品规格
     *  1.新增商品属性对象 -> 属性id
     *  2.批量添加商品属性值对象
     * @param prodProp
     * @return
     */
    @Override
    @CacheEvict(key = ProductConstants.PROD_PROP_KEY)
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveProdSpec(ProdProp prodProp) {
        // 新增商品属性对象
        prodProp.setShopId(1L);
        prodProp.setRule(2);
        int i = prodPropMapper.insert(prodProp);
        if (i > 0) {
            // 获取属性id
            Long propId = prodProp.getPropId();
            // 添加商品属性对象与属性值的记录
            // 获取商品属性值集合
            List<ProdPropValue> prodPropValues = prodProp.getProdPropValues();
            // 判断是否有值
            if (CollectionUtil.isNotEmpty(prodPropValues) && prodPropValues.size() != 0) {
                // 循环遍历属性值对象集合
                prodPropValues.forEach(prodPropValue -> prodPropValue.setPropId(propId));
                // 批量添加属性值对象集合
                prodPropValueService.saveBatch(prodPropValues);
            }
        }
        return i>0;
    }

    @Override
    @CacheEvict(key = ProductConstants.PROD_PROP_KEY)
    @Transactional(rollbackFor = Exception.class)
    public Boolean modifyProdSpec(ProdProp prodProp) {
        // 获取新的属性值对象集合
        List<ProdPropValue> prodPropValues = prodProp.getProdPropValues();
        // 批量修改属性值对象
        boolean flag = prodPropValueService.updateBatchById(prodPropValues);
        if (flag) {
            // 修改属性对象
            prodPropMapper.updateById(prodProp);
        }
        return flag;
    }

    @Override
    @CacheEvict(key = ProductConstants.PROD_PROP_KEY)
    @Transactional(rollbackFor = Exception.class)
    public Boolean removeProdSpecByPropId(Long propId) {
        // 根据属性标识删除属性值
        prodPropValueMapper.delete(new LambdaQueryWrapper<ProdPropValue>()
                .eq(ProdPropValue::getPropId,propId)
        );
        // 删除属性对象
        return prodPropMapper.deleteById(propId)>0;
    }

    @Override
    @Cacheable(key = ProductConstants.PROD_PROP_KEY)
    public List<ProdProp> queryProdPropList() {
        return prodPropMapper.selectList(null);
    }
}
