package com.example.demo.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * Database connection pool and JPA performance configuration.
 * 
 * Features:
 * - HikariCP connection pooling (fastest pool available)
 * - Optimized pool sizing based on formula: connections = ((core_count * 2) + effective_spindle_count)
 * - Query performance hints
 * - Batch processing
 * - Second-level cache support
 */
@Configuration
@EnableTransactionManagement
@Slf4j
public class DatabaseConfig {

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUsername;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    @Value("${spring.jpa.hibernate.ddl-auto:update}")
    private String ddlAuto;

    /**
     * HikariCP DataSource - High-performance connection pool.
     * HikariCP is the fastest, most reliable connection pool for Java.
     */
    @Bean
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        
        // Connection settings
        config.setJdbcUrl(dbUrl);
        config.setUsername(dbUsername);
        config.setPassword(dbPassword);
        config.setDriverClassName("org.postgresql.Driver");  // PostgreSQL driver
        
        // Pool sizing (for typical server with 4-8 cores)
        config.setMinimumIdle(5);                    // Min connections
        config.setMaximumPoolSize(20);               // Max connections
        config.setConnectionTimeout(30000);          // 30 seconds
        config.setIdleTimeout(600000);               // 10 minutes
        config.setMaxLifetime(1800000);              // 30 minutes
        config.setLeakDetectionThreshold(60000);     // 60 seconds (detect connection leaks)
        
        // Performance optimizations
        config.setAutoCommit(true);
        config.setConnectionTestQuery("SELECT 1");
        config.setPoolName("HikariPool-SocialApp");
        
        // Cache prepared statements
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("useLocalSessionState", "true");
        config.addDataSourceProperty("rewriteBatchedStatements", "true");
        config.addDataSourceProperty("cacheResultSetMetadata", "true");
        config.addDataSourceProperty("cacheServerConfiguration", "true");
        config.addDataSourceProperty("elideSetAutoCommits", "true");
        config.addDataSourceProperty("maintainTimeStats", "false");
        
        HikariDataSource dataSource = new HikariDataSource(config);
        
        log.info("HikariCP initialized - Pool size: {}-{}, Leak detection: {}ms",
                config.getMinimumIdle(), config.getMaximumPoolSize(), 
                config.getLeakDetectionThreshold());
        
        return dataSource;
    }

    /**
     * JPA EntityManagerFactory with performance tuning.
     */
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("com.example.demo.entity");
        em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        em.setJpaProperties(hibernateProperties());
        
        log.info("JPA EntityManagerFactory configured with performance optimizations");
        return em;
    }

    /**
     * Hibernate performance properties.
     */
    private Properties hibernateProperties() {
        Properties properties = new Properties();
        
        // Basic settings (ddl-auto from config: use 'validate' in prod, 'update' in dev)
        properties.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");  // PostgreSQL
        properties.setProperty("hibernate.hbm2ddl.auto", ddlAuto);
        properties.setProperty("hibernate.show_sql", "false");
        properties.setProperty("hibernate.format_sql", "false");
        
        // Performance optimizations
        properties.setProperty("hibernate.jdbc.batch_size", "25");              // Batch inserts/updates
        properties.setProperty("hibernate.order_inserts", "true");              // Order batched inserts
        properties.setProperty("hibernate.order_updates", "true");              // Order batched updates
        properties.setProperty("hibernate.jdbc.batch_versioned_data", "true");  // Batch versioned data
        properties.setProperty("hibernate.jdbc.fetch_size", "50");              // Fetch size hint
        
        // Query optimizations
        properties.setProperty("hibernate.max_fetch_depth", "3");               // Limit join depth
        properties.setProperty("hibernate.default_batch_fetch_size", "16");     // Batch fetch associations
        properties.setProperty("hibernate.query.in_clause_parameter_padding", "true"); // Optimize IN clauses
        properties.setProperty("hibernate.query.fail_on_pagination_over_collection_fetch", "true");
        properties.setProperty("hibernate.query.plan_cache_max_size", "2048");  // Query plan cache
        properties.setProperty("hibernate.query.plan_parameter_metadata_max_size", "128");
        
        // Connection & statement optimizations
        properties.setProperty("hibernate.connection.provider_disables_autocommit", "true");
        properties.setProperty("hibernate.jdbc.lob.non_contextual_creation", "true");
        
        // Statistics (disable in production for better performance)
        properties.setProperty("hibernate.generate_statistics", "false");
        
        // Second-level cache (disabled for now - requires hibernate-jcache dependency)
        properties.setProperty("hibernate.cache.use_second_level_cache", "false");
        properties.setProperty("hibernate.cache.use_query_cache", "false");
        
        log.debug("Hibernate properties configured - Batch size: 25, Fetch size: 50");
        return properties;
    }

    /**
     * Transaction manager.
     */
    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory);
        return transactionManager;
    }
}
