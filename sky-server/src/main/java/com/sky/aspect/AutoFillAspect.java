package com.sky.aspect;

import com.fasterxml.jackson.databind.ser.Serializers;
import com.sky.annotation.AutoFill;
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

import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Aspect
@Component
@Slf4j
public class AutoFillAspect {
    /**
     * 切入点
     */
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointcut() {}

    /**
     * 前置通知 指定切入点
     */
    @Before("autoFillPointcut()")
    public void autoFill(JoinPoint joinPoint){
        log.info("公共字段填充...");
        // 获取操作类型，根据操作类型不同对不同字段赋值
        MethodSignature signature =(MethodSignature) joinPoint.getSignature();//获得方法签名
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class);//获得注解
        OperationType operationType = autoFill.value();//获取操作类型

        //获取实体对象
        Object[] args = joinPoint.getArgs();
        if(args==null || args.length==0){
            return ;
        }
        Object obj = args[0];
        LocalDateTime now = LocalDateTime.now();
        Long id = BaseContext.getCurrentId();
        if(operationType==OperationType.INSERT){
            try{
                Method setCreateUser = obj.getClass().getDeclaredMethod("setCreateUser",Long.class);
                Method setCreateTime = obj.getClass().getDeclaredMethod("setCreateTime",LocalDateTime.class);
                Method setUpdateUser = obj.getClass().getDeclaredMethod("setUpdateUser",Long.class);
                Method setUpdateTime = obj.getClass().getDeclaredMethod("setUpdateTime",LocalDateTime.class);
                setCreateUser.invoke(obj,id);
                setCreateTime.invoke(obj,now);
                setUpdateUser.invoke(obj,id);
                setUpdateTime.invoke(obj,now);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (operationType==OperationType.UPDATE) {
            try{
                Method setUpdateUser = obj.getClass().getDeclaredMethod("setUpdateUser",Long.class);
                Method setUpdateTime = obj.getClass().getDeclaredMethod("setUpdateTime",LocalDateTime.class);
                setUpdateUser.invoke(obj,id);
                setUpdateTime.invoke(obj,now);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
