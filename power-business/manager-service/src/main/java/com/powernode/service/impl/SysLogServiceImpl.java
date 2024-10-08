package com.powernode.service.impl;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.powernode.mapper.SysLogMapper;
import com.powernode.domain.SysLog;
import com.powernode.service.SysLogService;
@Service
public class SysLogServiceImpl extends ServiceImpl<SysLogMapper, SysLog> implements SysLogService{

}
