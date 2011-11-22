package com.motorpast.services.persistence;

import com.motorpast.additional.MotorpastException;

public class MotorpastPersistenceException extends MotorpastException
{
    private static final long serialVersionUID = 8776640474987634326L;


    public static enum PersistenceErrorCode
    {
        /**
         * no data for the given CAR-Identifier found
         */
        data_notFound_carId,
        /**
         * no data has been found for the given database id
         */
        data_notFound_id,
        /**
         * no mileages for carid found
         */
        mileages_notFound_carId,
        /**
         * it wasn't possible to store the registrationdate
         */
        regdate_storing_impossible,
        /**
         * two or more clients modify the same entity
         */
        concurrent_writing_access,
        /**
         * something went wrong during persistence process
         */
        unexpected
    }


    private PersistenceErrorCode errorCode;


    @SuppressWarnings("unused")
    private MotorpastPersistenceException() {
    }

    public MotorpastPersistenceException(final PersistenceErrorCode errorCode) {
        this.errorCode = errorCode;
    }


    public boolean isShowErrorDialog() {
        return true;
    }

    public String getErrorCode() {
        return errorCode.name();
    }
}
