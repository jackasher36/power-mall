package com.powernode.service;

import com.powernode.domain.SysMenu;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Set;

public interface SysMenuService extends IService<SysMenu>{


    /**
     * 根据用户标识查询菜单权限集合
     * @param loginUserId
     * @return
     */
    Set<SysMenu> queryUserMenuListByUserId(Long loginUserId);

    /**
     * 查询系统所有权限集合
     * @return
     */
    List<SysMenu> queryAllSysMenuList();

    /**
     * 新增权限
     * @param sysMenu
     * @return
     */
    Boolean saveSysMenu(SysMenu sysMenu);

    /**
     * 修改菜单权限信息
     * @param sysMenu
     * @return
     */
    Boolean modifySysMenu(SysMenu sysMenu);

    /**
     * 删除菜单权限
     * @param menuId
     * @return
     */
    Boolean removeSysMenuById(Long menuId);
}
