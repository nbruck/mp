package com.motorpast.services.persistence;

import java.util.Date;
import java.util.List;

import com.motorpast.dataobjects.CarData;
import com.motorpast.dataobjects.CarMileage;

/**
 * at least there are three implementations: hibernate, mongoDB and simpleDB
 */
public interface PersistenceService<T extends CarData>
{
    /**
     * get data for a given car-id (car identifier)
     */
    public CarData getDataForCarId(final String carId) throws MotorpastPersistenceException;

    /**
     * only returns the mileage
     */
    public int getMileageForCarId(final String carId) throws MotorpastPersistenceException;

    /**
     * delivers all mileages for a car-identifier
     * maybe we sould return a pojo-list of cardata
     */
    public List<Integer> getAllMileagesForCar(final String carId) throws MotorpastPersistenceException;

    /**
     * returns all mileagedata-objects ordered by date desc
     */
    public List<CarMileage> getAllMileageDataForCar(final String carId) throws MotorpastPersistenceException;

    /**
     * save a car and/or stores the mileage
     */
    public CarData saveNewCarData(final T carToSave, final String ip, final String hostInfo) throws MotorpastPersistenceException;

    /**
     * update existing car data
     */
    public CarData updateCarData(final T selectedCar, final Date newStoringDate, final int newMileage,
          final String ip, final String hostInfo
    ) throws MotorpastPersistenceException;

    /**
     * only sets attempts for given cardataentity
     */
    public CarData updateCarDataAttempts(final T selectedCar, final int... attemptsLeft) throws MotorpastPersistenceException;

    /**
     * store regdate
     */
    public CarData updateCarDataWithRegistrationDate(final String carId, final Date regDate) throws MotorpastPersistenceException;
}
