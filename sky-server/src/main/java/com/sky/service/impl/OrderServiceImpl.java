package com.sky.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private WeChatPayUtil weChatPayUtil;
    @Autowired
    private UserMapper userMapper;
    /**
     * 用户下单
     * @param ordersSubmitDTO
     * @return
     */
    @Transactional
    @Override
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {
        //业务异常(地址为空,购物车为空)
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if(addressBook==null){
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        //业务异常(地址为空,购物车为空)
        //查购物车表
        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(userId);
        List<ShoppingCart> shoppingcartlist = shoppingCartMapper.list(shoppingCart);
        if(shoppingcartlist==null||shoppingcartlist.size()==0){
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        //向订单表插入1条数据
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO,orders);
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID);
        orders.setStatus(Orders.PENDING_PAYMENT);
        //订单号使用时间戳
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        //之前已经查过地址簿了,虽然没有传过来,但是地址簿有啊
        orders.setAddress(addressBook.getDetail());
        orders.setPhone(addressBook.getPhone());
        orders.setConsignee(addressBook.getConsignee());
        orders.setUserId(userId);
        //终于把orders补全了
        orderMapper.insert(orders);

        //向订单明细表插入n条数据[这里需要订单id,所以上一步需要返回主键值!]
        //别忘了之前已经查过购物车了
        List<OrderDetail> orderDetails = new ArrayList<>();
        for (ShoppingCart cart : shoppingcartlist) {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(cart,orderDetail);
            orderDetail.setOrderId(orders.getId());

            orderDetails.add(orderDetail);
        }
        orderDetailMapper.insertBatch(orderDetails);

        //洁空当前用户的购物车数据
        shoppingCartMapper.cleanShoppingCart(userId);
        //封装vo返回结果
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .orderAmount(orders.getAmount())
                .orderTime(orders.getOrderTime())
                .id(orders.getId())
                .orderNumber(orders.getNumber())
                .build();
        return orderSubmitVO;
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);

        //调用微信支付接口，生成预支付交易单
        JSONObject jsonObject = weChatPayUtil.pay(
                ordersPaymentDTO.getOrderNumber(), //商户订单号
                new BigDecimal(0.01), //支付金额，单位 元
                "苍穹外卖订单", //商品描述
                user.getOpenid() //微信用户的openid
        );

        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new OrderBusinessException("该订单已支付");
        }

        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        return vo;
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);
    }

    /**
     * 历史订单分页查询
     * @param pageNum
     * @param pageSize
     * @param status
     * @return
     */
    @Override
    public PageResult pageQuerry(int pageNum, int pageSize, Integer status) {
        PageHelper.startPage(pageNum,pageSize);
        OrdersPageQueryDTO ordersPageQueryDTO =new OrdersPageQueryDTO();
        //ordersPageQueryDTO.setPage(pageNum);
        //ordersPageQueryDTO.setPageSize(pageSize);
        ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());
        ordersPageQueryDTO.setStatus(status);
        //条件分页查询
        Page<Orders> page = orderMapper.pageQuery(ordersPageQueryDTO);

        List<OrderVO> orderVOList = new ArrayList<>();
        if(page!=null&&page.getTotal()>0){
            //遍历订单列表
            for (Orders orders : page) {
                Long orderid = orders.getId();
                List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderid(orderid);

                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders,orderVO);
                //orderVO的第一项是一个字符串啊,这nm也能拷贝吗,  ---不是这样的,OrderVO extends Orders 他继承了父类Orders,
                // 所以orderVO拥有父类Orders的全部属性
                orderVO.setOrderDetailList(orderDetailList);

                orderVOList.add(orderVO);
            }
        }


        return new PageResult(page.getTotal(),orderVOList);
    }

    /**
     * 查询订单详情
     * @param orderid
     * @return
     */
    @Override
    public OrderVO getOrderDetails(Long orderid) {
        //根据id查订单
        Orders orders = orderMapper.getById(orderid);
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderid(orderid);
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders,orderVO);
        orderVO.setOrderDetailList(orderDetailList);
        return orderVO;
    }

    @Override
    public void cancelOrder(Long id) {
        Orders orders = new Orders();
        orders.setId(id);
        orders.setStatus(Orders.CANCELLED);
        orderMapper.update(orders);
    }

    @Override
    public void repetition(Long orderid) {
        Long userId = BaseContext.getCurrentId();
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderid(orderid);

        for (OrderDetail orderDetail : orderDetailList) {
            ShoppingCart shoppingCart = new ShoppingCart();
            shoppingCart.setUserId(userId);

            BeanUtils.copyProperties(orderDetail,shoppingCart);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartMapper.insert(shoppingCart);
        }

    }

    @Override
    public PageResult pageQuerryAdmin(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(),ordersPageQueryDTO.getPageSize());
        //条件分页查询
        Page<Orders> page = orderMapper.pageQuery(ordersPageQueryDTO);

        List<OrderVO> orderVOList = new ArrayList<>();
        if(page!=null&&page.getTotal()>0){
            //遍历订单列表
            for (Orders orders : page) {
                Long orderid = orders.getId();
                List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderid(orderid);

                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders,orderVO);
                //orderVO的第一项是一个字符串啊,这nm也能拷贝吗,  ---不是这样的,OrderVO extends Orders 他继承了父类Orders,
                // 所以orderVO拥有父类Orders的全部属性
                orderVO.setOrderDishes(orderDetailList.toString());

                orderVOList.add(orderVO);
            }
        }
        return new PageResult(page.getTotal(),orderVOList);
    }

    /**
     * 各个状态的订单数量统计
     * @return
     */
    @Override
    public OrderStatisticsVO getOrdersStatistics() {
        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        orderStatisticsVO.setToBeConfirmed(orderMapper.count(Orders.TO_BE_CONFIRMED));
        orderStatisticsVO.setConfirmed(orderMapper.count(Orders.CONFIRMED));
        orderStatisticsVO.setDeliveryInProgress(orderMapper.count(Orders.DELIVERY_IN_PROGRESS));

        return orderStatisticsVO;
    }

    /**
     * 接单
     * @param id
     */
    @Override
    public void confirm(Long id) {
        Orders orders = new Orders();
        orders.setId(id);
        orders.setStatus(Orders.CONFIRMED);
        orderMapper.update(orders);
    }

    @Override
    public void rejection(OrdersRejectionDTO ordersRejectionDTO) throws Exception {
        // 根据id查询订单
        Orders ordersDB = orderMapper.getById(ordersRejectionDTO.getId());

        // 订单只有存在且状态为2（待接单）才可以拒单
        if (ordersDB == null || !ordersDB.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        //支付状态
        Integer payStatus = ordersDB.getPayStatus();
        if (payStatus == Orders.PAID) {
            //用户已支付，需要退款
            String refund = weChatPayUtil.refund(
                    ordersDB.getNumber(),
                    ordersDB.getNumber(),
                    new BigDecimal(0.01),
                    new BigDecimal(0.01));
            log.info("申请退款：{}", refund);
        }

        Orders orders = new Orders();
        orders.setId(ordersDB.getId());
        orders.setStatus(Orders.CANCELLED);
        orders.setRejectionReason(ordersRejectionDTO.getRejectionReason());
        orders.setCancelTime(LocalDateTime.now());

        orderMapper.update(orders);
    }

    /**
     * 取消订单
     *
     * @param ordersCancelDTO
     */
    public void cancel(OrdersCancelDTO ordersCancelDTO) throws Exception {
        // 根据id查询订单
        Orders ordersDB = orderMapper.getById(ordersCancelDTO.getId());

        //支付状态
        Integer payStatus = ordersDB.getPayStatus();
        if (payStatus == 1) {
            //用户已支付，需要退款
            String refund = weChatPayUtil.refund(
                    ordersDB.getNumber(),
                    ordersDB.getNumber(),
                    new BigDecimal(0.01),
                    new BigDecimal(0.01));
            log.info("申请退款：{}", refund);
        }

        // 管理端取消订单需要退款，根据订单id更新订单状态、取消原因、取消时间
        Orders orders = new Orders();
        orders.setId(ordersCancelDTO.getId());
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason(ordersCancelDTO.getCancelReason());
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);
    }

    /**
     * 派送订单
     *
     * @param id
     */
    public void delivery(Long id) {
        // 根据id查询订单
        Orders ordersDB = orderMapper.getById(id);

        // 校验订单是否存在，并且状态为3
        if (ordersDB == null || !ordersDB.getStatus().equals(Orders.CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = new Orders();
        orders.setId(ordersDB.getId());
        // 更新订单状态,状态转为派送中
        orders.setStatus(Orders.DELIVERY_IN_PROGRESS);

        orderMapper.update(orders);
    }

}
