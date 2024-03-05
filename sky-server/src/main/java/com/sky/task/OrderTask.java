package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class OrderTask {

    @Autowired
    private OrderMapper orderMapper;
    /**
     * 处理超时订单
     */
    //@Scheduled(cron = "0/5 * * * * ? ")//每5秒的第0秒
    @Scheduled(cron = "0 0 * * * ? ")//每分钟/小时
    public void processTimeoutOrder(){
        log.info("定时处理超时订单");
        List<Orders> orderList = orderMapper.getByStatusAndOrderTime(Orders.PENDING_PAYMENT, LocalDateTime.now().minusMinutes(15));
        for (Orders orders : orderList) {
            orders.setStatus(Orders.CANCELLED);
            orders.setCancelReason("点单超时自动取消");
            orders.setCancelTime(LocalDateTime.now());
            orderMapper.update(orders);
        }
    }

    /**
     * 定时处理派送中订单
     */
    //@Scheduled(cron = "2/5 * * * * ? ")//每5秒的第2秒
    @Scheduled(cron = "0 0 1 * * ? ")//每天1点触发1次
    public void processDeliveryOrder(){
        log.info("定时完成处理派送中订单");
        List<Orders> orderList = orderMapper.getByStatusAndOrderTime(Orders.DELIVERY_IN_PROGRESS, LocalDateTime.now().minusHours(1));
        for (Orders orders : orderList) {
            orders.setStatus(Orders.COMPLETED);

            orderMapper.update(orders);
        }
    }
}
