package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    UserMapper userMapper;

    /**
     * turnoverReportVO
     * @param begin
     * @param end
     * @return
     */
    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        LocalDate day = begin;
        while (!day.equals(end)){
            //日期计算,指定日期后一天的日期
            day = day.plusDays(1);
            dateList.add(day);
        }
        List<Double> turnoverList = new ArrayList<>();
        for (LocalDate localDate : dateList) {
//査询date日期对应的营业额数据、营业额是指:状态为“已完成”的订单金额合计
            //把这个日期转成时分秒形式,拼上最小和最大时间
            LocalDateTime localDateTimeBegin = LocalDateTime.of(localDate, LocalTime.MIN);
            LocalDateTime localDateTimeEnd = LocalDateTime.of(localDate, LocalTime.MAX);
            //select sum(amount) from
            Map map = new HashMap<>();
            map.put("begin",localDateTimeBegin);
            map.put("end",localDateTimeEnd);
            map.put("status", Orders.COMPLETED);
            Double turnover = orderMapper.sumByMap(map);
            turnover = turnover==null?0.0:turnover;
            turnoverList.add(turnover);
        }
        //构建日期字符串
        String dateListStr = StringUtils.join(dateList, ",");
        //构建营业额字符串
        String turnoverListStr = StringUtils.join(turnoverList, ",");
        TurnoverReportVO turnoverReportVO = TurnoverReportVO.builder()
                .turnoverList(turnoverListStr)
                .dateList(dateListStr)
                .build();

        return turnoverReportVO;
    }

    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        LocalDate day = begin;
        while (!day.equals(end)){
            //日期计算,指定日期后一天的日期
            day = day.plusDays(1);
            dateList.add(day);
        }
        List<Integer> newUserList = new ArrayList<>();
        List<Integer> totalUserList = new ArrayList<>();

        for (LocalDate localDate : dateList) {
            //把这个日期转成时分秒形式,拼上最小和最大时间
            LocalDateTime localDateTimeBegin = LocalDateTime.of(localDate, LocalTime.MIN);
            LocalDateTime localDateTimeEnd = LocalDateTime.of(localDate, LocalTime.MAX);
            //select sum(amount) from
            Map map = new HashMap<>();

            map.put("end",localDateTimeEnd);
            Integer totalUsersNum = userMapper.getUserNumByMap(map);
            map.put("begin",localDateTimeBegin);
            Integer newUsersNum = userMapper.getUserNumByMap(map);



            newUserList.add(newUsersNum);
            totalUserList.add(totalUsersNum);
        }
        String dateListStr = StringUtils.join(dateList, ",");
        String newUserListStr = StringUtils.join(newUserList,",");
        String totalUserListStr = StringUtils.join(totalUserList,",");

        UserReportVO userReportVO = UserReportVO.builder()
                .dateList(dateListStr)
                .newUserList(newUserListStr)
                .totalUserList(totalUserListStr)
                .build();
        return userReportVO;
    }

    @Override
    public OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        LocalDate day = begin;
        while (!day.equals(end)){
            //日期计算,指定日期后一天的日期
            day = day.plusDays(1);
            dateList.add(day);
        }
        List<Integer> totalOrderList = new ArrayList<>();
        List<Integer> validOrderList = new ArrayList<>();
        for (LocalDate localDate : dateList) {
            //把这个日期转成时分秒形式,拼上最小和最大时间
            LocalDateTime localDateTimeBegin = LocalDateTime.of(localDate, LocalTime.MIN);
            LocalDateTime localDateTimeEnd = LocalDateTime.of(localDate, LocalTime.MAX);

            Map map = new HashMap<>();
            map.put("begin",localDateTimeBegin);
            map.put("end",localDateTimeEnd);
            //查询每天的订单总数
            Integer totalOrderNum = orderMapper.countByMap(map);
            //查询每天的有效订单数
            map.put("status", Orders.COMPLETED);
            Integer validOrderNum = orderMapper.countByMap(map);
            totalOrderList.add(totalOrderNum);
            validOrderList.add(validOrderNum);
        }
        //所谓订单总数 是这时间内,也就是是列表的元素加一起的和
        Integer totalOrderCount = totalOrderList.stream().reduce(Integer::sum).get();
        Integer validOrderCount = validOrderList.stream().reduce(Integer::sum).get();
        Double orderCompletionRate = 0.0;
        if(totalOrderCount!=0){
            orderCompletionRate = validOrderCount.doubleValue()/totalOrderCount;
        }
        OrderReportVO orderReportVO = OrderReportVO.builder()
                .dateList(StringUtils.join(dateList,","))
                .orderCountList(StringUtils.join(totalOrderList,","))
                .validOrderCountList(StringUtils.join(validOrderList,","))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
        return orderReportVO;
    }

    /**
     * 老师写的很简洁啊
     * @param begin
     * @param end
     * @return
     */
    @Override
    public SalesTop10ReportVO getSalesTop9(LocalDate begin, LocalDate end) {
        LocalDateTime localDateTimeBegin = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime localDateTimeEnd = LocalDateTime.of(end, LocalTime.MAX);
        List<GoodsSalesDTO> salesTop = orderMapper.getSalesTop(localDateTimeBegin, localDateTimeEnd);

        List<String> names = salesTop.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        List<Integer> numbers = salesTop.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());
        SalesTop10ReportVO salesTop10ReportVO = SalesTop10ReportVO.builder()
                .nameList(StringUtils.join(names,","))
                .numberList(StringUtils.join(numbers,","))
                .build();
        return salesTop10ReportVO;
    }

    /**
     * 我自己写的~
     * @param begin
     * @param end
     * @return
     */
    @Override
    public SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end) {

        LocalDateTime localDateTimeBegin = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime localDateTimeEnd = LocalDateTime.of(end, LocalTime.MAX);
        Map map = new HashMap<>();
        map.put("begin",localDateTimeBegin);
        map.put("end",localDateTimeEnd);
        //查询每天的有效订单数
        map.put("status", Orders.COMPLETED);

        List<Orders> orderslist = orderMapper.getByMap(map);
        //建一个map来存
        Map<String,Integer> dishmap = new HashMap<>();
        for (Orders orders : orderslist) {
            List<OrderDetail> orderDetails = orderDetailMapper.getByOrderid(orders.getId());
            for (OrderDetail orderDetail : orderDetails) {
                //getOrDefault
                dishmap.put(orderDetail.getName(),dishmap.getOrDefault(orderDetail.getName(),0)+orderDetail.getNumber());
            }
        }
        //建一个List存Map.Entry<>
        List<Map.Entry<String,Integer>> sortedEntries = new ArrayList<>(dishmap.entrySet());
        //然后这个list是可以排序的
        sortedEntries.sort((o1, o2) -> {return o2.getValue()-o1.getValue();});
        //截取前十名
        List<Map.Entry<String,Integer>> tokeTen = sortedEntries.subList(0,Math.min(10,sortedEntries.size()));
        List<String> topname = new ArrayList<>();
        List<Integer> topnum = new ArrayList<>();
        for (Map.Entry<String, Integer> topdish : tokeTen) {
            topname.add(topdish.getKey());
            topnum.add(topdish.getValue());
        }
        SalesTop10ReportVO salesTop10ReportVO = SalesTop10ReportVO.builder()
                .nameList(StringUtils.join(topname,","))
                .numberList(StringUtils.join(topnum,","))
                .build();
        return salesTop10ReportVO;
    }
}
