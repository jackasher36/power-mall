package com.powernode.controller;

import com.powernode.domain.SysMenu;
import com.powernode.model.Result;
import com.powernode.service.SysMenuService;
import com.powernode.util.AuthUtils;
import com.powernode.vo.MenuAndAuth;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * 系统权限控制层
 */
@Api(tags = "系统权限接口管理")
@RequestMapping("sys/menu")
@RestController
public class SysMenuController {

    @Autowired
    private SysMenuService sysMenuService;

    /**
     * 查询用户的菜单权限和操作权限
     * @return
     */
//    sys/menu/nav
    @ApiOperation("查询用户的菜单权限和操作权限")
    @GetMapping("nav")
    public Result<MenuAndAuth> loadUserMenuAndAuth() {
        // 获取当前登录用户的标识
//        SecurityUser securityUser = (SecurityUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//        Long userId = securityUser.getUserId();
        Long loginUserId = AuthUtils.getLoginUserId();

        // 根据用户标识查询操作权限集合
        Set<String> perms = AuthUtils.getLoginUserPerms();
        // 根据用户标识查询菜单权限集合
        Set<SysMenu> menus = sysMenuService.queryUserMenuListByUserId(loginUserId);

        // 创建菜单和操作权限对象
        MenuAndAuth menuAndAuth = new MenuAndAuth(menus,perms);
        return Result.success(menuAndAuth);
    }

    /**
     * 查询系统所有权限集合
     * @return  List<SysMenu> 所有权限集合
     */
    @ApiOperation("查询系统所有权限集合")
    @GetMapping("table")
    @PreAuthorize("hasAuthority('sys:menu:list')")
    public Result<List<SysMenu>> loadAllSysMenuList() {
        List<SysMenu> list = sysMenuService.queryAllSysMenuList();
        return Result.success(list);
    }

    /**
     * 新增权限
     * @param sysMenu 系统权限对象
     * @return
     */
    @ApiOperation("新增权限")
    @PostMapping
    @PreAuthorize("hasAuthority('sys:menu:save')")
    public Result<String> saveSysMenu(@RequestBody SysMenu sysMenu) {
        Boolean saved = sysMenuService.saveSysMenu(sysMenu);
        return Result.handle(saved);
    }

    /**
     * 根据标识查询菜单权限信息
     * @param menuId 菜单权限标识
     * @return
     */
    @ApiOperation("根据标识查询菜单权限信息")
    @GetMapping("info/{menuId}")
    @PreAuthorize("hasAuthority('sys:menu:info')")
    public Result<SysMenu> loadSysMenuInfo(@PathVariable Long menuId) {
        SysMenu sysMenu = sysMenuService.getById(menuId);
        return Result.success(sysMenu);
    }

    /**
     * 修改菜单权限信息
     * @param sysMenu 菜单权限对象
     * @return
     */
    @ApiOperation("修改菜单权限信息")
    @PutMapping
    @PreAuthorize("hasAuthority('sys:menu:update')")
    public Result<String> modifySysMenu(@RequestBody SysMenu sysMenu) {
        Boolean modified = sysMenuService.modifySysMenu(sysMenu);
        return Result.handle(modified);
    }

    /**
     * 删除菜单权限
     * @param menuId 菜单权限标识
     * @return
     */
    @ApiOperation("删除菜单权限")
    @DeleteMapping("{menuId}")
    @PreAuthorize("hasAuthority('sys:menu:delete')")
    public Result<String> removeSysMenu(@PathVariable Long menuId) {
        Boolean removed = sysMenuService.removeSysMenuById(menuId);
        return Result.handle(removed);
    }
}
