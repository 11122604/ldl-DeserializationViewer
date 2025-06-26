package com.datalight.tools.deserialization.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.service.Parameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableSwagger2
public class SwaggerConfig {

    private static final Logger logger = LoggerFactory.getLogger(SwaggerConfig.class);

    private BeanFactory beanFactory;

    private static final String API_GROUP_NAME = "cdp";
    private static final String API_TITLE = "DeserializationViewer API";
    private static final String API_DESCRIPTION = "缓存反序列化查看";
    private static final String APIS_PACKAGE = "com.datalight.tools.deserialization.controller";

	@Value("${swagger.switch:true}")
	private boolean swaggerSwitch;

    @Bean
    @SuppressWarnings({"unchecked"})
    public Docket testApi() {
        logger.info("启动Swagger");
        ParameterBuilder tokenPar = new ParameterBuilder();
        List<Parameter> pars = new ArrayList<>();
        tokenPar.name("X-Auth").description("令牌").modelRef(new ModelRef("string")).parameterType("header").required(false).build();
        pars.add(tokenPar.build());
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName(API_GROUP_NAME)
                .select()
                .apis(RequestHandlerSelectors.basePackage(APIS_PACKAGE))
                //.build().globalOperationParameters(pars)
                .build()
                .apiInfo(testApiInfo()).enable(swaggerSwitch)
                ;
    }

    private ApiInfo testApiInfo() {
        return new ApiInfoBuilder()
                .title(API_TITLE)
                .description(API_DESCRIPTION)
                .build()
                ;
    }
    
}
