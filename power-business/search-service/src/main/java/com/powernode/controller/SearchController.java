package com.powernode.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powernode.domain.Prod;
import com.powernode.model.Result;
import com.powernode.service.SearchService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 搜索业务控制层
 */
@Api(tags = "搜索业务接口管理")
@RestController
public class SearchController {

    @Autowired
    private SearchService searchService;

    /**
     * 根据分组标签分页查询商品
     * @param current   页码
     * @param size      每页显示条数
     * @param tagId     分组标签id
     * @return
     */
//    prod/prodListByTagId?tagId=6&size=6
    @ApiOperation("根据分组标签分页查询商品")
    @GetMapping("prod/prodListByTagId")
    public Result<Page<Prod>> loadWxProdPageByTagId(@RequestParam(defaultValue = "1") Long current,
                                                    @RequestParam Long size,
                                                    @RequestParam Long tagId) {
        // 根据分组标签分页查询商品
        Page<Prod> page = searchService.queryWxProdPageByTagId(current,size,tagId);
        return Result.success(page);
    }

    /**
     * 根据商品类目标识查询商品集合
     * @param categoryId    商品类目id
     * @return
     */
//    prod/category/prod/list?categoryId=107
    @ApiOperation("根据商品类目标识查询商品集合")
    @GetMapping("prod/category/prod/list")
    public Result<List<Prod>> loadWxProdListByCategoryId(@RequestParam Long categoryId) {
        // 根据商品类目标识查询商品集合
        List<Prod> list = searchService.queryWxProdListByCategoryId(categoryId);
        return Result.success(list);
    }
}
