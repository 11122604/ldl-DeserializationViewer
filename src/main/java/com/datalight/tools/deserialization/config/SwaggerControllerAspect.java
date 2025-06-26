package com.datalight.tools.deserialization.config;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Order(-1)
public class SwaggerControllerAspect {

    private static final Logger logger = LoggerFactory.getLogger(SwaggerControllerAspect.class);

    @Pointcut("execution(* springfox.documentation.swagger2.web.Swagger2Controller.getDocumentation(..))")
    public void point(){}

    //@Before("point()")
    @Before(value = "execution(* springfox.documentation.swagger2.web.Swagger2Controller.getDocumentation(..))")
    public void hehe() {
        logger.debug("before ...");
    }

    @After("point()")
    public void haha() {
        logger.debug("After ...");
    }

    @AfterReturning("point()")
    public void xixi() {
        logger.debug("AfterReturning ...");
    }

    @Around("point()")
    public void xxx(ProceedingJoinPoint pj) {
        try {
            logger.debug("Around aaa ...");
            pj.proceed();
            logger.debug("Around bbb ...");
        } catch (Throwable throwable) {
            logger.error("AOP执行异常", throwable);
        }
    }
}
