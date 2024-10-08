package com.powernode.feign.sentinel;

import com.powernode.domain.Sku;
import com.powernode.feign.OrderProdFeign;
import com.powernode.model.ChangeStock;
import com.powernode.model.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 *
 */
@Component
@Slf4j
public class OrderProdFeignSentinel implements OrderProdFeign {
    @Override
    public Result<List<Sku>> getSkuListBySkuIds(List<Long> skuIds) {
        log.error("远程接口调用：根据商品skuId集合查询商品sku对象集合 失败");
        return null;
    }

    @Override
    public Result<Boolean> changeProdAndSkuStock(ChangeStock changeStock) {
        log.error("远程接口调用失败：修改商品prod和sku库存数量");
        return null;
    }
}
