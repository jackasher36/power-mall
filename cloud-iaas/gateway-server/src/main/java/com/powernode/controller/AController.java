package com.powernode.controller;

import com.powernode.model.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author Jackasher
 * @version 1.0
 * @className AController
 * @since 1.0
 **/

@Component
@ResponseBody
@RequestMapping("/gateway-server")
public class AController {


    @RequestMapping("/a")
    public Result testa(){
        System.out.println("a is emitted!");
        return Result.success("success");
    }

    @RequestMapping("/doLogin")
    public Result testLogin(){
        System.out.println("doLogin is emitted!");
        return Result.success("success");
    }

    @Value("${server.port}")
    private String host;

    @Value("${mybatis-plus.mapper-locations}")
    private String location;


    @RequestMapping("/free")
    public String testFree(){
        return "free: " + host + "location:" + location;
    }

}
