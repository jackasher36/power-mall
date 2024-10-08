package com.powernode.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.powernode.constant.ProductConstants;
import com.powernode.domain.Category;
import com.powernode.ex.handler.BusinessException;
import com.powernode.mapper.CategoryMapper;
import com.powernode.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
@Service
@CacheConfig(cacheNames = "com.powernode.service.impl.CategoryServiceImpl")
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService{

    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    @Cacheable(key = ProductConstants.ALL_CATEGORY_LIST_KEY)
    public List<Category> queryAllCategoryList() {
        return categoryMapper.selectList(new LambdaQueryWrapper<Category>()
                .orderByDesc(Category::getSeq)
        );
    }

    @Override
    @Cacheable(key = ProductConstants.FIRST_CATEGORY_LIST_KEY)
    public List<Category> queryFirstCategoryList() {
        return categoryMapper.selectList(new LambdaQueryWrapper<Category>()
                .eq(Category::getParentId,0)
                        .eq(Category::getStatus,1)
                .orderByDesc(Category::getSeq)
        );
    }

    @Override
    @Caching(evict = {
        @CacheEvict(key = ProductConstants.ALL_CATEGORY_LIST_KEY),
        @CacheEvict(key = ProductConstants.FIRST_CATEGORY_LIST_KEY),
        @CacheEvict(key = ProductConstants.WX_FIRST_CATEGORY)
    })
    public Boolean saveCategory(Category category) {
        category.setCreateTime(new Date());
        category.setUpdateTime(new Date());
        return categoryMapper.insert(category)>0;
    }

    /**
     * 修改商品类目
     *  需求：允许商品类目修改类目级别
     * @param category
     * @return
     */
    @Override
    @Caching(evict = {
            @CacheEvict(key = ProductConstants.ALL_CATEGORY_LIST_KEY),
            @CacheEvict(key = ProductConstants.FIRST_CATEGORY_LIST_KEY),
            @CacheEvict(key = ProductConstants.WX_FIRST_CATEGORY)
    })
    public Boolean modifyCategory(Category category) {
        // 修改后的pid
        Long parentId = category.getParentId();
        // 根据标识查询类目详情
        Category beforeCategory = categoryMapper.selectById(category.getCategoryId());
        // 获取商品类目之前的级别,如果parentId为0即为1级类目，不为0即为2级类目
        Long beforeParentId = beforeCategory.getParentId();
        // 判断商品类目修改的详情
        // 1 -> 2 : 之前pid为0 且 修改后的pid不为0
        if (0 == beforeParentId && null != parentId && 0 != parentId) {
            // 查询当前类目是否包含子类目，如果包含子类目，则不允许修改
            // 根据当前类目标识查询子类目
            List<Category> childList = categoryMapper.selectList(new LambdaQueryWrapper<Category>()
                    .eq(Category::getParentId, category.getCategoryId())
            );
            // 判断是否有值
            if (CollectionUtil.isNotEmpty(childList) && childList.size() != 0) {
                // 说明：当前类目包含子类目，不允许修改
                throw new BusinessException("当前类目包含子类目，不可修改");
            }
        }

        // 2 -> 1：之前pid不为0 且 当前pid为null
        if (0 != beforeParentId && null == parentId) {
            category.setParentId(0L);
        }
        return categoryMapper.updateById(category)>0;
    }

    /**
     * 删除商品类目
     *  需求：如果一级类目包含子类目，则不可删除
     * @param categoryId
     * @return
     */
    @Override
    @Caching(evict = {
            @CacheEvict(key = ProductConstants.ALL_CATEGORY_LIST_KEY),
            @CacheEvict(key = ProductConstants.FIRST_CATEGORY_LIST_KEY),
            @CacheEvict(key = ProductConstants.WX_FIRST_CATEGORY)
    })
    public Boolean removeCategoryById(Long categoryId) {
        // 根据类目标识查询子类目集合
        List<Category> childCategoryList = categoryMapper.selectList(new LambdaQueryWrapper<Category>()
                .eq(Category::getParentId, categoryId)
        );
        // 判断是否有值
        if (CollectionUtil.isNotEmpty(childCategoryList) && childCategoryList.size() != 0) {
            // 说明：当前类目包含子类目，不可删除
            throw new BusinessException("当前类目包含子类目，不可删除");
        }
        // 说明：当前类目不包含子类目
        return categoryMapper.deleteById(categoryId)>0;
    }

    @Override
    @Cacheable(key = ProductConstants.WX_FIRST_CATEGORY)
    public List<Category> queryWxCategoryListByPid(Long pid) {
        return categoryMapper.selectList(new LambdaQueryWrapper<Category>()
                .eq(Category::getStatus,1)
                .eq(Category::getParentId,pid)
                .orderByDesc(Category::getSeq)
        );
    }
}
