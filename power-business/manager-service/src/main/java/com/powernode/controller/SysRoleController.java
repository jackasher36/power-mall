package com.powernode.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powernode.domain.SysRole;
import com.powernode.model.Result;
import com.powernode.service.SysRoleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 系统角色管理控制层
 */
@Api(tags = "系统角色接口管理")
@RequestMapping("sys/role")
@RestController
public class SysRoleController {

    @Autowired
    private SysRoleService sysRoleService;

    /**
     * 查询系统所有角色
     * @return
     */
    @ApiOperation("查询系统所有角色")
    @GetMapping("list")
    @PreAuthorize("hasAuthority('sys:role:list')")
    public Result<List<SysRole>> loadSysRoleList() {
        List<SysRole> roleList = sysRoleService.querySysRoleList();
        return Result.success(roleList);
    }

    /**
     *  多条件分页查询角色列表
     * @param current   页码
     * @param size      每页显示条件
     * @param roleName  角色名称
     * @return
     */
    @ApiOperation("多条件分页查询角色列表")
    @GetMapping("page")
    @PreAuthorize("hasAuthority('sys:role:page')")
    public Result<Page<SysRole>> loadSysRolePage(@RequestParam Long current,
                                                 @RequestParam Long size,
                                                 @RequestParam(required = false) String roleName) {
        // 创建分页对象
        Page<SysRole> page = new Page<>(current,size);
        // 多条件分页查询角色列表
        page = sysRoleService.page(page,new LambdaQueryWrapper<SysRole>()
                .like(StringUtils.hasText(roleName),SysRole::getRoleName,roleName)
                .orderByDesc(SysRole::getCreateTime)
        );
        return Result.success(page);
    }

    /**
     * 新增角色
     * @param sysRole 角色对象
     * @return
     */
    @ApiOperation("新增角色")
    @PostMapping
    @PreAuthorize("hasAuthority('sys:role:save')")
    public Result<String> saveSysRole(@RequestBody SysRole sysRole) {
        Boolean saved = sysRoleService.saveSysRole(sysRole);
        return Result.handle(saved);
    }

    /**
     * 根据标识查询角色详情
     * @param roleId    角色标识
     * @return
     */
    @ApiOperation("根据标识查询角色详情")
    @GetMapping("info/{roleId}")
    @PreAuthorize("hasAuthority('sys:role:info')")
    public Result<SysRole> loadSysRoleInfo(@PathVariable Long roleId) {
        SysRole sysRole = sysRoleService.querySysRoleInfoByRoleId(roleId);
        return Result.success(sysRole);
    }

    /**
     * 修改角色信息
     * @param sysRole 角色对象
     * @return
     */
    @ApiOperation("修改角色信息")
    @PutMapping
    @PreAuthorize("hasAuthority('sys:role:update')")
    public Result<String> modifySysRole(@RequestBody SysRole sysRole) {
        Boolean updated = sysRoleService.modifySysRole(sysRole);
        return Result.handle(updated);
    }

    /**
     * 批量/单个删除角色
     * @param roleIdList 角色id集合
     * @return
     */
    @ApiOperation("批量/单个删除角色")
    @DeleteMapping
    @PreAuthorize("hasAuthority('sys:role:delete')")
    public Result<String> removeSysRole(@RequestBody List<Long> roleIdList) {
        Boolean removed = sysRoleService.removeSysRoleListByIds(roleIdList);
        return Result.handle(removed);
    }
}
