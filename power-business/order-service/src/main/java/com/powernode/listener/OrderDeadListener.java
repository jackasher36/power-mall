package com.powernode.listener;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.powernode.constant.QueueConstants;
import com.powernode.domain.Order;
import com.powernode.model.ChangeStock;
import com.powernode.service.OrderService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 订单死信队列监听
 */
@Component
@Slf4j
public class OrderDeadListener {

    @Autowired
    private OrderService orderService;

    @RabbitListener(queues = QueueConstants.ORDER_DEAD_QUEUE)
    public void handlerOrderDeadMsg(Message message, Channel channel) {
        // 获取消息
        JSONObject jsonObject = JSONObject.parseObject(new String(message.getBody()));
        // 获取订单编号
        String orderNumber = jsonObject.getString("orderNumber");
        // 获取商品数量对象
        ChangeStock changeStock = jsonObject.getObject("changeStock", ChangeStock.class);

        // 根据订单编号查询订单
        Order order = orderService.getOne(new LambdaQueryWrapper<Order>()
                .eq(Order::getOrderNumber, orderNumber)
        );

        // 判断订单是否存在
        if (ObjectUtil.isNull(order)) {
            log.error("订单编号{}无效",orderNumber);
            try {
                // 签收消息
                channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return;
        }
        // 判断订单是否已支付
        if (1 == order.getIsPayed()) {
            try {
                // 签收消息
                channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return;
        }

        // 目前还不确定当前订单是否已经支付
        // 必须调用第三方的订单查询接口，查询订单支付情况，如果第三方说：订单已支付，签收消息结束，否则订单数据回滚

        // 假设：当前订单已经确认没有支付
        try {
            // 订单回滚
            orderService.orderRollBack(order,changeStock);
            // 签收消息
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
