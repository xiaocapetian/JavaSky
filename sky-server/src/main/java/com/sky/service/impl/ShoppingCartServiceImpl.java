package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import com.sky.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class ShoppingCartServiceImpl implements ShoppingCartService {

    @Autowired
    ShoppingCartMapper shoppingCartMapper;
    @Autowired
    DishMapper dishmapper;
    @Autowired
    SetmealMapper setmealMapper;


    @Override
    public void add(ShoppingCartDTO shoppingCartDTO) {
        //判断当前几入到购物车中的商品是否已经存在了
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO,shoppingCart);
        //用户的id在哪里?在请求头里,拦截器拦下来了!所以取出来
        Long userId = BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);

        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        //如果已经存在了,只需要将数最加一
        if(list!=null&&list.size()>0){//实际上这时候只可能等于1
            ShoppingCart shoppingCart1 = list.get(0);
            shoppingCart1.setNumber(shoppingCart1.getNumber()+1);
            shoppingCartMapper.updateNumberById(shoppingCart1);
        }
        //如果不存在,需要插入一条购物车数据
        else {
            //首先判断本次添加的是菜品还是套餐,才能去菜品表里查信息补全这个shoppingCart
            Long dishId = shoppingCart.getDishId();
            if(dishId!=null){
                //本次添加的是菜品
                Dish dish = dishmapper.getById(dishId);
                shoppingCart.setName(dish.getName());
                shoppingCart.setAmount(dish.getPrice());
                shoppingCart.setImage(dish.getImage());

            }else {
                //本次添加的是套餐
                SetmealVO setmealVO = setmealMapper.getById(shoppingCart.getSetmealId());
                shoppingCart.setName(setmealVO.getName());
                shoppingCart.setAmount(setmealVO.getPrice());
                shoppingCart.setImage(setmealVO.getImage());

            }
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            //现在shoppingCart补全了,可以插入了
            shoppingCartMapper.insert(shoppingCart);

        }
    }

    @Override
    public List<ShoppingCart> showShoppingCart() {
        ShoppingCart cart = ShoppingCart.builder().userId(BaseContext.getCurrentId()).build();
        List<ShoppingCart> listShoppingCart = shoppingCartMapper.list(cart);
        return listShoppingCart;
    }

    @Override
    public void cleanShoppingCart() {
        //用户的id在哪里?在请求头里,拦截器拦下来了!所以取出来
        Long userId = BaseContext.getCurrentId();
        shoppingCartMapper.cleanShoppingCart(userId);
    }

    @Override
    public void sub(ShoppingCartDTO shoppingCartDTO) {
        //判断当前要删购物车中的商品是否只剩1了
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO,shoppingCart);
        //用户的id在哪里?在请求头里,拦截器拦下来了!所以取出来
        Long userId = BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);

        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);


        if(list!=null&&list.size()>0){
            ShoppingCart shoppingCart1 = list.get(0);
            Integer number = shoppingCart1.getNumber();

            if(number==1){
                Long dishId = shoppingCart1.getDishId();
                shoppingCartMapper.deleteById(shoppingCart1.getId());

            }
            else {//如果>1,只需要将数减一
                shoppingCart1.setNumber(shoppingCart1.getNumber()-1);
                shoppingCartMapper.updateNumberById(shoppingCart1);
            }
        }





    }
}
