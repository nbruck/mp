package com.motorpast.services.persistence.hibernate;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;

import com.motorpast.dataobjects.hibernate.CarDataEntity;
import com.motorpast.dataobjects.hibernate.CarMileageEntity;

public class HibernatePostgresSessionFactoryDevelopLocal implements HibernateSessionFactory
{
    private final SessionFactory sessionFactory;


    public HibernatePostgresSessionFactoryDevelopLocal() {
        final AnnotationConfiguration cfg = new AnnotationConfiguration();

        cfg.setProperty("hibernate.connection.driver_class", "org.postgresql.Driver");
        cfg.setProperty("hibernate.connection.url", "jdbc:postgresql://localhost/motorpast_scheme");
        cfg.setProperty("hibernate.connection.username", "mpSimpleUser");
        cfg.setProperty("hibernate.connection.password", "4793cD1941e73D453df32bef15");
        cfg.setProperty("hibernate.current_session_context_class", "thread");
        cfg.setProperty("hibernate.transaction.factory_class", "org.hibernate.transaction.JDBCTransactionFactory");
        cfg.setProperty("hibernate.show_sql", "false");
        cfg.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");

        //here the c3po settings are coming
        cfg.setProperty("hibernate.c3p0.max_size", "30");
        cfg.setProperty("hibernate.c3p0.min_size", "10");
        cfg.setProperty("hibernate.c3p0.timeout", "1800");
        cfg.setProperty("hibernate.c3p0.acquire_increment", "1");
        cfg.setProperty("hibernate.c3p0.idle_test_period", "120");
        cfg.setProperty("hibernate.c3p0.max_statements", "60");

        cfg.addAnnotatedClass(CarDataEntity.class);
        cfg.addAnnotatedClass(CarMileageEntity.class); 

        sessionFactory = cfg.buildSessionFactory();
    }


    public final SessionFactory getInstance() {
        return sessionFactory;
    }
}
