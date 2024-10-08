package com.powernode.feign;

import com.powernode.domain.Sku;
import com.powernode.feign.sentinel.OrderProdFeignSentinel;
import com.powernode.model.ChangeStock;
import com.powernode.model.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 订单业务模块调用商品业务模块：feign接口
 */
@FeignClient(value = "product-service",fallback = OrderProdFeignSentinel.class)
public interface OrderProdFeign {

    @GetMapping("prod/prod/getSkuListBySkuIds")
    public Result<List<Sku>> getSkuListBySkuIds(@RequestParam List<Long> skuIds);

    @PostMapping("prod/prod/changeProdAndSkuStock")
    public Result<Boolean> changeProdAndSkuStock(@RequestBody ChangeStock changeStock);
}
