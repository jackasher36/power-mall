package com.powernode.feign.sentinel;

import com.powernode.domain.Prod;
import com.powernode.feign.MemberProdFeign;
import com.powernode.model.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 *
 */
@Component
@Slf4j
public class MemberProdFeignSentinel implements MemberProdFeign {
    @Override
    public Result<List<Prod>> getProdListByIds(List<Long> prodIdList) {
        log.error("远程调用失败：根据商品id集合查询商品对象集合");
        return null;
    }
}
