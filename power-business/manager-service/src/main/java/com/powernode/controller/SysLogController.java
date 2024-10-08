package com.powernode.controller;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powernode.domain.SysLog;
import com.powernode.model.Result;
import com.powernode.service.SysLogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 系统操作日志管理控制层
 */
@Api(tags = "系统操作日志接口管理")
@RequestMapping("sys/log")
@RestController
public class SysLogController {

    @Autowired
    private SysLogService sysLogService;

    /**
     *  多条件分页查询系统操作日志
     * @param current   页码
     * @param size      每页显示条件
     * @param userId    用户标识
     * @param operation 用户操作描述
     * @return
     */
    @ApiOperation("多条件分页查询系统操作日志")
    @GetMapping("page")
    @PreAuthorize("hasAuthority('sys:log:page')")
    public Result<Page<SysLog>> loadSysLogPage(@RequestParam Long current,
                                               @RequestParam Long size,
                                               @RequestParam(required = false) Long userId,
                                               @RequestParam(required = false) String operation) {
        // 创建分页对象page
        Page<SysLog> page = new Page<>(current,size);
        // 多条件分页查询系统操作日志
        page = sysLogService.page(page,new LambdaQueryWrapper<SysLog>()
                // 如果查询条件eq和like同时存在的话，优先eq
                .eq(ObjectUtil.isNotNull(userId),SysLog::getUserId,userId)
                .like(StringUtils.hasText(operation),SysLog::getOperation,operation)
                .orderByDesc(SysLog::getCreateDate)
        );
        return Result.success(page);
    }
}
