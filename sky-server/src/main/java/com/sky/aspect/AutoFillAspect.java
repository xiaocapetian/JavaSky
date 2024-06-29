package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 *自定义切面，实现公共字段自动填充处理逻辑
 * 自动填充时间和处理人
 */

@Aspect
@Component
@Slf4j
public class AutoFillAspect {
    /**
     * 切入点
     * 拦截条件:com.sky.mapper.下的所有的类的所有的方法所有的参数 &&且 加上了注解@AutoFill
     */
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    //对哪些类的哪些方法进行拦截
    public void autoFillPointCut(){}

    /**
     * 通知  前置通知
     */
    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        //joinPoint是传进方法里的东西,它字面意思是连接点

        log.info("开始进行公共字段自动填充");
        //获取到当前被拦截方法上的数据库操作类型
        MethodSignature signature = (MethodSignature)joinPoint.getSignature();//signature意思是签名
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class);//获得方法上是注解对象
        OperationType operationType = autoFill.value();//获取它注解的值

        //获取到当前被拦裁的方法的参数--实体对象
        Object[] args = joinPoint.getArgs();//获取方法的参数
        if(args==null|| args.length==0){
            return;
        }
        Object entity = args[0];//约定:第一个参数就是要动的对象

        //准备赋值的数据
        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();
        //根据当前不同的操作类型,为对应的属性通过反射来赋值
        if(operationType == OperationType.INSERT){
            //为4个公共字殷赋值
            Method setCreateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
            Method serCreateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
            Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
            Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

            //通过反射为对象属性赋值
            setCreateTime.invoke(entity,now);
            serCreateUser.invoke(entity,currentId);
            setUpdateTime.invoke(entity,now);
            setUpdateUser.invoke(entity,currentId);

        }else if(operationType == OperationType.UPDATE){
            //为2个公共字段赋值
            Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
            Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
            //通过反射为对象属性赋值
            setUpdateTime.invoke(entity,now);
            setUpdateUser.invoke(entity,currentId);

        }
    }
}
