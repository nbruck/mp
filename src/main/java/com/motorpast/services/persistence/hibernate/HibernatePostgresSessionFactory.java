package com.motorpast.services.persistence.hibernate;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;

import com.motorpast.dataobjects.hibernate.CarDataEntity;
import com.motorpast.dataobjects.hibernate.CarMileageEntity;

public class HibernatePostgresSessionFactory implements HibernateSessionFactory
{
    private final SessionFactory sessionFactory;


    public HibernatePostgresSessionFactory() {
        final AnnotationConfiguration cfg = new AnnotationConfiguration();

        // DATABASE_URL=postgres://user:password@hostname/path
        String databaseUrl = System.getenv("DATABASE_URL");
        String[] dbUrlComps = databaseUrl.split("//");

        // "jdbc:postgresql://localhost/motorpast_scheme"
        final String connectionUrl = "jdbc:postgresql://" + dbUrlComps[1].split("@")[1];

        String[] credentials =   dbUrlComps[1].split("@")[0].split(":");
        // mpsimpleuser
        final String username = credentials[0];
        // password
        final String password = credentials[1];

        cfg.setProperty("hibernate.connection.driver_class", "org.postgresql.Driver");
        cfg.setProperty("hibernate.connection.url", connectionUrl);
        cfg.setProperty("hibernate.connection.username", username);
        cfg.setProperty("hibernate.connection.password", password);
        cfg.setProperty("hibernate.current_session_context_class", "thread");
        cfg.setProperty("hibernate.transaction.factory_class", "org.hibernate.transaction.JDBCTransactionFactory");
        cfg.setProperty("hibernate.show_sql", "false");
        cfg.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        cfg.setProperty("hibernate.hbm2ddl.auto", "create");

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
