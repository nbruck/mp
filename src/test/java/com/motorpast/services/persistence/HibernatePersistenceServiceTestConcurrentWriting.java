package com.motorpast.services.persistence;

import java.util.Date;

import org.apache.tapestry5.ioc.test.TestBase;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.motorpast.dataobjects.CarData;
import com.motorpast.dataobjects.hibernate.CarDataEntity;
import com.motorpast.services.persistence.hibernate.HibernatePersistenceService;
import com.motorpast.services.persistence.hibernate.HibernateMySQLSessionFactory;

/**
 * I hope it will simulate n threads which parallel doing requests to hibernate-system or mongo or simpledb
 */
public class HibernatePersistenceServiceTestConcurrentWriting extends TestBase
{
    private final int dbRequestSize = 2;

    private PersistenceService<CarDataEntity> persistenceService;
    private DBRequest[] dbRequests;


    @BeforeTest
    public void init() {
        persistenceService = new HibernatePersistenceService(new HibernateMySQLSessionFactory(), 2, 90);
        dbRequests = new DBRequest[dbRequestSize];

        for(int i = 0; i < dbRequestSize; i++) {
            dbRequests[i] = new DBRequest(i + 1, persistenceService);
        }
    }

    @Test
    public void persistenceMultiHibernate() {
        for(int i = 0; i < dbRequestSize; i++) {
            dbRequests[i].start();
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        for(int i = 0; i < dbRequestSize; i++) {
            try {
                dbRequests[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    private class DBRequest extends Thread
    {
        private final int threadId;
        private final PersistenceService<CarDataEntity> persistenceService;

        private int[] mileagesToStore;
        private int ctr;

        DBRequest(
            final int threadId,
            final PersistenceService<CarDataEntity> persistenceService
        ) {
            this.threadId = threadId;
            this.persistenceService = persistenceService;
            this.mileagesToStore = new int[] {
                1000, 2000, 3000, 4000, 5000, 6000, 7000, 8000, 9000, 10000
            };
            this.ctr = 1;
        }


        public void run() {
            for(int i = 0; i < 20; i++) {
                long start = System.currentTimeMillis();

                try {
                    persistenceService.saveNewCarData(
                        new CarDataEntity(
                            "1001",
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
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }

                CarData carData = null;
                try {
                    carData = persistenceService.getCarDataById("t-" + threadId);
                } catch (MotorpastPersistenceException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }

                long end = System.currentTimeMillis();

                //assertEquals(carData.getLastMileage(), threadId * mileagesToStore[i] + ctr);
                System.out.println("db write and read took: " + (end - start) + "ms");
                ctr++;

                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
