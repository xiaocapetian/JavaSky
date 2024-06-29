package com.sky.annotation;


import com.sky.enumeration.OperationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义注解，用于标识某个方法需要进行功能字段自动填充处理
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoFill {

    OperationType value();

    //OperationType是自己写的枚举
    /*内容是
    UPDATE,
    INSERT
    (更新操作,插入操作)* */
    //写了这个后的效果是自己定的注解需要value参数,参数内容是枚举里的内容

    //在切面中就会写加了这个注解会怎样AutoFillAspect
}
