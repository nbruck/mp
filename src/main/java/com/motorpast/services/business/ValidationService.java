package com.motorpast.services.business;

public interface ValidationService
{
    boolean validateNumeric(final String value);

    boolean validateCarId(final String carId);

    boolean validateMileage(final String mileage);
}
