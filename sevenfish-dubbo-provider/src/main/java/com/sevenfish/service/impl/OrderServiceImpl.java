package com.sevenfish.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sevenfish.dao.OrderMapper;
import com.sevenfish.entity.Order;
import com.sevenfish.service.OrderService;
import org.springframework.stereotype.Service;

@Service
@DS("master_2")
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {
}
