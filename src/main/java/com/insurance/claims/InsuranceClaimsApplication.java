package com.insurance.claims;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 保险理赔规则引擎系统主启动类
 * 
 * @author AI Assistant
 * @since 2025-06-26
 */
@SpringBootApplication
public class InsuranceClaimsApplication {

    public static void main(String[] args) {
        SpringApplication.run(InsuranceClaimsApplication.class, args);
        System.out.println("\n=== 保险理赔规则引擎系统已启动 ===");
        System.out.println("访问地址: http://localhost:8080");
        System.out.println("API文档: http://localhost:8080/swagger-ui.html");
        System.out.println("====================================\n");
    }
}
