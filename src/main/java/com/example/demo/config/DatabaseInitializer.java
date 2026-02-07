package com.example.demo.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Database initializer that creates the database if it doesn't exist.
 * This runs before any beans are created (including DataSource).
 */
@Component
@Slf4j
public class DatabaseInitializer implements BeanFactoryPostProcessor, EnvironmentAware {

    private Environment environment;

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        String dbUrl = environment.getProperty("spring.datasource.url");
        String dbUsername = environment.getProperty("spring.datasource.username");
        String dbPassword = environment.getProperty("spring.datasource.password");
        
        initialize(dbUrl, dbUsername, dbPassword);
    }

    private void initialize(String dbUrl, String dbUsername, String dbPassword) {
        // Extract database name from URL (e.g., jdbc:postgresql://localhost:5432/socialhub)
        String databaseName = extractDatabaseName(dbUrl);
        
        if (databaseName == null) {
            log.warn("Could not extract database name from URL: {}", dbUrl);
            return;
        }

        // Connect to default 'postgres' database to check/create our database
        String postgresUrl = dbUrl.replace("/" + databaseName, "/postgres");
        
        try {
            if (!databaseExists(postgresUrl, databaseName, dbUsername, dbPassword)) {
                log.info("Database '{}' does not exist. Creating it...", databaseName);
                createDatabase(postgresUrl, databaseName, dbUsername, dbPassword);
                log.info("Database '{}' created successfully!", databaseName);
            } else {
                log.info("Database '{}' already exists.", databaseName);
            }
        } catch (SQLException e) {
            log.error("Failed to initialize database: {}", e.getMessage());
            // Don't throw exception - let Spring Boot handle the connection error
        }
    }

    private String extractDatabaseName(String url) {
        // Extract database name from jdbc:postgresql://host:port/dbname
        int lastSlash = url.lastIndexOf('/');
        if (lastSlash > 0 && lastSlash < url.length() - 1) {
            String dbPart = url.substring(lastSlash + 1);
            // Remove query parameters if any
            int queryStart = dbPart.indexOf('?');
            return queryStart > 0 ? dbPart.substring(0, queryStart) : dbPart;
        }
        return null;
    }

    private boolean databaseExists(String postgresUrl, String databaseName, String dbUsername, String dbPassword) throws SQLException {
        try (Connection conn = DriverManager.getConnection(postgresUrl, dbUsername, dbPassword);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT 1 FROM pg_database WHERE datname = '" + databaseName + "'")) {
            return rs.next();
        }
    }

    private void createDatabase(String postgresUrl, String databaseName, String dbUsername, String dbPassword) throws SQLException {
        try (Connection conn = DriverManager.getConnection(postgresUrl, dbUsername, dbPassword);
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE DATABASE " + databaseName);
        }
    }
}
