package com.motorpast.services.security;

import com.motorpast.additional.MotorpastException;

public class MotorpastSecurityException extends MotorpastException
{
    /**
     * 
     */
    private static final long serialVersionUID = -2511000993477451657L;

	public static enum SecurityErrorCode
    {
        error_security,
    }

    private SecurityErrorCode errorCode;


	@SuppressWarnings("unused")
    private MotorpastSecurityException() {
    }

    public MotorpastSecurityException(final SecurityErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    @Override
    public boolean isShowErrorDialog() {
        return false;
    }

    @Override
    public String getErrorCode() {
        return errorCode.name();
    }
}
