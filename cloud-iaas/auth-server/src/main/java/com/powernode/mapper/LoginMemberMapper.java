package com.powernode.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.powernode.domain.LoginMember;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LoginMemberMapper extends BaseMapper<LoginMember> {
}