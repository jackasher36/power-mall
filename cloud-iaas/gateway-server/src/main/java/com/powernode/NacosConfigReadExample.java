package com.powernode;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;

import java.util.Properties;
import java.util.concurrent.Executor;

public class NacosConfigReadExample {
    public static void read() {
        try {
            // Nacos服务器地址
            String serverAddr = "127.0.0.1:8848";
            // 配置ID
            String dataId = "gateway-server-dev.yaml";
            // 配置分组
            String group = "A_GROUP";

            // 初始化Nacos配置客户端
            Properties properties = new Properties();
            properties.put("serverAddr", serverAddr);
            properties.put("namespace", "d8e857f1-d010-41d3-9317-0dd40153e3c4");

            ConfigService configService = NacosFactory.createConfigService(properties);

            // 获取配置
            String content = configService.getConfig(dataId, group, 5000);
            System.out.println("配置内容: \n" + content);

            // 动态监听配置变化（可选）
            configService.addListener(dataId, group, new Listener() {
                @Override
                public Executor getExecutor() {
                    return null; // 使用默认线程池
                }

                @Override
                public void receiveConfigInfo(String configInfo) {
                    System.out.println("配置已更新，新内容: " + configInfo);
                }
            });
        } catch (NacosException e) {
            e.printStackTrace();
        }
    }
}