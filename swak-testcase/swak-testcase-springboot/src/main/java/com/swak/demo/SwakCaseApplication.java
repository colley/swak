package com.swak.demo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author colley
 */
@SpringBootApplication
@MapperScan("com.swak.demo.mapper")
public class SwakCaseApplication {
    public static void main(String[] args) {
        SpringApplication.run(SwakCaseApplication.class, args);
    }
}

