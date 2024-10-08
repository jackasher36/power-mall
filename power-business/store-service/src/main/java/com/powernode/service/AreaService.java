package com.powernode.service;

import com.powernode.domain.Area;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface AreaService extends IService<Area>{


    /**
     * 查询全国地区列表
     * @return
     */
    List<Area> queryAllAreaList();

}
