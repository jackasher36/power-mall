package com.powernode.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录统一结果对象
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("登录统一结果对象")
public class LoginResult {
    
    @ApiModelProperty("令牌TOKEN")
    private String accessToken;

    @ApiModelProperty("有效时长")
    private Long expireIn;
}
