package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import org.springframework.context.annotation.Bean;

import java.util.List;

public interface DishService {
    /**
     * 新增菜品和对应的口味
     */
    public void saveWithFlavor(DishDTO dishDTO);

    PageResult pageQuerry(DishPageQueryDTO dishPageQueryDTO);

    void deletebatch(List<Long> ids);
}
