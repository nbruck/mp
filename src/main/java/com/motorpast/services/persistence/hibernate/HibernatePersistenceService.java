package com.motorpast.services.persistence.hibernate;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.StaleObjectStateException;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.motorpast.dataobjects.CarData;
import com.motorpast.dataobjects.CarMileage;
import com.motorpast.dataobjects.hibernate.CarDataEntity;
import com.motorpast.dataobjects.hibernate.CarMileageEntity;
import com.motorpast.services.persistence.MotorpastPersistenceException;
import com.motorpast.services.persistence.MotorpastPersistenceException.PersistenceErrorCode;
import com.motorpast.services.persistence.PersistenceService;

public class HibernatePersistenceService implements PersistenceService<CarDataEntity>
{
    private final Logger logger;
    private final HibernateSessionFactory sessionFactory;

    private final int initialAttempts;
    private final int blockForDays;


    public HibernatePersistenceService(final HibernateSessionFactory sessionFactory, final int initialAttempts, final int blockForDays) {
        this.logger = LoggerFactory.getLogger(HibernatePersistenceService.class);
        this.sessionFactory = sessionFactory;
        this.initialAttempts = initialAttempts;
        this.blockForDays = blockForDays;
        logger.info("HibernatePersistenceService startet...");
    }


    public CarData getDataForCarId(final String carId) throws MotorpastPersistenceException {
        final Session session = getSession();

        return wrapHibernateCommandIntoTransaction(
            new HibernateCommand<CarData>(session) {
                CarData execute() throws MotorpastPersistenceException {
                    return getDataForCarIdInternal(session, carId);
                }
            }
        );
    }

    private CarData getDataForCarIdInternal(final Session session, final String carId) throws MotorpastPersistenceException {
        CarDataEntity selectedCar = (CarDataEntity)session.createQuery("from CarDataEntity c where c.carId = :carId")
              .setString("carId", carId)
              .uniqueResult();

        if(selectedCar != null) {
            checkBlockingDateAndUpdateBlockingDateAndAttempts(session, selectedCar);
            return (CarData)selectedCar;
        }

        logger.debug("no data for carId=" + carId + " found");
        throw new MotorpastPersistenceException(PersistenceErrorCode.data_notFound_carId);
    }

    private void checkBlockingDateAndUpdateBlockingDateAndAttempts(final Session session, final CarData carData) {
        if(carData.getBlockingdate() == null) { // blockingDate should only have to set in attemptsupdate-method
            return;
        }

        final long nowTimeStamp = new Date().getTime();

        if(carData.getBlockingdate().getTime() <= nowTimeStamp) {
            logger.debug("blockingdate expired for carid=" + carData.getCarId() + " so reset attepmts to default and blockingdate to null");
            carData.setAttemptsLeft(initialAttempts);
            carData.setBlockingdate(null);
            session.update(carData);
        }
    }

    public int getMileageForCarId(final String carId) throws MotorpastPersistenceException {
        CarData carData = getDataForCarId(carId);

        return carData.getLastMileage();
    }

    public List<Integer> getAllMileagesForCar(final String carId) throws MotorpastPersistenceException {
        final List<CarMileage> mileageData = getAllMileageDataForCar(carId);

        List<Integer> mileages = new ArrayList<Integer>(mileageData.size());

        for(CarMileage currentData : mileageData) {
            mileages.add(currentData.getEarlierStoredMileage());
        }

        return mileages;
    }

    @SuppressWarnings("unchecked")
    public List<CarMileage> getAllMileageDataForCar(final String carId) throws MotorpastPersistenceException {
        final Session session = getSession();

        return wrapHibernateCommandIntoTransaction(
            new HibernateCommand<List<CarMileage>>(session) {
                List<CarMileage> execute() throws MotorpastPersistenceException {
                    CarDataEntity selectedCar = (CarDataEntity)session.createQuery("from CarDataEntity c where c.carId = :carId")
                    .setString("carId", carId)
                    .uniqueResult();

                    if(selectedCar == null) {
                        logger.debug("no data for carId=" + carId + " found");
                        throw new MotorpastPersistenceException(PersistenceErrorCode.data_notFound_carId);
                    }

                    List<CarMileageEntity> mileageEntities = session.createQuery("from CarMileageEntity where carTupelId = "
                        + selectedCar.getIdentifier()
                        + " order by mileageStoringDate desc")
                        .list();

                    if(mileageEntities == null || mileageEntities.isEmpty()) {
                        logger.debug("mileages are null or no data found for carId=" + carId);
                        throw new MotorpastPersistenceException(PersistenceErrorCode.mileages_notFound_carId);
                    }

                    List<CarMileage> mileageData = new ArrayList<CarMileage>(mileageEntities.size());
                    for(final CarMileageEntity currentMileageEntity : mileageEntities) {
                        mileageData.add(currentMileageEntity);
                    }

                    return mileageData;
                }
            }
        );
    }

