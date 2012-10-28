package com.motorpast.services.persistence.hibernate;

import org.hibernate.HibernateException;
import org.hibernate.StaleObjectStateException;
import org.hibernate.Transaction;
import org.slf4j.Logger;

import com.motorpast.services.persistence.MotorpastPersistenceException;
import com.motorpast.services.persistence.MotorpastPersistenceException.PersistenceErrorCode;

public class MotorPastHibernateManager
{
    final private Logger logger;

    MotorPastHibernateManager(final Logger logger) {
        this.logger = logger;
    }

    /**
     * wrapping all hibernate calls into a transaction
     * @throws MotorpastPersistenceException bubble up for later handling
     */
    @SuppressWarnings("unchecked")
    <R> R executeCommand(HibernateCommand<?> command) throws MotorpastPersistenceException {
        Transaction tx = null;
        Object result = null;

        try {
            tx = command.getSession().beginTransaction();

            result = command.execute();

            tx.commit();
        } catch(StaleObjectStateException e) {
            logger.warn("StaleObjectStateException: two clients tried to modify same car(id) which can be suspicious");
            logger.info("StaleObjectStateException: some values for councurrent writing access: entityname=" + e.getEntityName() +
                    " , identifier=" + e.getIdentifier());
            // concurrent access to one object, maybe some database-info statements?
            throw new RuntimeException(e);
        } catch(RuntimeException runtimeException) {
            try {
                logger.error("runtime-exception... try to rollback");

                if(tx != null) {
                    tx.rollback();
                }
            } catch(HibernateException hibernateException) {
                logger.error("Transaction could not be rolled back!");

                throw hibernateException;
            }

            if(runtimeException.getCause() != null && runtimeException.getCause() instanceof StaleObjectStateException) {
                throw new MotorpastPersistenceException(PersistenceErrorCode.concurrent_writing_access);
            }

            throw runtimeException;
        } finally {
            if(command.getSession() != null && command.getSession().isOpen()) {
                // in case of current_session_context_class = thread
                // session is automatically closed by tx.commit() or tx.rollback()
                command.getSession().close(); 
            }
        }

        return (R)result;
    }

    /**
     * wrapping all hibernate calls into a transaction
     * @throws MotorpastPersistenceException bubble up for later handling
     */
    void wrapCommandIntoTxVoid(HibernateCommand<?> command) throws MotorpastPersistenceException {
        Transaction tx = null;

        try {
            tx = command.getSession().beginTransaction();

            command.execute();

            tx.commit();
        } catch(StaleObjectStateException e) {
            logger.warn("StaleObjectStateException: two clients tried to modify same car(id) which can be suspicious");
            logger.info("StaleObjectStateException: some values for councurrent writing access: entityname=" + e.getEntityName() +
                    " , identifier=" + e.getIdentifier());
            // concurrent access to one object, maybe some database-info statements?
            throw new RuntimeException(e);
        } catch(RuntimeException runtimeException) {
            try {
                logger.error("runtime-exception... try to rollback");

                if(tx != null) {
                    tx.rollback();
                }
            } catch(HibernateException hibernateException) {
                logger.error("Transaction could not be rolled back!");

                throw hibernateException;
            }

            if(runtimeException.getCause() != null && runtimeException.getCause() instanceof StaleObjectStateException) {
                throw new MotorpastPersistenceException(PersistenceErrorCode.concurrent_writing_access);
            }

            throw runtimeException;
        } finally {
            if(command.getSession() != null && command.getSession().isOpen()) {
                // in case of current_session_context_class = thread
                // session is automatically closed by tx.commit() or tx.rollback()
                command.getSession().close(); 
            }
        }
    }
}
