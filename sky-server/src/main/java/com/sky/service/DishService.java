package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;
import org.springframework.context.annotation.Bean;

import java.util.List;

public interface DishService {
    /**
     * 新增菜品和对应的口味
     */
    public void saveWithFlavor(DishDTO dishDTO);

    PageResult pageQuerry(DishPageQueryDTO dishPageQueryDTO);

    void deletebatch(List<Long> ids);

    DishVO getByIdWithFlavor(Long id);

    void updateWithFlavor(DishDTO dishDTO);

    List<Dish> getByCategoryId(Long catgeoryId);

    /**
     * 条件查询菜品和口味
     * @param dish
     * @return
     */
    List<DishVO> listWithFlavor(Dish dish);

    void startOrStop(Integer status, Long id);
}