    public CarData saveNewCarData(final CarDataEntity carToSave, final String ip, final String hostInfo) throws MotorpastPersistenceException {
        final Session session = getSession();

        return wrapHibernateCommandIntoTransaction(
            new HibernateCommand<CarData>(session) {
                CarData execute() {
                    session.save(carToSave);

                    storeMileageEntityForCar(session, carToSave, ip, hostInfo);

                    return (CarData)carToSave;
                }
            }
        );
    }

    private void storeMileageEntityForCar(final Session session, final CarDataEntity carDataEntity, final String ip, final String hostInfo) {
        // and store new tupel in mileage table (for BOTH! cases: saveNew or Update)
        CarMileageEntity carMileageEntity = new CarMileageEntity(
            carDataEntity.getLastMileage(),
            carDataEntity.getLastMileageStoringDate(),
            ip,
            hostInfo
        );

        session.save(carMileageEntity);
        // set relation (only on mileage)
        carMileageEntity.setCarData(carDataEntity);
    }

    public CarData updateCarData(final CarDataEntity selectedCar, final Date newStoringDate, final int newMileage,
          final String ip, final String hostInfo) throws MotorpastPersistenceException
    {
        final Session session = getSession();

        return wrapHibernateCommandIntoTransaction(new HibernateCommand<CarData>(session) {
            CarData execute() {
                selectedCar.setLastMileage(newMileage);
                selectedCar.setLastMileageStoringDate(newStoringDate);
                session.update(selectedCar);

                storeMileageEntityForCar(session, selectedCar, ip, hostInfo);

                return selectedCar;
            }
        });
    }

    @Override
    public CarData updateCarDataAttempts(final CarDataEntity selectedCar, final int... attemptsLeftParameter) throws MotorpastPersistenceException {
        final Session session = getSession();

        return wrapHibernateCommandIntoTransaction(new HibernateCommand<CarData>(session) {
            CarData execute() throws MotorpastPersistenceException {
                int attemptsLeft = -1;
                if(attemptsLeftParameter == null || attemptsLeftParameter.length == 0) {
                    attemptsLeft = initialAttempts;
                } else {
                    attemptsLeft = attemptsLeftParameter[0];
                }

                if(attemptsLeft == -1) {
                    throw new MotorpastPersistenceException(PersistenceErrorCode.unexpected);
                }
                logger.debug("attempts for " + selectedCar.getCarId() + " will be set to=" + attemptsLeft);
                selectedCar.setAttemptsLeft(attemptsLeft);

                if(attemptsLeft == 0) { // set blocking date and update then
                    final Calendar calendar = new GregorianCalendar();
                    calendar.set(Calendar.HOUR_OF_DAY, 0);
                    calendar.set(Calendar.MINUTE, 0);
                    calendar.set(Calendar.SECOND, 1);
                    calendar.add(Calendar.DATE, blockForDays);
                    final Date blockingDate = calendar.getTime();

                    selectedCar.setBlockingdate(blockingDate);
                    logger.info("Blocking date for carid=" + selectedCar.getCarId() + " set to " + blockingDate.toString());
                }
                session.update(selectedCar);

                return selectedCar;
            }
        });
    }

    @Override
    public CarData updateCarDataWithRegistrationDate(final String carId, final Date regDate) throws MotorpastPersistenceException {
        final Session session = getSession();

        return wrapHibernateCommandIntoTransaction(new HibernateCommand<CarData>(session) {
            CarData execute() throws MotorpastPersistenceException {
                final CarData selectedCarData = getDataForCarIdInternal(session, carId);
                selectedCarData.setRegistrationdate(regDate);
                session.update(selectedCarData);

                return selectedCarData;
            }
        });
    }

    /**
     * all calls should use this for a unique session
     */
    private Session getSession() {
        return sessionFactory.getInstance().getCurrentSession();
    }

    /**
     * wrapping all hibernate calls into a transaction
     * @throws MotorpastPersistenceException bubble up for later handling
     */
    @SuppressWarnings("unchecked")
    private<R> R wrapHibernateCommandIntoTransaction(HibernateCommand<?> command) throws MotorpastPersistenceException {
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
}
