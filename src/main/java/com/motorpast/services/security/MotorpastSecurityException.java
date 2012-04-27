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

    public MotorpastSecurityException(final SecurityErrorCode errorCode, final String logMessage, final Object... param) {
        this.errorCode = errorCode;

        if(param != null && param.length > 0) {
            super.logMessage = String.format(logMessage, param);
        } else {
            super.logMessage = logMessage;
        }
    }

    public String getLogMessage() {
        return super.logMessage;
    }

    @Override
    public String getErrorCode() {
        return errorCode.name();
    }
}
