package com.powernode.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powernode.domain.ProdProp;
import com.powernode.domain.ProdPropValue;
import com.powernode.model.Result;
import com.powernode.service.ProdPropService;
import com.powernode.service.ProdPropValueService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品规格管理控制层
 */
@Api(tags = "商品规格接口管理")
@RequestMapping("prod/spec")
@RestController
public class ProdSpecController {

    @Autowired
    private ProdPropService prodPropService;

    @Autowired
    private ProdPropValueService prodPropValueService;

    /**
     * 多条件分页查询商品规格
     * @param current   页码
     * @param size      每页显示条数
     * @param propName  属性名称
     * @return
     */
    @ApiOperation("多条件分页查询商品规格")
    @GetMapping("page")
    @PreAuthorize("hasAuthority('prod:spec:page')")
    public Result<Page<ProdProp>> loadProdSpecPage(@RequestParam Long current,
                                                   @RequestParam Long size,
                                                   @RequestParam(required = false) String propName) {
        // 多条件分页查询商品规格
        Page<ProdProp> page = prodPropService.queryProdSpecPage(current,size,propName);
        return Result.success(page);
    }

    /**
     * 新增商品规格
     * @param prodProp 商品属性对象
     * @return
     */
    @ApiOperation("新增商品规格")
    @PostMapping
    @PreAuthorize("hasAuthority('prod:spec:save')")
    public Result<String> saveProdSpec(@RequestBody ProdProp prodProp) {
        Boolean saved = prodPropService.saveProdSpec(prodProp);
        return Result.handle(saved);
    }

    /**
     * 修改商品规格信息
     * @param prodProp 商品属性对象
     * @return
     */
    @ApiOperation("修改商品规格信息")
    @PutMapping
    @PreAuthorize("hasAuthority('prod:spec:update')")
    public Result<String> modifyProdSpec(@RequestBody ProdProp prodProp) {
        Boolean modified = prodPropService.modifyProdSpec(prodProp);
        return Result.handle(modified);
    }

    /**
     * 删除商品规格
     * @param propId 属性标识
     * @return
     */
    @ApiOperation("删除商品规格")
    @DeleteMapping("{propId}")
    @PreAuthorize("hasAuthority('prod:spec:delete')")
    public Result<String> removeProdSpec(@PathVariable Long propId) {
        Boolean removed = prodPropService.removeProdSpecByPropId(propId);
        return Result.handle(removed);
    }

//    prod/spec/list

    /**
     * 查询系统商品属性集合
     * @return
     */
    @ApiOperation("查询系统商品属性集合")
    @GetMapping("list")
    @PreAuthorize("hasAuthority('prod:spec:page')")
    public Result<List<ProdProp>> loadProdPropList() {
        List<ProdProp> prodProps = prodPropService.queryProdPropList();
        return Result.success(prodProps);
    }

//    prod/spec/listSpecValue/85

    /**
     * 根据商品属性id查询属性值集合
     * @param propId 商品属性id
     * @return
     */
    @ApiOperation("根据商品属性id查询属性值集合")
    @GetMapping("listSpecValue/{propId}")
    @PreAuthorize("hasAuthority('prod:spec:page')")
    public Result<List<ProdPropValue>> loadProdPropValues(@PathVariable Long propId) {
        List<ProdPropValue> prodPropValues = prodPropValueService.list(new LambdaQueryWrapper<ProdPropValue>()
                .eq(ProdPropValue::getPropId,propId)
        );
        return Result.success(prodPropValues);
    }
}
