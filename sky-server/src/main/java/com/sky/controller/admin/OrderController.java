package com.sky.controller.admin;

import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/admin/order")
@Slf4j
@Api(tags = "管理端订单接口")
@RestController("adminOrderController")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/conditionSearch")
    @ApiOperation("订单搜索")
    public Result<PageResult> getOrders(OrdersPageQueryDTO ordersPageQueryDTO){
        log.info("订单搜索");
        PageResult pageResult= orderService.pageQuerryAdmin(ordersPageQueryDTO);
        return Result.success(pageResult);
    }

    @GetMapping("/statistics")
    @ApiOperation("各个状态的订单数量统计")
    public Result<OrderStatisticsVO> getOrdersStatistics(){
        log.info("各个状态的订单数量统计");
        OrderStatisticsVO orderStatisticsVO= orderService.getOrdersStatistics();
        return Result.success(orderStatisticsVO);
    }

    @GetMapping("/details/{id}")
    @ApiOperation("查询订单详情")
    public Result<OrderVO> getOrderDetails(@PathVariable Long id){
        log.info("查询订单详情");
        OrderVO orderVO = orderService.getOrderDetails(id);
        return Result.success(orderVO);
    }
    @PutMapping("/confirm")
    @ApiOperation("接单")
    public Result confirm(@RequestBody Long id){
        log.info("接单");
        orderService.confirm(id);
        return Result.success();
    }
    @PutMapping("/rejection")
    @ApiOperation("拒单")
    public Result rejection(@RequestBody Long id,String rejectionReason) throws Exception {
        log.info("拒单");
        OrdersRejectionDTO ordersRejectionDTO = new OrdersRejectionDTO();
        ordersRejectionDTO.setId(id);
        ordersRejectionDTO.setRejectionReason(rejectionReason);
        orderService.rejection(ordersRejectionDTO);
        return Result.success();
    }
    /**
     * 取消订单
     *
     * @return
     */
    @PutMapping("/cancel")
    @ApiOperation("取消订单")
    public Result cancel(@RequestBody OrdersCancelDTO ordersCancelDTO) throws Exception {
        orderService.cancel(ordersCancelDTO);
        return Result.success();
    }

    /**
     * 派送订单
     *
     * @return
     */
    @PutMapping("/delivery/{id}")
    @ApiOperation("派送订单")
    public Result delivery(@PathVariable("id") Long id) {
        orderService.delivery(id);
        return Result.success();
    }
}
