package com.powernode.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powernode.domain.ProdProp;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface ProdPropService extends IService<ProdProp>{


    /**
     * 多条件分页查询商品规格
     * @param current
     * @param size
     * @param propName
     * @return
     */
    Page<ProdProp> queryProdSpecPage(Long current, Long size, String propName);

    /**
     * 新增商品规格
     * @param prodProp
     * @return
     */
    Boolean saveProdSpec(ProdProp prodProp);

    /**
     * 修改商品规格信息
     * @param prodProp
     * @return
     */
    Boolean modifyProdSpec(ProdProp prodProp);

    /**
     * 删除商品规格
     * @param propId
     * @return
     */
    Boolean removeProdSpecByPropId(Long propId);

    /**
     * 查询系统商品属性集合
     * @return
     */
    List<ProdProp> queryProdPropList();
}
