package com.motorpast.services.business;

import java.util.Date;

import com.motorpast.additional.MotorRequestState;
import com.motorpast.dataobjects.CarData;
import com.motorpast.services.persistence.MotorpastPersistenceException;

/**
 * all db requests, hold logic for trustlevel
 */
public interface BusinessService<R, T>
{
    /**
     * a valid request can trigger saving new data or only get a mileage for a given id
     * @param cardId needed in all cases
     * @param mileage if set we store a new car or mileage if car already exists
     * @return nothing or Data
     */
    public R doSystemRequest(final String carId, final String mileage, final T request)
          throws MotorpastPersistenceException, MotorpastBusinessException;

    /**
     * only decides by parameter if true or not - only id is set then it's true
     */
    public boolean isViewRequest(final String carId, final String mileage);

    /**
     * only decides by parameter if true or not - both values set then it's true
     */
    public boolean isStoringRequest(final String carId, final String mileage);

    /**
     * returns the value 81% or 8/10 for example
     */
    public String getTrustLevel(final CarData carData, final String carId) throws MotorpastPersistenceException;

    /**
     * returning requeststate by cardata
     */
    public MotorRequestState getMotorRequestStateByCarData(CarData carData);

    /**
     * formatting a given date by locale
     */
    public String getFormattedDate(final Date dateToFormat, final String languageIso2);

    /**
     * delivers an object-array ony for check
     */
    public Object[] getRedirectNullCheck();
}
