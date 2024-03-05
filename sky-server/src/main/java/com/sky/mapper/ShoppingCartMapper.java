package com.sky.mapper;

import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ShoppingCartMapper {

    /**
     * 动态条件查询
     * @param shoppingCart
     * @return
     */
    List<ShoppingCart> list(ShoppingCart shoppingCart);

    /**
     * 根据id修改商品数量
     * @param shoppingCart1
     */
    @Update("update sky_take_out.shopping_cart set number = #{number} where id = #{id}")
    void updateNumberById(ShoppingCart shoppingCart1);

    /**
     * 购物车加东西
     * @param shoppingCart
     */
    @Insert("insert into sky_take_out.shopping_cart (name, image, user_id, dish_id, setmeal_id," +
            " dish_flavor, amount, create_time) values " +
            "(#{name},#{image},#{userId},#{dishId},#{setmealId},#{dishFlavor},#{amount},#{createTime})")
    void insert(ShoppingCart shoppingCart);

    @Delete("delete from sky_take_out.shopping_cart where user_id = #{userId}")
    void cleanShoppingCart(Long userId);

    @Delete("delete from sky_take_out.shopping_cart where id = #{id}")
    void deleteById(Long id);
}
