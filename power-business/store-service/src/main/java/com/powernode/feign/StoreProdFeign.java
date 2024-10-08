package com.powernode.feign;

import com.powernode.domain.Prod;
import com.powernode.feign.sentinel.StoreProdFeignSentinel;
import com.powernode.model.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 门店业务模块调用商品业务模块feign接口
 */
@FeignClient(value = "product-service",fallback = StoreProdFeignSentinel.class)
public interface StoreProdFeign {

    @GetMapping("prod/prod/getProdListByIds")
    public Result<List<Prod>> getProdListByIds(@RequestParam List<Long> prodIdList);

}
