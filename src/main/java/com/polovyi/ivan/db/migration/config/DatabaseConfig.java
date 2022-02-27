package com.polovyi.ivan.db.migration.config;

import org.hibernate.jpa.HibernatePersistenceProvider;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@Configuration
public class DatabaseConfig {

    @Bean
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource datasource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.postgresdatasource")
    public DataSource postgresDatasource() {
        return DataSourceBuilder.create().build();
    }

    @Primary
    @Bean
    public EntityManagerFactory mysqlEntityManagerFactory() {
        LocalContainerEntityManagerFactoryBean lem = new LocalContainerEntityManagerFactoryBean();
        lem.setDataSource(datasource());
        lem.setPackagesToScan("com.polovyi.ivan.db.migration.entity.mysql");
        lem.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        lem.setPersistenceProviderClass(HibernatePersistenceProvider.class);
        lem.afterPropertiesSet();
        return lem.getObject();
    }

    @Bean
    public EntityManagerFactory postgresEntityManagerFactory() {
        LocalContainerEntityManagerFactoryBean lem = new LocalContainerEntityManagerFactoryBean();
        lem.setDataSource(postgresDatasource());
        lem.setPackagesToScan("com.polovyi.ivan.db.migration.entity.postgres");
        lem.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        lem.setPersistenceProviderClass(HibernatePersistenceProvider.class);
        lem.afterPropertiesSet();
        return lem.getObject();
    }

    @Primary
    @Bean
    public JpaTransactionManager jpaTransactionManager() {
        JpaTransactionManager jpaTransactionManager = new JpaTransactionManager();
        jpaTransactionManager.setDataSource(datasource());
        jpaTransactionManager.setEntityManagerFactory(mysqlEntityManagerFactory());
        return jpaTransactionManager;
    }
}
