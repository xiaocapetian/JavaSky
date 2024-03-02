package com.sky.controller.admin;

import com.sky.annotation.AutoFill;
import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController("AdminShopController")
@RequestMapping("/admin/shop")
@Api(tags="店铺相关接口")
@Slf4j
public class ShopController {

    public static final String SHOP_STATUS_KEY = "SHOP_STATUS";
    @Autowired
    private RedisTemplate redisTemplate;

    @PutMapping("/{status}")
    @ApiOperation("设置店铺营业状态")
    public Result setStatus(@PathVariable Integer status){
        log.info("设置店铺状态为,{},{}",status,status==1?"营业中":"打烊中");
        redisTemplate.opsForValue().set(SHOP_STATUS_KEY,status);
        return Result.success();

    }

    @GetMapping("/status")
    @ApiOperation("获取店铺营业状态")
    public Result<Integer>getStatus(){
        Integer status = (Integer) redisTemplate.opsForValue().get(SHOP_STATUS_KEY);
        log.info("获取店铺营业状态为,{}",status==1?"营业中":"打烊中");
        return Result.success(status);
    }
}
