package com.talented.buttie.common.config;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.talented.buttie.common.security.AuthUserArgumentResolver;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;

@EnableWebMvc
@ComponentScan(basePackages = {
    "com.talented.buttie.user.controller",
    "com.talented.buttie.mydata.controller",
    "com.talented.buttie.ledger.controller",
    "com.talented.buttie.simulation.controller",
    "com.talented.buttie.dashboard.controller",
    "com.talented.buttie.quest.controller",
    "com.talented.buttie.catalog.controller",
    "com.talented.buttie.notification.controller",
    "com.talented.buttie.common.exception"
})
@Import(SwaggerConfig.class)
public class ServletConfig implements WebMvcConfigurer {

    private final AuthUserArgumentResolver authUserArgumentResolver;

    public ServletConfig(
        AuthUserArgumentResolver authUserArgumentResolver
    ) {
        this.authUserArgumentResolver = authUserArgumentResolver;
    }

    @Override
    public void addArgumentResolvers(
        List<HandlerMethodArgumentResolver> resolvers
    ){
        resolvers.add(authUserArgumentResolver);
    }



    @Bean
    public MultipartResolver multipartResolver() {
        StandardServletMultipartResolver resolver = new StandardServletMultipartResolver();
        return resolver;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOrigins(
                "http://localhost:5173",
                "https://www.buttie.site",
                "https://buttie.site"
                
            )
            .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .exposedHeaders("Authorization", "X-Identity-Verification-Token")
            .allowCredentials(true);
    }

    @Bean
    public LocalValidatorFactoryBean validator() {
        return new LocalValidatorFactoryBean();
    }

    @Override
    public Validator getValidator() {
        return validator();
    }

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.stream()
            .filter(MappingJackson2HttpMessageConverter.class::isInstance)
            .map(MappingJackson2HttpMessageConverter.class::cast)
            .forEach(converter -> converter.getObjectMapper()
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS));
    }

    //swagger
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("swagger-ui.html")
            .addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**")
            .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

//    resources 밑 정적 파일로 제공 시, 해당 설정 필요
//    @Override
//    public void addResourceHandlers(ResourceHandlerRegistry registry) {
//        registry
//            .addResourceHandler("/resources/**")
//            .addResourceLocations("/resources/");
//    }

//    @Override
//    public void configureViewResolvers(ViewResolverRegistry registry) {
//        InternalResourceViewResolver bean = new InternalResourceViewResolver();
//        bean.setViewClass(JstlView.class);
//        bean.setPrefix("/WEB-INF/views/");
//        bean.setSuffix(".jsp");
//
//        registry.viewResolver(bean);
//    }


}
