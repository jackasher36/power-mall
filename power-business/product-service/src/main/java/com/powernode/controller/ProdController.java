package com.powernode.controller;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powernode.domain.Prod;
import com.powernode.domain.Sku;
import com.powernode.model.ChangeStock;
import com.powernode.model.Result;
import com.powernode.service.ProdService;
import com.powernode.service.SkuService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品管理控制层
 */
@Api(tags = "商品接口管理")
@RequestMapping("prod/prod")
@RestController
public class ProdController {

    @Autowired
    private ProdService prodService;

    @Autowired
    private SkuService skuService;

    /**
     * 多条件分页查询商品
     * @param current   页码
     * @param size      每页显示条数
     * @param prodName  商品名称
     * @param status    商品状态
     * @return
     */
    @ApiOperation("多条件分页查询商品")
    @GetMapping("page")
    @PreAuthorize("hasAuthority('prod:prod:page')")
    public Result<Page<Prod>> loadProdPage(@RequestParam Long current,
                                           @RequestParam Long size,
                                           @RequestParam(required = false) String prodName,
                                           @RequestParam(required = false) Long status) {
        // 创建商品分页对象
        Page<Prod> page = new Page<>(current,size);
        // 多条件分页查询商品
        page = prodService.page(page,new LambdaQueryWrapper<Prod>()
                .eq(ObjectUtil.isNotNull(status),Prod::getStatus,status)
                .like(StringUtils.hasText(prodName),Prod::getProdName,prodName)
                .orderByDesc(Prod::getCreateTime)
        );
        return Result.success(page);
    }

    /**
     * 新增商品
     * @param prod 商品对象
     * @return
     */
    @ApiOperation("新增商品")
    @PostMapping
    @PreAuthorize("hasAuthority('prod:prod:save')")
    public Result<String> saveProd(@RequestBody Prod prod) {
        Boolean saved = prodService.saveProd(prod);
        return Result.handle(saved);
    }

    /**
     * 根据标识查询商品详情
     * @param prodId 商品id
     * @return
     */
    @ApiOperation("根据标识查询商品详情")
    @GetMapping("info/{prodId}")
    @PreAuthorize("hasAuthority('prod:prod:info')")
    public Result<Prod> loadProdInfo(@PathVariable Long prodId) {
        Prod prod = prodService.queryProdInfoById(prodId);
        return Result.success(prod);
    }

    /**
     * 修改商品信息
     * @param prod 商品对象
     * @return
     */
    @ApiOperation("修改商品信息")
    @PutMapping
    @PreAuthorize("hasAuthority('prod:prod:update')")
    public Result<String> modifyProdInfo(@RequestBody Prod prod) {
        Boolean modified = prodService.modifyProdInfo(prod);
        return Result.handle(modified);
    }

    /**
     * 删除商品
     * @param prodId 商品id
     * @return
     */
    @ApiOperation("删除商品")
    @DeleteMapping("{prodId}")
    @PreAuthorize("hasAuthority('prod:prod:delete')")
    public Result<String> removeProd(@PathVariable Long prodId) {
        Boolean removed = prodService.removeProdById(prodId);
        return Result.handle(removed);
    }

    //////////////////////////////////// feign 接口 ///////////////////////////
    @GetMapping("getProdListByIds")
    public Result<List<Prod>> getProdListByIds(@RequestParam List<Long> prodIdList) {
        List<Prod> prods = prodService.listByIds(prodIdList);
        return Result.success(prods);
    }

    @GetMapping("getProdListByCategoryIds")
    public Result<List<Prod>> getProdListByCategoryIds(@RequestParam List<Long> categoryIds) {
        List<Prod> list = prodService.list(new LambdaQueryWrapper<Prod>()
                .in(Prod::getCategoryId, categoryIds)
        );
        return Result.success(list);
    }

    @GetMapping("getSkuListBySkuIds")
    public Result<List<Sku>> getSkuListBySkuIds(@RequestParam List<Long> skuIds) {
        List<Sku> skus = skuService.listByIds(skuIds);
        return Result.success(skus);
    }

    @PostMapping("changeProdAndSkuStock")
    public Result<Boolean> changeProdAndSkuStock(@RequestBody ChangeStock changeStock) {
        Boolean changed = prodService.changeProdAndSkuChangeStock(changeStock);
        return Result.success(changed);
    }


    ////////////////////////////// 微信小程序数据接口 /////////////////////////

    /**
     * 小程序根据商品标识查询商品详情
     * @param prodId    商品标识
     * @return
     */
//    prod/prod/prod/prodInfo?prodId=99
    @ApiOperation("小程序根据商品标识查询商品详情")
    @GetMapping("prod/prodInfo")
    public Result<Prod> loadWxProdInfo(@RequestParam Long prodId) {
        // 根据商品标识查询商品详情
        Prod prod = prodService.queryWxProdInfoByProdId(prodId);
        return Result.success(prod);
    }
}
