package com.motorpast.components;

import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.corelib.components.TextField;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.slf4j.Logger;

import com.motorpast.additional.MotorRequestState;
import com.motorpast.additional.MotorpastException;
import com.motorpast.dataobjects.UserSessionObj;
import com.motorpast.pages.HelpPage;
import com.motorpast.pages.ResultPage;
import com.motorpast.services.business.MotorpastBusinessException;
import com.motorpast.services.business.MotorpastBusinessException.BusinessErrorCode;
import com.motorpast.services.business.ValidationService;
import com.motorpast.services.security.MotorpastSecurityException;
import com.motorpast.services.security.MotorpastSecurityException.SecurityErrorCode;

/**
 * text1 = carid for search request
 * text2 = fake field for bad spam bots
 */
public class MileageSearchComponent
{
    @SessionState
    private UserSessionObj sessionObj;

    @Inject
    private Logger logger;

    @Inject
    private PageRenderLinkSource pageRenderLinkSource;

    @Inject
    private Messages messages;

    @Inject
    private ValidationService validationService;

    @InjectPage
    private ResultPage resultPage;

    @InjectComponent
    private MotorForm mileSearchForm;

    @InjectComponent(value="text1")
    private TextField vinsearch;

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
        logger.debug("received values from mileSearchForm:  text1=" + text1 + ", fakefield=" + text2 + "(which should be null or empty)");

        if(text2 != null) {
            logger.info("hidden fake-textfield has been filled with value=" + text2);
            throw new MotorpastSecurityException(SecurityErrorCode.error_security);
        }

        if(messages.get("txt.input.vin.placeholder").equals(text1)) {
            text1 = null;
        }
        carId = text1 != null ? text1.toUpperCase() : null;

        //carid only is required
        if(carId == null || carId.isEmpty()) {
            mileSearchForm.recordError(vinsearch, messages.get("error.carId.required"));
            return;
        }

        if(!validationService.validateCarId(carId)) {
            mileSearchForm.recordError(vinsearch, messages.get("error.carId.regex-invalid"));
            return;
        }
    }

    Object onSuccessFromMileSearchForm() throws MotorpastException {
        resultPage.setPageParameter(MotorRequestState.SimpleViewRequest, carId, null);
        return resultPage;
    }

    public Object[] getToolTipParameter() {
        return new Object[] {
            pageRenderLinkSource.createPageRenderLink(HelpPage.class).toURI()
        };
    }
}
