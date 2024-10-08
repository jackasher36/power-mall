package com.powernode.feign.sentinel;

import com.powernode.domain.Member;
import com.powernode.feign.ProdMemberFeign;
import com.powernode.model.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 *
 */
@Component
@Slf4j
public class ProdMemberFeignSentinel implements ProdMemberFeign {
    @Override
    public Result<List<Member>> getMemberListByOpenIds(List<String> openIds) {
        log.error("远程调用：根据会员openId集合查询会员对象集合失败");
        return null;
    }
}
