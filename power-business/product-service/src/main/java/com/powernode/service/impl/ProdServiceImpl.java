package com.powernode.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.powernode.domain.Prod;
import com.powernode.domain.ProdTagReference;
import com.powernode.domain.Sku;
import com.powernode.mapper.ProdMapper;
import com.powernode.mapper.ProdTagReferenceMapper;
import com.powernode.mapper.SkuMapper;
import com.powernode.model.ChangeStock;
import com.powernode.model.ProdChange;
import com.powernode.model.SkuChange;
import com.powernode.service.ProdService;
import com.powernode.service.ProdTagReferenceService;
import com.powernode.service.SkuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProdServiceImpl extends ServiceImpl<ProdMapper, Prod> implements ProdService{

    @Autowired
    private ProdMapper prodMapper;

    @Autowired
    private ProdTagReferenceService prodTagReferenceService;

    @Autowired
    private SkuService skuService;

    @Autowired
    private ProdTagReferenceMapper prodTagReferenceMapper;

    @Autowired
    private SkuMapper skuMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveProd(Prod prod) {
        // 新增商品
        prod.setShopId(1L);
        prod.setSoldNum(0);
        prod.setCreateTime(new Date());
        prod.setUpdateTime(new Date());
        prod.setPutawayTime(new Date());
        prod.setVersion(0);
        Prod.DeliveryModeVo deliveryModeVo = prod.getDeliveryModeVo();
        prod.setDeliveryMode(JSONObject.toJSONString(deliveryModeVo));
        int i = prodMapper.insert(prod);
        if (i > 0) {
            Long prodId = prod.getProdId();
            // 处理商品与分组标签的关系
            // 获取商品分组标签
            List<Long> tagIdList = prod.getTagList();
            // 判断是否有值
            if (CollectionUtil.isNotEmpty(tagIdList) && tagIdList.size() != 0) {
                // 创建商品与分组标签关系集合
                List<ProdTagReference> prodTagReferenceList = new ArrayList<>();
                // 循环遍历分组标签id集合
                tagIdList.forEach(tagId -> {
                    // 创建商品与分组标签的关系记录
                    ProdTagReference prodTagReference = new ProdTagReference();
                    prodTagReference.setProdId(prodId);
                    prodTagReference.setTagId(tagId);
                    prodTagReference.setCreateTime(new Date());
                    prodTagReference.setShopId(1L);
                    prodTagReference.setStatus(1);
                    prodTagReferenceList.add(prodTagReference);
                });
                // 批量添加商品与分组标签的关系记录
                prodTagReferenceService.saveBatch(prodTagReferenceList);
            }

            // 处理商品与商品sku的关系
            // 获取商品sku对象集合
            List<Sku> skuList = prod.getSkuList();
            // 判断是否有值
            if (CollectionUtil.isNotEmpty(skuList) && skuList.size() != 0) {
                // 循环遍历商品sku对象集合
                skuList.forEach(sku -> {
                    sku.setProdId(prodId);
                    sku.setCreateTime(new Date());
                    sku.setUpdateTime(new Date());
                    sku.setVersion(0);
                    sku.setActualStocks(sku.getStocks());
                });
                // 批量添加商品sku对象集合
                skuService.saveBatch(skuList);
            }
        }
        return i>0;
    }

    @Override
    public Prod queryProdInfoById(Long prodId) {
        // 根据标识查询商品详情
        Prod prod = prodMapper.selectById(prodId);
        if (ObjectUtil.isNull(prod)) {
            return prod;
        }
        // 根据商品标识查询商品与分组标签的关系
        List<ProdTagReference> prodTagReferenceList = prodTagReferenceMapper.selectList(new LambdaQueryWrapper<ProdTagReference>()
                .eq(ProdTagReference::getProdId, prodId)
        );
        // 判断是否有值
        if (CollectionUtil.isNotEmpty(prodTagReferenceList) && prodTagReferenceList.size() != 0) {
            // 从商品与分组标签的关系集合中获取分组标签id集合
            List<Long> tagIdList = prodTagReferenceList.stream().map(ProdTagReference::getTagId).collect(Collectors.toList());
            prod.setTagList(tagIdList);
        }
        // 根据商品id查询商品sku对象集合
        List<Sku> skus = skuMapper.selectList(new LambdaQueryWrapper<Sku>()
                .eq(Sku::getProdId, prodId)
        );
        prod.setSkuList(skus);
        return prod;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean modifyProdInfo(Prod prod) {
        // 获取商品标识
        Long prodId = prod.getProdId();
        // 删除商品原有的与分组标签的关系
        prodTagReferenceMapper.delete(new LambdaQueryWrapper<ProdTagReference>()
                .eq(ProdTagReference::getProdId,prodId)
        );
        // 获取商品分组标签
        List<Long> tagIdList = prod.getTagList();
        // 判断是否有值
        if (CollectionUtil.isNotEmpty(tagIdList) && tagIdList.size() != 0) {
            // 创建商品与分组标签关系集合
            List<ProdTagReference> prodTagReferenceList = new ArrayList<>();
            // 循环遍历分组标签id集合
            tagIdList.forEach(tagId -> {
                // 创建商品与分组标签的关系记录
                ProdTagReference prodTagReference = new ProdTagReference();
                prodTagReference.setProdId(prodId);
                prodTagReference.setTagId(tagId);
                prodTagReference.setCreateTime(new Date());
                prodTagReference.setShopId(1L);
                prodTagReference.setStatus(1);
                prodTagReferenceList.add(prodTagReference);
            });
            // 批量添加商品与分组标签的关系记录
            prodTagReferenceService.saveBatch(prodTagReferenceList);
        }

        // 批量修改商品sku对象集合
        // 获取商品sku对象集合
        List<Sku> skuList = prod.getSkuList();
        // 循环遍历商品sku对象集合
        skuList.forEach(sku -> {
            sku.setUpdateTime(new Date());
            sku.setActualStocks(sku.getStocks());
        });
        // 批量修改商品sku对象集合
        skuService.updateBatchById(skuList);

        // 修改商品对象
        prod.setUpdateTime(new Date());
        return prodMapper.updateById(prod)>0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean removeProdById(Long prodId) {
        // 删除商品与分组标签的关系
        prodTagReferenceMapper.delete(new LambdaQueryWrapper<ProdTagReference>()
                .eq(ProdTagReference::getProdId,prodId)
        );
        // 根据商品id删除商品sku对象
        skuMapper.delete(new LambdaQueryWrapper<Sku>()
                .eq(Sku::getProdId,prodId)
        );
        return prodMapper.deleteById(prodId)>0;
    }

    @Override
    public Prod queryWxProdInfoByProdId(Long prodId) {
        // 根据标识查询商品信息
        Prod prod = prodMapper.selectById(prodId);
        // 根据商品标识查询商品sku对象
        List<Sku> skus = skuMapper.selectList(new LambdaQueryWrapper<Sku>()
                .eq(Sku::getProdId, prodId)
        );
        prod.setSkuList(skus);
        return prod;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean changeProdAndSkuChangeStock(ChangeStock changeStock) {
        Boolean flag = false;
        // 获取商品sku购买数量对象
        List<SkuChange> skuChangeList = changeStock.getSkuChangeList();
        for (SkuChange skuChange : skuChangeList) {
            Long skuId = skuChange.getSkuId();
            Sku sku = skuMapper.selectById(skuId);
            Integer count = skuMapper.updateSkuStock(skuId,skuChange.getCount(),sku.getVersion());
            if (count == 1) {
                flag = true;
            } else {
                throw new RuntimeException("更新失败");
            }
        }

        // 获取商品prod购买数量对象
        List<ProdChange> prodChangeList = changeStock.getProdChangeList();
        for (ProdChange prodChange : prodChangeList) {
            Long prodId = prodChange.getProdId();
            Prod prod = prodMapper.selectById(prodId);
            Integer count = prodMapper.updateProdStock(prodId,prodChange.getCount(),prod.getVersion());
            if (count == 1) {
                flag = true;
            } else {
                throw new RuntimeException("更新失败");
            }
        }
        return flag;
    }
}
