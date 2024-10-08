package com.powernode.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powernode.domain.MemberCollection;
import com.powernode.domain.Prod;
import com.powernode.model.Result;
import com.powernode.service.MemberCollectionService;
import com.powernode.util.AuthUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 会员收藏商品业务控制层
 */
@Api(tags = "会员收藏商品接口管理")
@RequestMapping("p/collection")
@RestController
public class MemberCollectionController {

    @Autowired
    private MemberCollectionService memberCollectionService;

    /**
     * 查询会员收藏商品的数量
     * @return
     */
//    p/collection/count
    @ApiOperation("查询会员收藏商品的数量")
    @GetMapping("count")
    public Result<Long> loadMemberCollectionProdCount() {
        Long count = memberCollectionService.queryMemberCollectionProdCount();
        return Result.success(count);
    }

    /**
     * 分页查询会员收藏商品列表
     * @param current   页码
     * @param size      每页显示条数
     * @return
     */

//    p/collection/prods?current=1&size=10
    @ApiOperation("分页查询会员收藏商品列表")
    @GetMapping("prods")
    public Result<Page<Prod>> loadMemberCollectionProdPage(@RequestParam Long current,
                                                           @RequestParam Long size) {
        // 根据会员openid分页查询会员收藏商品列表
        Page<Prod> page = memberCollectionService.queryMemberCollectionProdPageByOpenId(AuthUtils.getMemberOpenId(),current,size);
        return Result.success(page);
    }

    /**
     * 小程序查询会员收藏商品状态
     * @param prodId    商品id
     * @return
     */
//    p/collection/isCollection?prodId=103
    @ApiOperation("小程序查询会员收藏商品状态")
    @GetMapping("isCollection")
    public Result<Boolean> loadMemberIsCollection(@RequestParam Long prodId) {
        long count = memberCollectionService.count(new LambdaQueryWrapper<MemberCollection>()
                .eq(MemberCollection::getOpenId, AuthUtils.getMemberOpenId())
                .eq(MemberCollection::getProdId, prodId)
        );
        return Result.success(1==count);
    }


    /**
     * 添加或取消收藏商品
     * @param prodId    商品id
     * @return
     */
//    p/collection/addOrCancel
    @ApiOperation("添加或取消收藏商品")
    @PostMapping("addOrCancel")
    public Result<String> addOrCancelMemberCollection(@RequestBody Long prodId) {
        Boolean flag = memberCollectionService.addOrCancelMemberCollection(AuthUtils.getMemberOpenId(),prodId);
        return Result.handle(flag);
    }
}

