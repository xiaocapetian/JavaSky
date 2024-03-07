package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface SetmealMapper {

    /**
     * 根据分类id查询套餐的数量
     * @param id
     * @return
     */
    @Select("select count(id) from sky_take_out.setmeal where category_id = #{categoryId}")
    Integer countByCategoryId(Long id);

    /**
     * 往Setmeal套餐表里加
     * @param setmeal
     */
    @AutoFill(value = OperationType.INSERT)
    void insert(Setmeal setmeal);

    /**
     * 往setmealDishes关系表里加
     * @param setmealDishes
     */
    void insertBatch(List<SetmealDish> setmealDishes);


    Page<SetmealVO> pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);

    /**
     * 动态条件查询套餐
     * @param setmeal
     * @return
     */
    List<Setmeal> list(Setmeal setmeal);

    /**
     * 根据套餐id查询菜品选项
     * @param setmealId
     * @return
     */
    @Select("select sd.name, sd.copies, d.image, d.description " +
            "from sky_take_out.setmeal_dish sd left join sky_take_out.dish d on sd.dish_id = d.id " +
            "where sd.setmeal_id = #{setmealId}")
    List<DishItemVO> getDishItemBySetmealId(Long setmealId);

    /**
     *
     */
    @Select("select d.*" +
            "from sky_take_out.dish d left join sky_take_out.setmeal_dish sd on sd.dish_id = d.id " +
            "where sd.setmeal_id = #{setmealId}")
    List<Dish> getDishBySetmealId(Long setmealId);
    /**
     * 根据套餐id查询套餐
     * @param id
     * @return
     */
    @Select("select sm.*,cat.status as categoryName from sky_take_out.setmeal sm " +
            "left join sky_take_out.category cat on sm.category_id = cat.id " +
            "where sm.id = #{id}")
    SetmealVO getById(Long id);

    /**
     * 根据套餐id删除套餐
     * @param ids
     */
    void deleteByIds(List<Long> ids);
    /**
     * 根据套餐id删除套餐dish关联表
     * @param ids
     */
    void deleteSetmealDishByIds(List<Long> ids);

    @AutoFill(value = OperationType.UPDATE)
    void update(Setmeal setmeal);

    /**
     * 根据条件统计套餐数量
     * @param map
     * @return
     */
    Integer countByMap(Map map);



}
