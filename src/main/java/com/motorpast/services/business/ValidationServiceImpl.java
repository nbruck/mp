package com.motorpast.services.business;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ValidationServiceImpl implements ValidationService
{
    private enum MotorValidationRegex
    {
        NUMERIC ("^[0-9]+$"),           // simple pattern for testing if is number
        CAR_ID ("^[A-Z0-9ÄÖÜ]{17}$"),   // 17 signs alphanumeric
        MILEAGE ("^\\d{1,7}$"),         // min 1, max 7 digits
        LEADING_ZEROS ("(0+)\\d+$"),    // getting leading zeros
        ;

        final private Pattern regexPattern;

        private MotorValidationRegex(final String regex) {
            this.regexPattern = Pattern.compile(regex);
        }

        final boolean isVaild(final String value) {
            Matcher m = regexPattern.matcher(value);

            return m.matches();
        }

        final Pattern getPattern() {
            return regexPattern;
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

    @Override
    public String deleteLeadingZeros(String mileage) {
        final Matcher m = MotorValidationRegex.LEADING_ZEROS.getPattern().matcher(mileage);

        if(m.matches()) {
            final String toRemove = m.group(1);

            return mileage.replaceFirst(toRemove, "");
        }

        return mileage;
    }
}
