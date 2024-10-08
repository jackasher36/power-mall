package com.powernode.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.powernode.constant.BusinessEnum;
import com.powernode.domain.Member;
import com.powernode.domain.ProdComm;
import com.powernode.ex.handler.BusinessException;
import com.powernode.feign.ProdMemberFeign;
import com.powernode.mapper.ProdCommMapper;
import com.powernode.model.Result;
import com.powernode.service.ProdCommService;
import com.powernode.vo.ProdCommData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProdCommServiceImpl extends ServiceImpl<ProdCommMapper, ProdComm> implements ProdCommService{

    @Autowired
    private ProdCommMapper prodCommMapper;

    @Autowired
    private ProdMemberFeign prodMemberFeign;


    @Override
    public Boolean replyAndExamineProdComm(ProdComm prodComm) {
        // 获取商品评论内容
        String replyContent = prodComm.getReplyContent();
        // 判断评论内容是否有值
        if (StringUtils.hasText(replyContent)) {
            prodComm.setReplyTime(new Date());
            prodComm.setReplySts(1);
        }
        return prodCommMapper.updateById(prodComm)>0;
    }

    @Override
    public ProdCommData queryWxProdCommDataByProdId(Long prodId) {
        // 根据商品id查询商品评论总数量
        Long allCount = prodCommMapper.selectCount(new LambdaQueryWrapper<ProdComm>()
                .eq(ProdComm::getProdId, prodId)
                .eq(ProdComm::getStatus, 1)
        );
        // 根据商品id查询商品好评数量
        Long goodCount = prodCommMapper.selectCount(new LambdaQueryWrapper<ProdComm>()
                .eq(ProdComm::getProdId, prodId)
                .eq(ProdComm::getStatus, 1)
                .eq(ProdComm::getEvaluate,0)
        );

        // 根据商品id查询商品中评数量
        Long secodeCount = prodCommMapper.selectCount(new LambdaQueryWrapper<ProdComm>()
                .eq(ProdComm::getProdId, prodId)
                .eq(ProdComm::getStatus, 1)
                .eq(ProdComm::getEvaluate,1)
        );

        // 根据商品id查询商品差评数量
        Long badCount = prodCommMapper.selectCount(new LambdaQueryWrapper<ProdComm>()
                .eq(ProdComm::getProdId, prodId)
                .eq(ProdComm::getStatus, 1)
                .eq(ProdComm::getEvaluate,2)
        );

        // 根据商品id查询商品有图评论数量
        Long picCount = prodCommMapper.selectCount(new LambdaQueryWrapper<ProdComm>()
                .eq(ProdComm::getProdId, prodId)
                .eq(ProdComm::getStatus, 1)
                .isNotNull(ProdComm::getPics)
        );

        // 好评率 = 好评数量 / 评论总数量
        BigDecimal goodLv = BigDecimal.ZERO;
        if (0 != allCount) {
            goodLv = new BigDecimal(goodCount)
                    .divide(new BigDecimal(allCount),3, RoundingMode.HALF_DOWN)
                    .multiply(new BigDecimal(100));
        }

        return ProdCommData.builder()
                .allCount(allCount).goodCount(goodCount).secondCount(secodeCount)
                .badCount(badCount).picCount(picCount).goodLv(goodLv)
                .build();
    }

    @Override
    public Page<ProdComm> queryWxProdCommPageByProd(Long current, Long size, Long prodId, Long evaluate) {
        // 创建评论分页对象
        Page<ProdComm> page = new Page<>(current,size);
        // 根据商品id分页查询单个商品的评论
        page = prodCommMapper.selectPage(page,new LambdaQueryWrapper<ProdComm>()
                .eq(ProdComm::getProdId,prodId)
                .eq(ProdComm::getStatus,1)
                .eq(0==evaluate||1==evaluate||2==evaluate,ProdComm::getEvaluate,evaluate)
                .isNotNull(3==evaluate,ProdComm::getPics)
                .orderByDesc(ProdComm::getCreateTime)
        );
        // 从分页对象中获取评论记录
        List<ProdComm> prodCommList = page.getRecords();
        // 判断是否有值
        if (CollectionUtils.isEmpty(prodCommList) || prodCommList.size() == 0) {
            return page;
        }
        // 从商品评论集合中获取会员openId集合
        List<String> openIdList = prodCommList.stream().map(ProdComm::getOpenId).collect(Collectors.toList());
        // 远程调用：根据会员openId集合查询会员对象集合
        Result<List<Member>> result = prodMemberFeign.getMemberListByOpenIds(openIdList);
        // 判断操作结果
        if (result.getCode().equals(BusinessEnum.OPERATION_FAIL.getCode())) {
            throw new BusinessException("远程调用：根据会员openId集合查询会员对象集合失败");
        }
        // 获取数据
        List<Member> memberList = result.getData();
        // 循环遍历评论集合
        prodCommList.forEach(prodComm -> {
            // 从会员对象集合中过滤出与当前会员对象的openId一致的会员对象
            Member member = memberList.stream()
                    .filter(m -> m.getOpenId().equals(prodComm.getOpenId()))
                    .collect(Collectors.toList()).get(0);
            // 将会员昵称进行脱敏操作
            StringBuilder stringBuilder = new StringBuilder(member.getNickName());
            StringBuilder replaceNickName = stringBuilder.replace(1, stringBuilder.length() - 1, "***");
            prodComm.setNickName(replaceNickName.toString());
            prodComm.setPic(member.getPic());
        });
        return page;
    }
}
