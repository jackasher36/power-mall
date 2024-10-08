package com.powernode.feign;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powernode.domain.Category;
import com.powernode.domain.Prod;
import com.powernode.domain.ProdTagReference;
import com.powernode.feign.sentinel.SearchProdFeignSentinel;
import com.powernode.model.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 搜索业务模块调用产品业务模块: feign接口
 */
@FeignClient(value = "product-service",fallback = SearchProdFeignSentinel.class)
public interface SearchProdFeign {

    @GetMapping("prod/prodTag/getProdTagReferencePageByTagId")
    public Result<Page<ProdTagReference>> getProdTagReferencePageByTagId(@RequestParam Long current,
                                                                         @RequestParam Long size,
                                                                         @RequestParam Long tagId);

    @GetMapping("prod/prod/getProdListByIds")
    public Result<List<Prod>> getProdListByIds(@RequestParam List<Long> prodIdList);

    @GetMapping("prod/category/getCategoryListByParentId")
    public Result<List<Category>> getCategoryListByParentId(@RequestParam Long parentId);

    @GetMapping("prod/prod/getProdListByCategoryIds")
    public Result<List<Prod>> getProdListByCategoryIds(@RequestParam List<Long> categoryIds);
}
