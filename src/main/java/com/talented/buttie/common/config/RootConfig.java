package com.talented.buttie.common.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.talented.buttie.user.dto.response.auth.VerifiedCustomer;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.time.Duration;
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
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableTransactionManagement
@PropertySource({"classpath:/application.properties"})
@ComponentScan(basePackages = {
    "com.talented.buttie.user.service",
    "com.talented.buttie.mydata.service",
    "com.talented.buttie.mydata.redis",
    "com.talented.buttie.mydata.client",
    "com.talented.buttie.account.service",
    "com.talented.buttie.ledger.service",
    "com.talented.buttie.snapshot.service",
    "com.talented.buttie.simulation.service",
    "com.talented.buttie.dashboard.service",
    "com.talented.buttie.quest.service",
    "com.talented.buttie.catalog.service",
    "com.talented.buttie.catalog.elasticsearch",
    "com.talented.buttie.notification.service",
    "com.talented.buttie.common.security",
    "com.talented.buttie.common.util"
})
@Import({
    RedisConfig.class
})
@MapperScan(basePackages = {"com.talented.buttie"})
public class RootConfig {

    @Value("${jdbc.driver}")
    String driver;
    @Value("${jdbc.url}")
    String url;
    @Value("${jdbc.username}")
    String username;
    @Value("${jdbc.password}")
    String password;

    @Bean
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName(driver);
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);

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
        return new RestTemplate();
    }

    @Bean
    public Cache<String, VerifiedCustomer> identityVerificationCache() {
        return Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterWrite(Duration.ofMinutes(30))
            .build();
    }

}
