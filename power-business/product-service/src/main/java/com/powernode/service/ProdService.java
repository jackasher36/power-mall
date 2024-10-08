package com.powernode.service;

import com.powernode.domain.Prod;
import com.baomidou.mybatisplus.extension.service.IService;
import com.powernode.model.ChangeStock;

public interface ProdService extends IService<Prod>{


    /**
     * 新增商品
     * @param prod
     * @return
     */
    Boolean saveProd(Prod prod);

    /**
     * 根据标识查询商品详情
     * @param prodId
     * @return
     */
    Prod queryProdInfoById(Long prodId);

    /**
     * 修改商品信息
     * @param prod
     * @return
     */
    Boolean modifyProdInfo(Prod prod);

    /**
     * 删除商品
     * @param prodId
     * @return
     */
    Boolean removeProdById(Long prodId);

    /**
     * 小程序根据商品标识查询商品详情
     * @param prodId
     * @return
     */
    Prod queryWxProdInfoByProdId(Long prodId);

    /**
     * 修改商品prod和sku库存数量
     * @param changeStock
     * @return
     */
    Boolean changeProdAndSkuChangeStock(ChangeStock changeStock);
}
