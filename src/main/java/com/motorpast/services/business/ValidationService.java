package com.motorpast.services.business;

public interface ValidationService
{
    public boolean validateNumeric(final String value);

    public boolean validateCarId(final String carId);

    public boolean validateMileage(final String mileage);

    public String deleteLeadingZeros(final String mileage);
}
