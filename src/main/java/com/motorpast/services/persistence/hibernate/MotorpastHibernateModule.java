package com.motorpast.services.persistence.hibernate;

import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.ioc.annotations.EagerLoad;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.slf4j.Logger;

import com.motorpast.additional.MotorApplicationConstants;
import com.motorpast.dataobjects.hibernate.CarDataEntity;
import com.motorpast.services.persistence.PersistenceService;

public class MotorpastHibernateModule {
    @EagerLoad
    public static HibernateSessionFactory buildHibernateSessionFactory(
            @Symbol(SymbolConstants.PRODUCTION_MODE) final boolean productionMode,
            final Logger logger
    ) {
        if (productionMode) {
            logger.info("it's live, I'll take the real-live-sessionfactory");

            return new HibernatePostgresSessionFactory();
        } else {
            logger.debug("developing, I'll take the local postgres db");

            return new HibernatePostgresSessionFactoryDevelopLocal();
        }
    }

    public static PersistenceService<CarDataEntity> buildPersistenceService(
            final HibernateSessionFactory sessionFactory,
            @Inject @Symbol(value = MotorApplicationConstants.RegistrationDateAttempts) final int initialAttempts,
            @Inject @Symbol(value = MotorApplicationConstants.BlockingTime) final int blockForDays
    ) {
        return new HibernatePersistenceService(sessionFactory, initialAttempts, blockForDays);
    }
}
