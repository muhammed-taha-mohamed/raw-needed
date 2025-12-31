package com.rawneeded.config;

import com.rawneeded.security.ScreenPermissionEvaluator;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;

@Configuration
@AllArgsConstructor
public class MethodSecurityConfig {

    private final ScreenPermissionEvaluator screenPermissionEvaluator;

    @Bean
    public MethodSecurityExpressionHandler methodSecurityExpressionHandler() {
        DefaultMethodSecurityExpressionHandler expressionHandler = new DefaultMethodSecurityExpressionHandler();
        expressionHandler.setPermissionEvaluator(screenPermissionEvaluator);
        return expressionHandler;
    }
}
