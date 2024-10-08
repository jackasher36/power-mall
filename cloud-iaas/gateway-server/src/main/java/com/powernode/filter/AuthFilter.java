package com.powernode.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.powernode.config.WhiteUrlsConfig;
import com.powernode.constant.AuthConstants;
import com.powernode.constant.BusinessEnum;
import com.powernode.constant.HttpConstants;
import com.powernode.model.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Date;

/**
 * 全局token过滤器
 *  前后端约定好令牌存放的位置：请求头的Authorization bearer token
 */
@Component
@Slf4j
public class AuthFilter implements GlobalFilter, Ordered {

    @Autowired
    private WhiteUrlsConfig whiteUrlsConfig;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 校验token
     * 1.获取请求路径
     * 2.判断请求路径是否可以放行
     *  放行：不需要验证身份
     *  不放行：需要对其进行身份的认证
     *
     * @param exchange
     * @param chain
     * @return
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 获取请求对象
        ServerHttpRequest request = exchange.getRequest();
        // 获取请求路径
        String path = request.getPath().toString();

        System.out.println("whiteUrlsConfig.getAllowUrls().contains(path):" + whiteUrlsConfig.getAllowUrls().contains(path));
        // 判断当前请求路径是否需要放行，是否存在于白名单中
        if (whiteUrlsConfig.getAllowUrls().contains(path)) {
            // 包含：请求路径包含在白名单中，即需要放行
            System.out.println("进入放行");
            return chain.filter(exchange);
        }

        // 请求路径不包含在白名单中，需要对其进行身份的验证
        // 从约定好的位置获取Authorization的值，值的格式为：bearer token
        String authorizationValue = request.getHeaders().getFirst(AuthConstants.AUTHORIZATION);
        // 判断是否有值
        if (StringUtils.hasText(authorizationValue)) {
            // 从Authorization的值中获取token
            String tokenValue = authorizationValue.replaceFirst(AuthConstants.BEARER, "");
            // 判断token值是否有值且是否在redis中存在
            if (StringUtils.hasText(tokenValue) && stringRedisTemplate.hasKey(AuthConstants.LOGIN_TOKEN_PREFIX + tokenValue)) {
                // 身份验证通过，放行
                return chain.filter(exchange);
            }
        }

        // 流程如果走到这：说明验证身份没有通过或请求不合法
        log.error("拦截非法请求，时间:{},请求API路径:{}",new Date(),path);

        // 获取响应对象
        ServerHttpResponse response = exchange.getResponse();
        // 设置响应头信息
        response.getHeaders().set(HttpConstants.CONTENT_TYPE, HttpConstants.APPLICATION_JSON);

        // 设置响应消息
        Result<Object> result = Result.fail(BusinessEnum.UN_AUTHORIZATION);

        // 创建一个objectMapper对象
        ObjectMapper objectMapper = new ObjectMapper();
        byte[] bytes = new byte[0];
        try {
            bytes = objectMapper.writeValueAsBytes(result);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        DataBuffer dataBuffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(dataBuffer));
    }

    @Override
    public int getOrder() {
        return -5;
    }
}
