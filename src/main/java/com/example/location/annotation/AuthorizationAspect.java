package com.example.location.annotation;

import com.example.location.util.BadRequestException;
import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Log4j2
public class AuthorizationAspect {

    @Before("@annotation(AuthorizationRequired)")
    public void checkAuthorization(JoinPoint joinPoint) {

        Object[] args = joinPoint.getArgs();
        for (Object arg : args) {
            if (arg instanceof String) {
                String uid = (String) arg;
                if (uid.equals("empty")) {
                    log.warn("Invalid or empty UID header received");
                    throw new BadRequestException();
                }
            }
        }
    }
}

