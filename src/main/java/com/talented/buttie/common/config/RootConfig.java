package com.talented.buttie.common.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.talented.buttie.ledger.domain.ExpenseCategory;
import com.talented.buttie.simulation.dto.response.recommendation.FinancialRecommendationResponse;
import com.talented.buttie.catalog.dto.response.PolicyResponse;
import com.talented.buttie.simulation.dto.response.recommendation.IncomeJobSearchResponse;
import com.talented.buttie.user.dto.response.auth.VerifiedCustomer;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.time.Duration;
import java.util.Optional;
import javax.sql.DataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.flywaydb.core.Flyway;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableTransactionManagement
@EnableScheduling
@PropertySource({"classpath:/application.properties"})
@ComponentScan(basePackages = {
    "com.talented.buttie.user.service",
    "com.talented.buttie.mydata.service",
    "com.talented.buttie.mydata.client",
    "com.talented.buttie.mydata.facade",
    "com.talented.buttie.ledger.service",
    "com.talented.buttie.simulation.service",
    "com.talented.buttie.dashboard.service",
    "com.talented.buttie.quest.service",
    "com.talented.buttie.catalog.service",
    "com.talented.buttie.catalog.elasticsearch",
    "com.talented.buttie.catalog.external",
    "com.talented.buttie.notification.service",
    "com.talented.buttie.common.security",
    "com.talented.buttie.common.util"
})
@Import({
    RedisConfig.class
})
@MapperScan(basePackages = {
    "com.talented.buttie.catalog.mapper",
    "com.talented.buttie.dashboard.mapper",
    "com.talented.buttie.ledger.mapper",
    "com.talented.buttie.mydata.mapper",
    "com.talented.buttie.notification.mapper",
    "com.talented.buttie.quest.mapper",
    "com.talented.buttie.simulation.mapper",
    "com.talented.buttie.user.mapper"
})
public class RootConfig {

    @Value("${jdbc.driver}")
    String driver;
    @Value("${jdbc.url}")
    String url;
    @Value("${jdbc.username}")
    String username;
    @Value("${jdbc.password}")
    String password;

    @Value("${hikari.maximum-pool-size}")
    int maximumPoolSize;
    @Value("${hikari.minimum-idle}")
    int minimumIdle;
    @Value("${hikari.connection-timeout}")
    long connectionTimeout;
    @Value("${hikari.idle-timeout}")
    long idleTimeout;
    @Value("${hikari.max-lifetime}")
    long maxLifetime;
    @Value("${hikari.validation-timeout}")
    long validationTimeout;

    @Bean
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName(driver);
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        config.setMaximumPoolSize(maximumPoolSize);
        config.setMinimumIdle(minimumIdle);
        config.setConnectionTimeout(connectionTimeout);
        config.setIdleTimeout(idleTimeout);
        config.setMaxLifetime(maxLifetime);
        config.setValidationTimeout(validationTimeout);
        config.setPoolName("buttie-hikari");
        config.setRegisterMbeans(true);

        return new HikariDataSource(config);
    }

    // Flyway
    @Bean(initMethod = "migrate")
    public Flyway flyway() {
        return Flyway.configure()
            .dataSource(dataSource())
            .locations("classpath:db/migration")
            .baselineOnMigrate(true)
            .load();
    }

    @Bean

    public static PropertySourcesPlaceholderConfigurer
    propertySourcesPlaceholderConfigurer() {
        PropertySourcesPlaceholderConfigurer configurer =
            new PropertySourcesPlaceholderConfigurer();
        configurer.setIgnoreUnresolvablePlaceholders(false);
        return configurer;
    }

    @Bean
    @DependsOn("flyway")
    public SqlSessionFactory sqlSessionFactory(ApplicationContext applicationContext) throws Exception {
        SqlSessionFactoryBean sqlSessionFactory = new SqlSessionFactoryBean();
        sqlSessionFactory.setConfigLocation(applicationContext.getResource("classpath:/mybatis-config.xml"));
        sqlSessionFactory.setDataSource(dataSource());
        sqlSessionFactory.setMapperLocations(applicationContext.getResources("classpath*:mapper/**/*.xml"));
        return sqlSessionFactory.getObject();
    }

    @Bean
    public DataSourceTransactionManager transactionManager() {
        return new DataSourceTransactionManager(dataSource());
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public RestTemplate restTemplate() {
        // ponytail: RegionCodeCache가 기동 시(@PostConstruct) 외부 API를 동기 호출하므로,
        // 타임아웃이 없으면 외부 API가 느릴 때 앱 기동 자체가 무한정 멈춘다.
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5_000);
        factory.setReadTimeout(10_000);
        return new RestTemplate(factory);
    }

    @Bean
    public Cache<String, VerifiedCustomer> identityVerificationCache() {
        return Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterWrite(Duration.ofMinutes(30))
            .build();
    }

    @Bean
    public Cache<String, Optional<ExpenseCategory>> merchantCategoryCache() {
        return Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterWrite(Duration.ofMinutes(30))
            .build();
    }

    @Bean
    public Cache<String, FinancialRecommendationResponse> financialRecommendationCache() {
        return Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterWrite(Duration.ofHours(6))
            .build();
    }

    @Bean
    public Cache<String, java.util.List<PolicyResponse>> policyRecommendationCache() {
        return Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterWrite(Duration.ofHours(6))
            .build();
    }

    @Bean
    public Cache<String, IncomeJobSearchResponse> incomeJobSearchCache() {
        return Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterWrite(Duration.ofHours(1))
            .build();
    }

}
