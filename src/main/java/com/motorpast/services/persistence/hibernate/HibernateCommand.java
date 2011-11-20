package com.motorpast.services.persistence.hibernate;

import org.hibernate.Session;

import com.motorpast.services.persistence.MotorpastPersistenceException;

abstract class HibernateCommand<T>
{
    private final Session session;


    HibernateCommand(final Session session) {
        this.session = session;
    }

    final Session getSession() {
        return session;
    }

    abstract T execute() throws MotorpastPersistenceException;
}
