package com.example.location.configuration;

import com.example.location.repositories.AccessRepository;
import com.example.location.repositories.AccessRepositoryImpl;
import com.example.location.repositories.HistoryRepository;
import com.example.location.repositories.HistoryRepositoryImpl;
import com.example.location.repositories.LocationRepository;
import com.example.location.repositories.LocationRepositoryImpl;
import com.example.location.repositories.UserRepository;
import com.example.location.repositories.UserRepositoryImpl;
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
    public HistoryRepository historyRepository(JdbcTemplate jdbcTemplate) {

        return new HistoryRepositoryImpl(jdbcTemplate);
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {

        return new JdbcTemplate(dataSource);
    }
}
