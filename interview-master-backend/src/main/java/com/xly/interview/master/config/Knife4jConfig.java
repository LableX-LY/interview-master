package com.xly.interview.master.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc;

/**
 * @author X-LY。
 * @version 1.0
 * @createtime 2025/7/3 15:16
 * @description
 **/
@Configuration
@EnableSwagger2WebMvc
public class Knife4jConfig {
    @Bean
    public Docket defaultApi2() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo()) // 使用自定义的 apiInfo 方法
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.xly.interview.master.controller"))
                .paths(PathSelectors.any())
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("InterviewMaster智能面试大师接口文档")
                .description("CInterviewMaster智能面试大师接口文档")
                .version("1.0")
                .termsOfServiceUrl("https://www.example.com/terms") // 服务条款链接
                .contact(new Contact("X-LY。", "https://github.com/LableX-LY", "501028734@qq.com")) // 作者信息
                .license("Apache 2.0") // 许可证名称
                .licenseUrl("https://www.apache.org/licenses/LICENSE-2.0") // 许可证链接
                .build();
    }
}
