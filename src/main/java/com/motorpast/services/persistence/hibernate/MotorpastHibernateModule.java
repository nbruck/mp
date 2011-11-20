package com.motorpast.services.persistence.hibernate;

import org.apache.tapestry5.ioc.annotations.EagerLoad;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.Symbol;

import com.motorpast.additional.MotorApplicationConstants;
import com.motorpast.dataobjects.hibernate.CarDataEntity;
import com.motorpast.services.persistence.PersistenceService;

public class MotorpastHibernateModule
{
    @EagerLoad
    public static HibernateSessionFactory buildHibernateSessionFactory() {
        //return new HibernateMySQLSessionFactory();
        return new HibernatePostgresSessionFactory();
    }

    public static PersistenceService<CarDataEntity> buildPersistenceService(
          final HibernateSessionFactory sessionFactory,
          @Inject @Symbol(value = MotorApplicationConstants.RegistrationDateAttempts) final int initialAttempts,
          @Inject @Symbol(value = MotorApplicationConstants.BlockingTime) final int blockForDays
    ) {
        return new HibernatePersistenceService(sessionFactory, initialAttempts, blockForDays);
    }
}
