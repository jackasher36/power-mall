package com.powernode.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.powernode.constant.ManagerConstants;
import com.powernode.domain.SysMenu;
import com.powernode.ex.handler.BusinessException;
import com.powernode.mapper.SysMenuMapper;
import com.powernode.service.SysMenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@CacheConfig(cacheNames = "com.powernode.service.impl.SysMenuServiceImpl")
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements SysMenuService{

    @Autowired
    private SysMenuMapper sysMenuMapper;

    @Override
    @Cacheable(key = "#loginUserId")
    public Set<SysMenu> queryUserMenuListByUserId(Long loginUserId) {
        // 根据用户标识查询菜单权限集合
        Set<SysMenu> menus = sysMenuMapper.selectUserMenuListByUserId(loginUserId);
        // 将菜单权限集合的数据转换为树结构（即：数据结构应该为层级关系的）
        return transformTree(menus,0L);
    }

    /**
     * 集合转换为树结构
     *  1.已知菜单深度 <=2
     *
     *  2.未知菜单深度
     *
     * @param menus
     * @param pid
     * @return
     */
    private Set<SysMenu> transformTree(Set<SysMenu> menus, Long pid) {
        // 已知菜单深度<=2
        // 从菜单集合中获取根节点集合
        /*Set<SysMenu> roots = menus.stream()
                .filter(m -> m.getParentId().equals(pid))
                .collect(Collectors.toSet());
        // 循环遍历根节点集合
        roots.forEach(root -> {
            // 从菜单集合中过滤出它的父节点值与当前根节点的id值一致的菜单集合
            Set<SysMenu> child = menus.stream()
                    .filter(m -> m.getParentId().equals(root.getMenuId()))
                    .collect(Collectors.toSet());
            root.setList(child);
        });*/

        // 未知菜单深度
        // 获取根节点集合
        Set<SysMenu> roots = menus.stream()
                .filter(m -> m.getParentId().equals(pid))
                .collect(Collectors.toSet());
        // 循环节点集合
        roots.forEach(r -> r.setList(transformTree(menus,r.getMenuId())));
        return roots;
    }

    @Override
    @Cacheable(key = ManagerConstants.SYS_ALL_MENU_KEY)
    public List<SysMenu> queryAllSysMenuList() {
        return sysMenuMapper.selectList(null);
    }

    @Override
    @CacheEvict(key = ManagerConstants.SYS_ALL_MENU_KEY)
    public Boolean saveSysMenu(SysMenu sysMenu) {
        return sysMenuMapper.insert(sysMenu)>0;
    }

    @Override
    @CacheEvict(key = ManagerConstants.SYS_ALL_MENU_KEY)
    public Boolean modifySysMenu(SysMenu sysMenu) {
        // 获取菜单类型
        Integer type = sysMenu.getType();
        if (0 == type) {
            sysMenu.setParentId(0L);
        }
        return sysMenuMapper.updateById(sysMenu)>0;
    }

    /**
     * 如果说明当前菜单节点包含子节点，不可删除
     * @param menuId
     * @return
     */
    @Override
    @CacheEvict(key = ManagerConstants.SYS_ALL_MENU_KEY)
    public Boolean removeSysMenuById(Long menuId) {
        // 根据菜单标识查询子菜单集合
        List<SysMenu> sysMenuList = sysMenuMapper.selectList(new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getParentId, menuId)
        );
        // 判断子菜单集合是否有值
        if (CollectionUtil.isNotEmpty(sysMenuList) && sysMenuList.size() != 0) {
            // 说明：当前菜单节点包含子节点集合
            throw new BusinessException("当前菜单节点包含子节点集合，不可删除");
        }
        // 说明：当前菜单节点不包含子节点集合，可以删除
        return sysMenuMapper.deleteById(menuId)>0;
    }
}
