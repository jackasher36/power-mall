package com.powernode.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.powernode.domain.Basket;
import com.powernode.model.Result;
import com.powernode.service.BasketService;
import com.powernode.util.AuthUtils;
import com.powernode.vo.CartTotalAmount;
import com.powernode.vo.CartVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 购物车业务控制层
 */
@Api(tags = "购物车业务接口管理")
@RequestMapping("p/shopCart")
@RestController
public class BasketController {

    @Autowired
    private BasketService basketService;


    /**
     * 查询会员购物车中商品数量
     * @return
     */
    @ApiOperation("查询会员购物车中商品数量")
    @GetMapping("prodCount")
    public Result<Integer> loadMemberBasketProdCount() {
        String openId = AuthUtils.getMemberOpenId();
        Integer count = basketService.queryMemberBasketProdCount(openId);
        return Result.success(count);
    }

    /**
     * 查询会员购物车页面数据
     * @return
     */
//    p/shopCart/info
    @ApiOperation("查询会员购物车页面数据")
    @GetMapping("info")
    public Result<CartVo> loadMemberCartVo() {
        CartVo cartVo = basketService.queryMemberCartVo();
        return Result.success(cartVo);
    }

    /**
     * 计算会员选中购物车中商品的金额
     * @param basketIds     选中购物车记录id集合
     * @return
     */
//    p/shopCart/totalPay
    @ApiOperation("计算会员选中购物车中商品的金额")
    @PostMapping("totalPay")
    public Result<CartTotalAmount> calculateMemberCheckedBasketTotalAmount(@RequestBody List<Long> basketIds) {
        CartTotalAmount cartTotalAmount = basketService.calculateMemberCheckedBasketTotalAmount(basketIds);
        return Result.success(cartTotalAmount);
    }

    /**
     * 添加商品到购物车或修改商品在购物车中的数量
     * @param basket 购物车对象(shopId,prodId,skuId,prodCount)
     * @return
     */
//    p/shopCart/changeItem
    @ApiOperation("添加商品到购物车或修改商品在购物车中的数量")
    @PostMapping("changeItem")
    public Result<String> changeCartItem(@RequestBody Basket basket) {
        Boolean changed = basketService.changeCartItem(basket);
        return Result.handle(changed);
    }

    /**
     * 删除会员选中的购物车记录
     * @param basketIds 选中购物车记录id集合
     * @return
     */
//    p/shopCart/deleteItem
    @ApiOperation("删除会员选中的购物车记录")
    @DeleteMapping("deleteItem")
    public Result<String> removeMemberCheckedBasket(@RequestBody List<Long> basketIds) {
        boolean removed = basketService.removeBatchByIds(basketIds);
        return Result.handle(removed);
    }

    ///////////////////////////// feign 接口 ///////////////////////////////////
    @GetMapping("getBasketListByIds")
    public Result<List<Basket>> getBasketListByIds(@RequestParam List<Long> ids) {
        List<Basket> basketList = basketService.listByIds(ids);
        return Result.success(basketList);
    }

    @DeleteMapping("removeBasketByOpenIdAndSkuIds")
    public Result<Boolean> removeBasketByOpenIdAndSkuIds(@RequestBody Map<String,Object> param) {
        // 获取会员openId
        String openId = (String) param.get("openId");
        // 获取商品skuId集合
        List<Long> skuIdList = (List<Long>) param.get("skuIdList");
        // 根据会员openId和商品skuId集合删除购物车记录
        boolean removed = basketService.remove(new LambdaQueryWrapper<Basket>()
                .eq(Basket::getOpenId, openId)
                .in(Basket::getSkuId, skuIdList)
        );
        return Result.success(removed);
    }
}
