package com.motorpast.additional;

public abstract class MotorpastException extends Exception
{
    private static final long serialVersionUID = 2806300483459568524L;


    protected boolean showErrorDialog;


    public abstract boolean isShowErrorDialog();

    public abstract String getErrorCode();
}
