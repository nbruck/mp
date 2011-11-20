package com.motorpast.components;

import org.apache.tapestry5.PersistenceConstants;
import org.apache.tapestry5.annotations.Cached;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.slf4j.Logger;

import com.motorpast.additional.MotorUtils;
import com.motorpast.services.security.MotorpastSecurityException;
import com.motorpast.services.security.MotorpastSecurityException.SecurityErrorCode;
import com.motorpast.services.security.SecurityService;


/**
 * NOT used at the moment because it's broken!!!
 * simple-render request: onprepare than beginrender
 * form-request(success): validate, success, onprepare, begin
 */
public class MotorSpambotForm
{
    @Inject
    private SecurityService securityService;

    @Inject
    private Logger logger;

    @Inject
    private Messages messages;

    @Property
    private String currentFieldValue;

    @Property
    private int fieldCounter;


    @Persist(PersistenceConstants.FLASH)
    private int[] textfieldsetup;

    private String carId, mileage;


    boolean setupRender() {
        return false; // TODO: maybe if we use this remove return!
    }

    void onPrepareForRender() {
        final int[] fieldsetup = securityService.getCombos();
        logger.debug("generated fieldsetup=" + fieldsetup[0] + ", " + fieldsetup[1] + ", " + fieldsetup[2] + ", " + fieldsetup[3]);

        textfieldsetup = fieldsetup;
    }

    void onAfterSubmit() throws MotorpastSecurityException {
        logger.debug("fieldcounter=" + fieldCounter + " currentFieldValue=" + currentFieldValue);

        if(textfieldsetup[0] == fieldCounter) {
            carId = currentFieldValue;
            return;
        } else if(textfieldsetup[1] == fieldCounter) {
            mileage = currentFieldValue;
            return;
        } else {
            if(currentFieldValue != null || !currentFieldValue.isEmpty()) {
                throw new MotorpastSecurityException(SecurityErrorCode.error_security);
            }
        }
    }

    void onValidate() {
        if(carId.length() != 2)
        	System.out.println("scheisse...");
    }

    void onSuccess() {
        System.out.println("success...");
    }

    void onFailure() {
        System.out.println("failure...");
    }


    public final String getClassText() {
        if(isFirstFieldSet() || isSecondFieldSet()) {
            return securityService.getRandomCss(false);
        } else {
            return securityService.getRandomCss(true);
        }
    }

    @Cached(watch = "fieldCounter")
    public final String getTextfieldId() {
        return "text" + fieldCounter;
    }

    public final String getLabelText() {
        if(isFirstFieldSet()) {
            return messages.get("label.txt.carId");
        } else if (isSecondFieldSet()) {
            return messages.get("label.txt.mileage");
        } else {
            return messages.get("fake.label." + MotorUtils.getRandomInt(3));
        }
    }

    public final String getTextfieldTooltipText() {
        if(isFirstFieldSet()) {
            return messages.get("tooltip.carId");
        } else if (isSecondFieldSet()) {
            return messages.get("tooltip.mileage");
        } else {
            return 0 == MotorUtils.getRandomInt(2) ? messages.get("tooltip.carId") : messages.get("tooltip.mileage");
        }
    }

    public final String getPlaceholder() {
        if(isFirstFieldSet()) {
            return messages.get("txt.carId");
        } else if(isSecondFieldSet()) {
            return messages.get("txt.mileage");
        } else {
        	return messages.get("fake.label." + MotorUtils.getRandomInt(3));
        }
    }

    public final int getMaxlength() { // 17 = carId, 7 = mileagelength
        if(isFirstFieldSet()) {
            return 17;
        } else if (isSecondFieldSet()) {
            return 7;
        } else {
            return 0 == MotorUtils.getRandomInt(2) ? 17 : 7;
        }
    }

    @Cached(watch = "fieldCounter")
    private final boolean isFirstFieldSet() {
        return textfieldsetup[0] == fieldCounter;
    }

    @Cached(watch = "fieldCounter")
    private final boolean isSecondFieldSet() {
        return textfieldsetup[1] == fieldCounter;
    }
}
