package com.example.location.annotation;

import com.example.location.dto.AccessDTO;
import com.example.location.dto.UserLocationDTO;
import com.example.location.entities.Location;
import com.example.location.services.UserService;
import com.example.location.util.BadRequestException;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Aspect
@Component
@AllArgsConstructor
@Log4j2
public class AuthorizationAspect {

    private final UserService userService;
    private final ParameterNameDiscoverer parameterNameDiscoverer = new LocalVariableTableParameterNameDiscoverer();

    @Before("@annotation(AuthorizationRequired) && args(uid,..)")
    public void checkAuthorization(Long uid) {

        if (uid == null || uid <= 0) throw new BadRequestException();
    }

    @Around("@annotation(AuthorizationOwner)")
    public Object checkOwner(ProceedingJoinPoint joinPoint) throws Throwable {

        Object[] args = joinPoint.getArgs();

        Long uid = null;
        Long lid = null;

        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        String[] parameterNames = parameterNameDiscoverer.getParameterNames(methodSignature.getMethod());
        if (parameterNames != null) {
            for (int i = 0; i < parameterNames.length; i++) {
                if ("uid".equals(parameterNames[i])) {
                    uid = (Long) args[i];
                } else if ("lid".equals(parameterNames[i])) {
                    lid = (Long) args[i];
                } else if (args[i] instanceof Location) {
                    Location location = (Location) args[i];
                    lid = location.getLid();
                } else if (args[i] instanceof AccessDTO) {
                    AccessDTO accessDTO = (AccessDTO) args[i];
                    lid = accessDTO.getLid();
                } else if (args[i] instanceof UserLocationDTO) {
                    UserLocationDTO userLocationDTO = (UserLocationDTO) args[i];
                    lid = userLocationDTO.getLid();
                }
            }
        }

        Boolean authorized = userService.authorizeOwnerOrAdmin(uid, lid).join();

        if (Boolean.FALSE.equals(authorized)) {
            return CompletableFuture.completedFuture(ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied"));
        }

        return joinPoint.proceed();
    }
}

