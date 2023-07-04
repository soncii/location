package com.example.location.configuration;

import com.example.location.repositories.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class AppConfig {

    @Bean
    public UserRepository userRepository(JdbcTemplate jdbcTemplate) {

        return new UserRepositoryImpl(jdbcTemplate);
    }

    @Bean
    public LocationRepository locationRepository(JdbcTemplate jdbcTemplate) {

        return new LocationRepositoryImpl(jdbcTemplate);
    }

    @Bean
    public AccessRepository accessRepository(JdbcTemplate jdbcTemplate) {

        return new AccessRepositoryImpl(jdbcTemplate);
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {

        return new JdbcTemplate(dataSource);
    }
}
