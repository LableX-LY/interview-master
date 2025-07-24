package com.xly.interview.master;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc;

@SpringBootApplication
@EnableSwagger2WebMvc
@MapperScan("com.xly.interview.master.mapper")
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
public class InterviewMasterBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(InterviewMasterBackendApplication.class, args);
    }

}
