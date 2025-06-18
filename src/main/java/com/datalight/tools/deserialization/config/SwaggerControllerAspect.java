package com.datalight.tools.deserialization.config;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Order(-1)
public class SwaggerControllerAspect {

    @Pointcut("execution(* springfox.documentation.swagger2.web.Swagger2Controller.getDocumentation(..))")
    public void point(){}

    //@Before("point()")
    @Before(value = "execution(* springfox.documentation.swagger2.web.Swagger2Controller.getDocumentation(..))")
    public void hehe() {
        System.out.println("before ...");
    }

    @After("point()")
    public void haha() {
        System.out.println("After ...");
    }

    @AfterReturning("point()")
    public void xixi() {
        System.out.println("AfterReturning ...");
    }

    @Around("point()")
    public void xxx(ProceedingJoinPoint pj) {
        try {
            System.out.println("Around aaa ...");
            pj.proceed();
            System.out.println("Around bbb ...");
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }
}
