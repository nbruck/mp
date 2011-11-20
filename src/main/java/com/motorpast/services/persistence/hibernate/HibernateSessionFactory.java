package com.motorpast.services.persistence.hibernate;

import org.hibernate.SessionFactory;

public interface HibernateSessionFactory
{
    SessionFactory getInstance();
}
