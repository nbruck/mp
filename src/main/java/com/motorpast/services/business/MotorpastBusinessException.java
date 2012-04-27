package com.motorpast.services.business;

import com.motorpast.additional.MotorpastException;

public class MotorpastBusinessException extends MotorpastException
{
    private static final long serialVersionUID = 2998980456735221843L;


    public static enum BusinessErrorCode
    {
        system_error,
        session_timeout,
    }


    private BusinessErrorCode errorCode;


    @SuppressWarnings("unused")
    private MotorpastBusinessException() {
    }

    public MotorpastBusinessException(final BusinessErrorCode errorCode) {
        this.errorCode = errorCode;
    }


    public String getErrorCode() {
        return errorCode.name();
    }
}
