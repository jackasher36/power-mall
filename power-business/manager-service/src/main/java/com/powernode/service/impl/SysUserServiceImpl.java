package com.powernode.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.powernode.domain.SysUser;
import com.powernode.domain.SysUserRole;
import com.powernode.mapper.SysUserMapper;
import com.powernode.mapper.SysUserRoleMapper;
import com.powernode.service.SysUserRoleService;
import com.powernode.service.SysUserService;
import com.powernode.util.AuthUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService{

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    @Autowired
    private SysUserRoleService sysUserRoleService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    /**
     * 新增管理员
     *  1.新增管理员
     *  2.新增管理员与角色的关系
     *
     * @param sysUser
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer saveSysUser(SysUser sysUser) {
        // 新增管理员
        sysUser.setCreateUserId(AuthUtils.getLoginUserId());
        sysUser.setCreateTime(new Date());
        sysUser.setShopId(1L);
        sysUser.setPassword(passwordEncoder.encode(sysUser.getPassword()));
        int i = sysUserMapper.insert(sysUser);
        if (i > 0) {
            // 获取管理员标识
            Long userId = sysUser.getUserId();
            // 新增管理员与角色的关系
            // 获取管理员的角色id集合
            List<Long> roleIdList = sysUser.getRoleIdList();
            // 判断是否有值
            if (CollectionUtil.isNotEmpty(roleIdList) && roleIdList.size() != 0) {
                // 创建管理员与角色关系的集合
                List<SysUserRole> sysUserRoleList = new ArrayList<>();
                // 循环遍历角色id集合
                roleIdList.forEach(roleId -> {
                    // 创建管理员与角色的关系
                    SysUserRole sysUserRole = new SysUserRole();
                    sysUserRole.setUserId(userId);
                    sysUserRole.setRoleId(roleId);
                    /*// 新增管理员与角色的关系
                    注意：不建议在循环中操作数据库，大家尽量需要避免在循环中操作数据库
                    sysUserRoleMapper.insert(sysUserRole);*/
                    sysUserRoleList.add(sysUserRole);
                });
                // 批量添加管理员与角色的关系
                sysUserRoleService.saveBatch(sysUserRoleList);
            }
        }
        return i;
    }

    @Override
    public SysUser querySysUserInfoByUserId(Long id) {
        // 根据标识查询管理员信息
        SysUser sysUser = sysUserMapper.selectById(id);
        // 根据用户标识查询管理员与角色的关系集合
        List<SysUserRole> sysUserRoleList = sysUserRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, id)
        );
        // 判断是否有值
        if (CollectionUtil.isNotEmpty(sysUserRoleList) && sysUserRoleList.size() != 0) {
            // 从管理员与角色关系集合中获取角色id集合
            List<Long> roldIdList = sysUserRoleList.stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
            sysUser.setRoleIdList(roldIdList);
        }
        return sysUser;
    }

    /**
     * 修改管理员信息
     *  1.删除原有的管理员与角色关系记录
     *  2.添加新的管理员与角色关系记录
     *  3.修改管理员信息
     * @param sysUser
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer modifySysUserInfo(SysUser sysUser) {
        // 获取管理员标识
        Long userId = sysUser.getUserId();
        // 删除原有的管理员与角色关系记录
        sysUserRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId,userId)
        );
        // 添加管理员与角色关系记录
        // 获取管理员的角色id集合
        List<Long> roleIdList = sysUser.getRoleIdList();
        // 判断是否有值
        if (CollectionUtil.isNotEmpty(roleIdList) && roleIdList.size() != 0) {
            // 创建管理员与角色关系的集合
            List<SysUserRole> sysUserRoleList = new ArrayList<>();
            // 循环遍历角色id集合
            roleIdList.forEach(roleId -> {
                // 创建管理员与角色的关系
                SysUserRole sysUserRole = new SysUserRole();
                sysUserRole.setUserId(userId);
                sysUserRole.setRoleId(roleId);
                    /*// 新增管理员与角色的关系
                    注意：不建议在循环中操作数据库，大家尽量需要避免在循环中操作数据库
                    sysUserRoleMapper.insert(sysUserRole);*/
                sysUserRoleList.add(sysUserRole);
            });
            // 批量添加管理员与角色的关系
            sysUserRoleService.saveBatch(sysUserRoleList);
        }
        // 修改管理员信息
        // 获取新密码（如果有值：说明管理员修改了密码，如果没有值：说明原密码不变）
        String newPassword = sysUser.getPassword();
        // 判断是否有值
        if (StringUtils.hasText(newPassword)) {
            // 有值：说明需要修改原密码
            sysUser.setPassword(passwordEncoder.encode(newPassword));
        }
        return sysUserMapper.updateById(sysUser);
    }

    /**
     * 批量/单个删除管理员
     * 1.批量/单个删除管理员与角色的关系记录
     * 2.批量/单个删除管理员
     *
     * @param userIds
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean removeSysUserListByUserIds(List<Long> userIds) {
        // 批量/单个删除管理员与角色的关系记录
        sysUserRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>()
                .in(SysUserRole::getUserId,userIds)
        );
        // 批量/单个删除管理员
        return sysUserMapper.deleteBatchIds(userIds)==userIds.size();
    }
}
