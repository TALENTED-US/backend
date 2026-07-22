package com.talented.buttie.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewResolverRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;

@EnableWebMvc
@ComponentScan(basePackages = {
    "com.talented.buttie.user.controller",
    "com.talented.buttie.mydata.controller",
    "com.talented.buttie.account.controller",
    "com.talented.buttie.ledger.controller",
    "com.talented.buttie.snapshot.controller",
    "com.talented.buttie.simulation.controller",
    "com.talented.buttie.quest.controller",
    "com.talented.buttie.catalog.controller",
    "com.talented.buttie.notification.controller",
    "com.talented.buttie.batch.controller",
    "com.talented.buttie.common.exception"
})
@Import(SwaggerConfig.class)
public class ServletConfig implements WebMvcConfigurer {

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
                "https://프론트엔드-도메인" //TODO FE Domain 수정
            )
            .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true);
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
