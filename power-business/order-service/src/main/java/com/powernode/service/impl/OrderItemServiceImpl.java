package com.powernode.service.impl;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.powernode.domain.OrderItem;
import com.powernode.mapper.OrderItemMapper;
import com.powernode.service.OrderItemService;
@Service
public class OrderItemServiceImpl extends ServiceImpl<OrderItemMapper, OrderItem> implements OrderItemService{

}
