package com.motorpast.components;

import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.slf4j.Logger;

import com.motorpast.additional.MotorRequestState;
import com.motorpast.additional.MotorpastException;
import com.motorpast.dataobjects.UserSessionObj;
import com.motorpast.pages.ResultPage;
import com.motorpast.services.business.MotorpastBusinessException;
import com.motorpast.services.business.MotorpastBusinessException.BusinessErrorCode;
import com.motorpast.services.business.ValidationService;
import com.motorpast.services.security.MotorpastSecurityException;
import com.motorpast.services.security.MotorpastSecurityException.SecurityErrorCode;
import com.motorpast.services.security.SecurityService;

/**
 * text1 = carid for search request
 * text2 = fake field for bad spam bots
 * text3 = unique token, text4 = timestamp : both values created in page and passed by parameters
 */
public class MileageSearchComponent
{
    @SessionState
    private UserSessionObj sessionObj;

    @Inject
    private Logger logger;

    @Inject
    private Messages messages;

    @Inject
    private ValidationService validationService;

    @Inject
    private SecurityService securityService;

    @InjectPage
    private ResultPage resultPage;

    @InjectComponent
    private MotorForm mileSearchForm;

    @Parameter(required = true, name = "token")
    @Property
    private String text3;

    @Parameter(required = true, name = "date")
    @Property
    private long text4;

    @Property
    private String text1, text2; // carid for search only, second field is fake

    private boolean sessionObjExists;
    private String carId;


    void onPrepareForRenderFromMileSearchForm() {
        text1 = messages.get("txt.input.vin.placeholder");
    }

    void onPrepareForSubmitFromMileSearchForm() throws MotorpastBusinessException {
        if(!sessionObjExists) {
            throw new MotorpastBusinessException(BusinessErrorCode.session_timeout);
        }
    }

    void onValidateFormFromMileSearchForm() throws MotorpastSecurityException {
        logger.debug("received values from mileSearchForm:  text1=" + text1 + ", text3=" + text3 + ", text4=" + text4 + " ...end");

        // this is an attempt to prevent CSRF
        if(!text3.equals(sessionObj.getUniqueToken())) {
            throw new MotorpastSecurityException(SecurityErrorCode.error_security);
        }

        if(text2 != null) {
            logger.info("hidden fake-textfield has been filled with value=" + text2);
            throw new MotorpastSecurityException(SecurityErrorCode.error_security);
        }

        if(messages.get("txt.input.vin.placeholder").equals(text1)) {
            text1 = null;
        }
        carId = text1;

        //carid only is required
        if(carId == null || carId.isEmpty()) {
            mileSearchForm.recordError(messages.get("error.carId.required"));
            return;
        }

        if(!validationService.validateCarId(carId)) {
            mileSearchForm.recordError(messages.get("error.carId.regex-invalid"));
            return;
        }

        // check date for spambot detection
        if(text4 == 0                                                       // field is missing -> spam
            || !validationService.validateNumeric(String.valueOf(text4))    // manipulation
            || text4 != sessionObj.getTimestamp()                           // manipulation
            || text4 >= securityService.generateCheckTimestamp()            // too fast -> spam
        ) {
            throw new MotorpastSecurityException(SecurityErrorCode.error_security);
        }
    }

    Object onSuccessFromMileSearchForm() throws MotorpastException {
        resultPage.setPageParameter(MotorRequestState.SimpleViewRequest, carId, null);
        return resultPage;
    }
}
