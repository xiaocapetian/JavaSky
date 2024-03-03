package com.sky.service.impl;


import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.annotation.AutoFill;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.enumeration.OperationType;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.SetmealEnableFailedException;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.DishVO;
import com.sky.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class SetmealServiceImpl implements SetmealService {

    @Autowired
    private SetmealMapper setmealMapper;

    /**新增套餐
     *
     * @param setmealDTO
     */
    @Transactional//事务注解一定要加啊!
    @Override
    public void saveSetmeal(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);
        //向套餐表插入数据
        setmealMapper.insert(setmeal);
        //把回写的套餐id拿到
        Long setmealId = setmeal.getId();


        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        setmealDishes.forEach(
                setmealDish -> setmealDish.setSetmealId(setmealId));

        setmealMapper.insertBatch(setmealDishes);
    }

    @Override
    public PageResult pageQuerry(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageHelper.startPage(setmealPageQueryDTO.getPage(),setmealPageQueryDTO.getPageSize());
        Page<SetmealVO> page = setmealMapper.pageQuery(setmealPageQueryDTO);
        return new PageResult(page.getTotal(),page.getResult());
    }

    /**
     * 条件查询
     * @param setmeal
     * @return
     */
    public List<Setmeal> list(Setmeal setmeal) {
        List<Setmeal> list = setmealMapper.list(setmeal);
        return list;
    }

    /**
     * 根据id查询菜品选项 根据套餐id查询菜品选项
     * @param id
     * @return
     */
    public List<DishItemVO> getDishItemById(Long id) {
        return setmealMapper.getDishItemBySetmealId(id);
    }

    @Transactional
    @Override
    public void deleteBatchSetmeal(List<Long> ids) {
        //判断当前菜品是否能够则除---是否存在起售中的菜品??
        for (Long id : ids) {
            SetmealVO setmealVO = setmealMapper.getById(id);
            if(setmealVO.getStatus() == StatusConstant.ENABLE){
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE );
            }
        }
        setmealMapper.deleteByIds(ids);
        setmealMapper.deleteSetmealDishByIds(ids);
    }


    @Override
    public SetmealVO getById(Long setmealId) {
        SetmealVO setmealVO = setmealMapper.getById(setmealId);
        //List<DishItemVO> dishItemBySetmealId = setmealMapper.getDishItemBySetmealId(setmealId);
        return setmealVO;
    }

    @Transactional
    @Override
    public void update(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);
        //向套餐表插入数据
        setmealMapper.update(setmeal);
        //套餐id拿到
        Long setmealId = setmealDTO.getId();
        List<Long> list = new ArrayList<>();
        list.add(setmealId);
        //把套餐菜品关系表内的数据删除,重写
        setmealMapper.deleteSetmealDishByIds(list);

        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        setmealDishes.forEach(
                setmealDish ->setmealDish.setSetmealId(setmealId) );
        setmealMapper.insertBatch(setmealDishes);
    }

    @Override
    public void startOrStop(Integer status, Long id) {
        //判断套餐里有无停售菜品
        List<Dish> dishBySetmealId = setmealMapper.getDishBySetmealId(id);
        dishBySetmealId.forEach(dish -> {if(dish.getStatus()==StatusConstant.DISABLE){
            throw new SetmealEnableFailedException(MessageConstant.SETMEAL_ENABLE_FAILED);
        }});

        Setmeal setmeal = Setmeal.builder()
                .id(id).status(status).build();
        setmealMapper.update(setmeal);
    }
}
