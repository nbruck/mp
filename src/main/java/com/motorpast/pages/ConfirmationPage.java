package com.motorpast.pages;

import java.util.Date;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.apache.tapestry5.Block;
import org.apache.tapestry5.PersistenceConstants;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SetupRender;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.slf4j.Logger;

import com.motorpast.additional.MotorRequestState;
import com.motorpast.additional.MotorUtils;
import com.motorpast.annotations.UrlRewrite;
import com.motorpast.base.BasePage;
import com.motorpast.dataobjects.CarData;
import com.motorpast.services.business.BusinessService;
import com.motorpast.services.business.MotorpastBusinessException;
import com.motorpast.services.persistence.MotorpastPersistenceException;
import com.motorpast.services.persistence.PersistenceService;

/**
 * only storing requests are allowed to see this page //TODO: secure me!
 */
@UrlRewrite(
    mappings={
        "/de/confirmationpage", "/eingaben-pruefen"
    },
    putInSitemap = false
)
public class ConfirmationPage extends BasePage
{
    @Inject
    private Messages messages;

    @Inject
    private Logger logger;

    @Inject
    private Locale locale;

    @Inject
    private BusinessService<CarData, HttpServletRequest> businessService;

    @Inject
    private PersistenceService<CarData> persistenceService;

    @InjectPage
    private ResultPage resultPage;

    @Inject
    private Block confirmOnlyBlock, notBlockedBlock, blockedBlock, fallbackBlock;

    @Property @Persist
    private String carId, mileage;

    @Persist(PersistenceConstants.FLASH) // only set this in eventhandler for a new renderrequest!
    private CarData carData;

    @Property @Persist
    private MotorRequestState motorRequestState;

    private Class<?>[] navigation;


    void pageLoaded() {
        navigation = new Class<?>[] {Index.class};
    }

    public Class<?>[] getNavigation() {
        return navigation;
    }

    @SetupRender
    void init() throws MotorpastBusinessException {
        super.checkForRedirect(Index.class, carId, mileage);

        if(carData != null) {
            logger.debug("cardata has been set with " + carData.toString());
        } else {
            try {
                carData = persistenceService.getDataForCarId(carId);
            } catch (MotorpastPersistenceException e) {
                //throw new MotorpastBusinessException(BusinessErrorCode.system_error);
                logger.debug("cardata will be null");
            }
        }

        //if(motorRequestState == null) {
        motorRequestState = businessService.getMotorRequestStateByCarData(carData);
        //}
        super.checkForRedirect(ErrorPage.class, motorRequestState); // state maybe is null
        logger.debug("motorRequestSate has been set to:" + motorRequestState);

        // reset
        resultPage.setPageParameter(null, null, null);
    }

    void cleanupRender() {
        if(motorRequestState == MotorRequestState.CarTupelNoAttemptsLeftAndBlocked) {
            carId = mileage = null;
            motorRequestState = null;
        }
    }

    public Block getActiveBlock() {
        switch (motorRequestState) {
            case CarTupelNoAttemptsLeftAndBlocked:
                return blockedBlock;
            case UpdateCarTupelWithAttemptsLeft:
                return notBlockedBlock;
            case SaveCompleteNewCarTupel:
            case UpdateCarTupelWhichDoesntAlreadyHaveRegistrationDate:
                return confirmOnlyBlock;
            default:
                return fallbackBlock;
        }
    }

    public String getBlockedMessage() {
        return getFormattedBlockedMessage(carData);
    }

    public Object onSelectedFromOk() {
        resultPage.setValidStoringRequest(true);
        return resetDataAndGoOn(ResultPage.class);
    }

    public void onGotoErrorPage() {
        super.checkForRedirect(ErrorPage.class, businessService.getRedirectNullCheck());
    }

    String onHandleValidateBubbleUp(final String day, final String month, final String year) throws MotorpastPersistenceException {
        final Date enteredDate = MotorUtils.createDateFromInput(Integer.valueOf(year), Integer.valueOf(month), Integer.valueOf(day));
        final CarData carDataForSecurityCheck = persistenceService.getDataForCarId(carId);

        String returnMessage = null;
        if(carDataForSecurityCheck.getAttemptsLeft() == 0) {
            //regDateForm.recordError(getFormattedBlockedMessage(carDataForSecurityCheck));
            returnMessage = getFormattedBlockedMessage(carDataForSecurityCheck);
            //motorRequestState = MotorRequestState.CarTupelNoAttemtsLeftAndBlocked;
            //return;
        } else if(carDataForSecurityCheck.getRegistrationdate().getTime() != enteredDate.getTime()) {
            persistenceService.updateCarDataAttempts(carDataForSecurityCheck, carDataForSecurityCheck.getAttemptsLeft() - 1);
            //regDateForm.recordError(messages.format("validation.error.regdate-invalid", carDataForSecurityCheck.getAttemptsLeft()));
            returnMessage = messages.format("validation.error.regdate-invalid", carDataForSecurityCheck.getAttemptsLeft());
        } else if(carDataForSecurityCheck.getRegistrationdate().getTime() == enteredDate.getTime() && carDataForSecurityCheck.getAttemptsLeft() != 2) {
            persistenceService.updateCarDataAttempts(carDataForSecurityCheck);
        }

        carData = carDataForSecurityCheck;
        return returnMessage;
    }

    private String getFormattedBlockedMessage(final CarData carData) {
        return messages.format("validation.error.regdate-blocked",
                carData.getCarId(),
                businessService.getFormattedDate(carData.getBlockingdate(), locale.getLanguage()));
    }

    Object onHandleSuccessBubbleUp(final String day, final String month, final String year) {
        resultPage.setValidStoringRequest(true);
        return resetDataAndGoOn(ResultPage.class); //TODO: sorry, don't working (navigation)
    }

    private Object resetDataAndGoOn(Class<?> clazz) {
        resultPage.setPageParameter(motorRequestState, carId, mileage);
        carId = mileage = null;
        carData = null;
        motorRequestState = null;
        return clazz;
    }

    void setPageParameter(final MotorRequestState state, final String carId, final String mileage) {
        this.motorRequestState = state;
        this.carId = carId;
        this.mileage = mileage;
    }
}
