package com.powernode.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.HashSet;

/**
 * swagger自动装配类
 */
@Configuration
@EnableConfigurationProperties(SwaggerProperties.class)
public class SwaggerAutoConfig {

    @Autowired
    private SwaggerProperties swaggerProperties;

    @Autowired
    private Environment environment;


    @Bean
    public Docket docket() {
        Boolean flag = true;
        String[] activeProfiles = environment.getActiveProfiles();
        for (String activeProfile : activeProfiles) {
            if (activeProfile.equals("pro")) {
                flag = false;
                break;
            }
        }
        return new Docket(DocumentationType.OAS_30)
                .apiInfo(getApiInfo())
                .enable(flag)
                .select()
                .apis(RequestHandlerSelectors.basePackage(swaggerProperties.getBasePackage())).build();
    }

    private ApiInfo getApiInfo() {
        Contact contact = new Contact(
                swaggerProperties.getName(),
                swaggerProperties.getUrl(),
                swaggerProperties.getEmail()
        );
        ApiInfo apiInfo = new ApiInfo(
                swaggerProperties.getTitle(),
                swaggerProperties.getDescription(),
                swaggerProperties.getVersion(),
                swaggerProperties.getTermsOfServiceUrl(),
                contact,
                swaggerProperties.getLicense(),
                swaggerProperties.getLicenseUrl(),
                new HashSet<>()
        );

        return apiInfo;
    }
}
