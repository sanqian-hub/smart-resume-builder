package com.smart.resume;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.smart.resume.mapper")
public class SmartResumeApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartResumeApplication.class, args);
    }

}
