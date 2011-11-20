package com.motorpast.services.persistence;

import java.util.Date;

import org.apache.tapestry5.ioc.test.TestBase;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.motorpast.dataobjects.hibernate.CarDataEntity;
import com.motorpast.services.persistence.hibernate.HibernatePersistenceService;
import com.motorpast.services.persistence.hibernate.HibernateMySQLSessionFactory;

/**
 * I hope it will simulate n threads which parallel doing requests to hibernate-system or mongo or simpledb
 */
public class HibernatePersistenceServiceTestFillDatabase extends TestBase
{
    private PersistenceService<CarDataEntity> persistenceService;


    @BeforeTest
    public void init() {
        persistenceService = new HibernatePersistenceService(new HibernateMySQLSessionFactory(), 2, 90);
    }


    @Test
    public void persistenceMultiHibernate() {
        long start = System.currentTimeMillis();

        for(int i = 0; i < 1000000; i++) {
            try {
                persistenceService.saveNewCarData(
                    new CarDataEntity(
                        "" + i,
                        i,
                        new Date(),
                        null,
                        null,
                        2,
                        false
                    ),
                    "123.456.789.000",
                    "fake user agent"
                );
            } catch (MotorpastPersistenceException e1) {
                e1.printStackTrace();
            }

            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        long end = System.currentTimeMillis();

        System.out.println("db fill took: " + (end - start) + "ms");
    }

}
