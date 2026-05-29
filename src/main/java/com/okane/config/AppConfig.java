package com.okane.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@ComponentScan(basePackages = "com.okane")
@EnableJpaRepositories(basePackages = "com.okane.repository")
@EnableTransactionManagement
public class AppConfig {

    @Bean(name = "entityManagerFactory")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean emf =
                new LocalContainerEntityManagerFactoryBean();

        emf.setDataSource(dataSource());
        emf.setPackagesToScan("com.okane.entity");
        emf.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

        Properties props = new Properties();
        props.setProperty("hibernate.dialect",      "org.hibernate.dialect.MySQLDialect");
        props.setProperty("hibernate.hbm2ddl.auto", "update");
        props.setProperty("hibernate.show_sql",     "true");
        props.setProperty("hibernate.format_sql",   "true");
        emf.setJpaProperties(props);

        return emf;
    }

    @Bean
    public JpaTransactionManager transactionManager(
            LocalContainerEntityManagerFactoryBean entityManagerFactory) {

        JpaTransactionManager tx = new JpaTransactionManager();
        tx.setEntityManagerFactory(entityManagerFactory.getObject());
        return tx;
    }

    @Bean
    public DataSource dataSource() {
        HikariDataSource ds = new HikariDataSource();

        ds.setJdbcUrl(System.getenv().getOrDefault(
                "SPRING_DATASOURCE_URL",
                "jdbc:mysql://localhost:3306/okane_db"
        ));
        ds.setUsername(System.getenv().getOrDefault(
                "SPRING_DATASOURCE_USERNAME",
                "root"
        ));
        ds.setPassword(System.getenv().getOrDefault(
                "SPRING_DATASOURCE_PASSWORD",
                ""   // 👈 set SPRING_DATASOURCE_PASSWORD in your .env or OS env
        ));

        ds.setDriverClassName("com.mysql.cj.jdbc.Driver");
        return ds;
    }
}