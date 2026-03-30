package org.sakaiproject.component.app.scheduler.config;


import org.hibernate.SessionFactory;
import org.sakaiproject.messaging.api.repository.UserNotificationRepository;
import org.sakaiproject.messaging.impl.repository.UserNotificationRepositoryImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.orm.hibernate5.LocalSessionFactoryBuilder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@EnableTransactionManagement
public class NotificationCleanUpJobConfig {


    @Bean
    public DataSource dataSource() {
        return new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .build();
    }

    @Bean
    public LocalSessionFactoryBean sessionFactory() {
        LocalSessionFactoryBean factory = new LocalSessionFactoryBean();
        factory.setDataSource(dataSource());
        factory.setPackagesToScan("org.sakaiproject.messaging.api.model", "org.sakaiproject.springframework.data", "org.sakaiproject.messaging.impl.repository");

        Properties props = new Properties();
        props.put("hibernate.hbm2ddl.auto", "create-drop");
        factory.setHibernateProperties(props);
        return factory;
    }


    @Bean
    public PlatformTransactionManager transactionManager() {
        return new HibernateTransactionManager(sessionFactory().getObject());
    }



    @Bean
    public UserNotificationRepository userNotificationRepository() {
        UserNotificationRepositoryImpl userNotificationRepository = new UserNotificationRepositoryImpl();
        userNotificationRepository.setSessionFactory(sessionFactory().getObject());
        return userNotificationRepository;
    }
}
