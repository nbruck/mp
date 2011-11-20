package com.motorpast.pages;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.annotations.SetupRender;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.Response;
import org.slf4j.Logger;

import com.motorpast.additional.MotorPages;
import com.motorpast.additional.MotorRequestState;
import com.motorpast.annotations.UrlRewrite;
import com.motorpast.base.BasePage;
import com.motorpast.components.MotorForm;
import com.motorpast.dataobjects.CarData;
import com.motorpast.dataobjects.UserSessionObj;
import com.motorpast.services.business.BusinessService;
import com.motorpast.services.business.MotorpastBusinessException;
import com.motorpast.services.business.MotorpastBusinessException.BusinessErrorCode;
import com.motorpast.services.business.ValidationService;
import com.motorpast.services.security.MotorpastSecurityException;
import com.motorpast.services.security.MotorpastSecurityException.SecurityErrorCode;
import com.motorpast.services.security.SecurityService;

@UrlRewrite(
    mappings={
        "/de/", "/"/*,"/en/", "/en"*/
    },
    sitemap={
        "changefreq", "monthly",
        "priority", "1.0"
})
public class Index extends BasePage
{
    @SessionState(create = true)
    private UserSessionObj sessionObj;


    @Inject
    private Response response;

    @Inject
    private HttpServletRequest httpServletRequest;

    @Inject
    private Logger logger;

    @Inject
    private Messages messages;

    @Inject
    private BusinessService<CarData, HttpServletRequest> businessService;

    @Inject
    private ValidationService validationService;

    @Inject
    private SecurityService securityService;

    @InjectComponent
    private MotorForm mainForm;

    @InjectPage
    private ResultPage resultPage;

    @InjectPage
    private ConfirmationPage confirmationPage;

    /**
     * text1 = car-id, text2 = fake, text3 = mileage
     */
    @Property
    private String text1, text2, text3;

    /**
     * a unique token to prevent CSRF
     */
    @Property
    private String text4;

    /**
     * a timestamp for spambot detection
     */
    @Property
    private long text5;


    private boolean sessionObjExists;
    private String carId, mileage;


    public String getPageName() {
        return Index.class.getSimpleName();
    }

    @Override
    public Class<?>[] getNavigation() {
        return MotorPages.getPagesForNavigation();
    }


    /**
     * catching all senseless urls for our application and sending 404
     */
    void onActivate(EventContext eventContext) {
        if(eventContext != null && eventContext.getCount()  > 0) {
            try {
                response.sendError(404, "not found");
            } catch (IOException e) {
                logger.error("ioException while try sending error 404");
            }
        }
    }

    @SetupRender
    void createUniqueRequestToken() {
        if(!sessionObjExists) {
            sessionObj.testForCreation();
        }
        // reset stuff
        confirmationPage.setPageParameter(null, null, null);
        resultPage.setPageParameter(null, null, null);
    }

    void onPrepareForRenderFromMainForm() {
        final String token = securityService.generateToken(httpServletRequest);
        text4 = token;
        sessionObj.setUniqueToken(token);

        final long timestamp = securityService.generateTimestamp();
        text5 = timestamp;
        sessionObj.setTimestamp(timestamp);
    }

    void onPrepareForSubmitFromMainForm() throws MotorpastBusinessException {
        if(!sessionObjExists) {
            throw new MotorpastBusinessException(BusinessErrorCode.session_timeout);
        }
    }

    void onValidateFormFromMainForm() throws MotorpastSecurityException {
        logger.debug("received values from mainform:  text1=" + text1 + ", text2=" + text2 + ", text3=" + text3 + 
              ", text4=" + text4 + ", text5="+ text5 + " ...end");

        // this is an attempt to prevent CSRF
        if(!text4.equals(sessionObj.getUniqueToken())) {
            throw new MotorpastSecurityException(SecurityErrorCode.error_security);
        }

        if(text2 != null) {
            logger.info("hidden fake-textfield has been filled with value=" + text2);
            throw new MotorpastSecurityException(SecurityErrorCode.error_security);
        }

        carId = text1;
        mileage = text3;

        //carid is required in both cases - search or new entry in DB
        if(carId == null || carId.isEmpty()) {
            mainForm.recordError(messages.get("error.carId.required"));
            return;
        }

        if(!validationService.validateCarId(carId)) {
            mainForm.recordError(messages.get("error.carId.regex-invalid"));
            return;
        }

        // check date for spambot detection
        if(text5 == 0                                                       // field is missing -> spam
            || !validationService.validateNumeric(String.valueOf(text5))    // manipulation
            || text5 != sessionObj.getTimestamp()                           // manipulation
            || text5 >= securityService.generateCheckTimestamp()            // too fast -> spam
        ) {
            throw new MotorpastSecurityException(SecurityErrorCode.error_security);
        }

        if(mileage != null && !mileage.isEmpty() && ! validationService.validateMileage(mileage)) {
            mainForm.recordError(messages.get("error.mileage.no-number"));
        }
    }

    Object onSuccessFromMainForm() throws MotorpastBusinessException {
        // now first check the kind of request
        if(businessService.isViewRequest(carId, mileage)) {
            resultPage.setPageParameter(MotorRequestState.SimpleViewRequest, carId, mileage);
            return resultPage;
        } else if(businessService.isStoringRequest(carId, mileage)){
            confirmationPage.setPageParameter(null, carId, mileage);
            return confirmationPage;
        } else {
            logger.error("first business-request in indexpage is called with wrong parameters: carId=" + carId + ", mileage=" + mileage);
            throw new MotorpastBusinessException(BusinessErrorCode.system_error);
        }
    }
}
