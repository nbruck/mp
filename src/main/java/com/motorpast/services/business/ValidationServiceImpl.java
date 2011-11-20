package com.motorpast.services.business;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ValidationServiceImpl implements ValidationService
{
    private enum MotorValidationRegex
    {
        NUMERIC("^[0-9]+$"),            // simple pattern for testing if is number
        CAR_ID("^[A-Z0-9ÄÖÜ]{17}$"),    // 17 signs alphanumeric
        MILEAGE("^\\d{1,7}$");          // min 1, max 7 digits

        final private Pattern regexPattern;

        private MotorValidationRegex(final String regex) {
            this.regexPattern = Pattern.compile(regex);
        }

        final boolean isVaild(final String value) {
            Matcher m = regexPattern.matcher(value);

            return m.matches();
        }
    }


    @Override
    public boolean validateNumeric(final String value) {
        return MotorValidationRegex.NUMERIC.isVaild(value);
    }

    @Override
    public boolean validateMileage(String mileage) {
        return MotorValidationRegex.MILEAGE.isVaild(mileage);
    }

    @Override
    public boolean validateCarId(final String carId) {
        if(!MotorValidationRegex.CAR_ID.isVaild(carId)) {
            return false;
        }

        return true;
    }
}
