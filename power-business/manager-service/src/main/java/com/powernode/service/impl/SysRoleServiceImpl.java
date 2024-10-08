package com.powernode.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.powernode.constant.ManagerConstants;
import com.powernode.domain.SysRole;
import com.powernode.domain.SysRoleMenu;
import com.powernode.mapper.SysRoleMapper;
import com.powernode.mapper.SysRoleMenuMapper;
import com.powernode.service.SysRoleMenuService;
import com.powernode.service.SysRoleService;
import com.powernode.util.AuthUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@CacheConfig(cacheNames = "com.powernode.service.impl.SysRoleServiceImpl")
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService{

    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Autowired
    private SysRoleMenuService sysRoleMenuService;

    @Autowired
    private SysRoleMenuMapper sysRoleMenuMapper;

    /**
     * 查询的是系统中所有角色数据（全量查询）
     *  全量查询是需要将数据存放到缓存中
     *
     * @return
     */
    @Override
    @Cacheable(key = ManagerConstants.SYS_ALL_ROLE_KEY)
    public List<SysRole> querySysRoleList() {
        return sysRoleMapper.selectList(new LambdaQueryWrapper<SysRole>()
                .orderByDesc(SysRole::getCreateTime)
        );
    }

    /**
     * 新增角色
     * 1.新增角色
     * 2.新增角色与权限的关系集合
     * @param sysRole
     * @return
     */
    @Override
    @CacheEvict(key = ManagerConstants.SYS_ALL_ROLE_KEY)
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveSysRole(SysRole sysRole) {
        // 新增角色
        sysRole.setCreateTime(new Date());
        sysRole.setCreateUserId(AuthUtils.getLoginUserId());
        int i = sysRoleMapper.insert(sysRole);
        if (i > 0) {
            // 获取角色id
            Long roleId = sysRole.getRoleId();
            // 新增角色与权限的关系记录
            // 获取角色对应的权限id集合
            List<Long> menuIdList = sysRole.getMenuIdList();
            // 创建角色与权限关系集合对象
            List<SysRoleMenu> sysRoleMenuList = new ArrayList<>();
            // 判断是否有值
            if (CollectionUtil.isNotEmpty(menuIdList) && menuIdList.size() != 0) {
                // 循环遍历权限id集合
                menuIdList.forEach(menuId -> {
                    // 创建角色与权限关系记录
                    SysRoleMenu sysRoleMenu = new SysRoleMenu();
                    sysRoleMenu.setRoleId(roleId);
                    sysRoleMenu.setMenuId(menuId);
                    // 收集角色与权限的关系记录
                    sysRoleMenuList.add(sysRoleMenu);
                });
                // 批量添加角色与权限关系集合
                sysRoleMenuService.saveBatch(sysRoleMenuList);
            }
        }
        return i>0;
    }

    @Override
    public SysRole querySysRoleInfoByRoleId(Long roleId) {
        // 根据角色标识查询角色详情
        SysRole sysRole = sysRoleMapper.selectById(roleId);
        // 根据角色标识查询角色与权限关系集合
        List<SysRoleMenu> sysRoleMenuList = sysRoleMenuMapper.selectList(new LambdaQueryWrapper<SysRoleMenu>()
                .eq(SysRoleMenu::getRoleId, roleId)
        );
        // 判断是否有值
        if (CollectionUtil.isNotEmpty(sysRoleMenuList) && sysRoleMenuList.size() != 0) {
            // 说明角色与权限关系有值
            // 从角色与权限关系集合中获取权限id集合
            List<Long> menuIdList = sysRoleMenuList.stream().map(SysRoleMenu::getMenuId).collect(Collectors.toList());
            sysRole.setMenuIdList(menuIdList);
        }
        return sysRole;
    }

    @Override
    @CacheEvict(key = ManagerConstants.SYS_ALL_ROLE_KEY)
    @Transactional(rollbackFor = Exception.class)
    public Boolean modifySysRole(SysRole sysRole) {
        // 获取角色标识
        Long roleId = sysRole.getRoleId();
        // 删除角色原有的权限集合
        sysRoleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>()
                .eq(SysRoleMenu::getRoleId, roleId)
        );

        // 获取角色对应的权限id集合
        List<Long> menuIdList = sysRole.getMenuIdList();
        // 创建角色与权限关系集合对象
        List<SysRoleMenu> sysRoleMenuList = new ArrayList<>();
        // 判断是否有值
        if (CollectionUtil.isNotEmpty(menuIdList) && menuIdList.size() != 0) {
            // 循环遍历权限id集合
            menuIdList.forEach(menuId -> {
                // 创建角色与权限关系记录
                SysRoleMenu sysRoleMenu = new SysRoleMenu();
                sysRoleMenu.setRoleId(roleId);
                sysRoleMenu.setMenuId(menuId);
                // 收集角色与权限的关系记录
                sysRoleMenuList.add(sysRoleMenu);
            });
            // 批量添加角色与权限关系集合
            sysRoleMenuService.saveBatch(sysRoleMenuList);

        }
        // 更新角色信息
        return sysRoleMapper.updateById(sysRole)>0;
    }

    @Override
    @CacheEvict(key = ManagerConstants.SYS_ALL_ROLE_KEY)
    @Transactional(rollbackFor = Exception.class)
    public Boolean removeSysRoleListByIds(List<Long> roleIdList) {
        // 批量或单个删除角色与权限关系集合
        sysRoleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>()
                .in(SysRoleMenu::getRoleId,roleIdList)
        );
        // 批量或单个删除角色
        return sysRoleMapper.deleteBatchIds(roleIdList)==roleIdList.size();
    }
}
