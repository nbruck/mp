package com.motorpast.pages;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.apache.tapestry5.Block;
import org.apache.tapestry5.PersistenceConstants;
import org.apache.tapestry5.annotations.Cached;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.annotations.SetupRender;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.slf4j.Logger;

import com.motorpast.additional.MotorRequestState;
import com.motorpast.additional.MotorUtils;
import com.motorpast.annotations.UrlRewrite;
import com.motorpast.base.BasePage;
import com.motorpast.dataobjects.CarData;
import com.motorpast.dataobjects.UserSessionObj;
import com.motorpast.services.business.BusinessService;
import com.motorpast.services.business.MotorpastBusinessException;
import com.motorpast.services.persistence.MotorpastPersistenceException;
import com.motorpast.services.persistence.MotorpastPersistenceException.PersistenceErrorCode;
import com.motorpast.services.persistence.PersistenceService;

/**
 * displays a mileage result (captcha is embedded)
 */
@UrlRewrite(
    mappings={
        "/de/resultpage", "/anfrage-ergebnis"
    },
    putInSitemap = false
)
public class ResultPage extends BasePage
{
    @SessionState
    private UserSessionObj sessionObj;

    @Inject
    private Logger logger;

    @Inject
    private Messages messages;

    @Inject
    private HttpServletRequest request;

    @Inject
    private BusinessService<CarData, HttpServletRequest> businessService;

    @Inject
    private PersistenceService<CarData> persistenceService;

    @InjectPage
    private ConfirmationPage confirmationPage;

    @Inject
    private Block viewOnlyBlock, newCarBlock, noRegdateStoredBeforeBlock, regDateStoredBeforeBlock, fallbackBlock;

    @Property @Persist
    private String carId, mileage;

    @Property @Persist
    private MotorRequestState motorRequestState;

    @Property(write = false)
    private boolean showTrustLevelOfCarData;

    @Property(write = false)
    private Date storingDate;

    @Property(write = false)
    private boolean showEnterComponent;

    @Persist(PersistenceConstants.FLASH) @Property(write = false)
    private boolean validStoringRequest;

    @Persist(PersistenceConstants.FLASH) @Property(write = false)
    private String errorMessage, otherMessage;

    private Class<?>[] navigation;


    void pageLoaded() {
        navigation = new Class<?>[] {Index.class};
    }

    public Class<?>[] getNavigation() {
        return navigation;
    }


    @SuppressWarnings("unchecked")
    @SetupRender
    boolean init() {
        boolean abort = super.checkForRedirect(Index.class, carId, motorRequestState);

        switch (motorRequestState) {
            case SimpleViewRequest:
                final CarData carDataView = doSystemRequestMehtod(null);
                showTrustLevelOfCarData = carDataView != null ? carDataView.isShowTrustLevel() : false;
                break;
            case SaveCompleteNewCarTupel:
            case UpdateCarTupelWhichDoesntAlreadyHaveRegistrationDate:
            case UpdateCarTupelWithAttemptsLeft:
                abort = super.checkForRedirect(ErrorPage.class, mileage);

                if(validStoringRequest) {
                    final CarData carDataSave = doSystemRequestMehtod(mileage);
                    if(carDataSave != null) {
                        showTrustLevelOfCarData = motorRequestState == MotorRequestState.SaveCompleteNewCarTupel ?
                              false : 
                              carDataSave.isShowTrustLevel();
                    }
                }
                break;
            default:
                abort = super.checkForRedirect(Index.class, businessService.getRedirectNullCheck());
                break;
        }

        confirmationPage.setPageParameter(null, null, null);
        return !abort;
    }

    void cleanupRender() {
        switch (motorRequestState) {
            //case SimpleViewRequest: // don't render again if it once has been displayed //TODO: keep this in mind!
            case UpdateCarTupelWithAttemptsLeft:
                carId = mileage = null;
                motorRequestState = null;
                break;
            default:
                break;
        }
    }

    final private CarData doSystemRequestMehtod(final String mileageParameter) {
        CarData carData = null;
        try {
            carData = businessService.doSystemRequest(carId, mileageParameter, request);
            storingDate = carData.getLastMileageStoringDate();
            logger.debug(carData.toString());
        } catch (MotorpastPersistenceException e) {
            if(motorRequestState == MotorRequestState.SimpleViewRequest && PersistenceErrorCode.data_notFound_carId.name().equals(e.getErrorCode())) {
                sessionObj.setMileageResultCaptcha(null); // reset for displaying the else block in .tml for data not found
            } else {
                checkForRedirect(ErrorPage.class, businessService.getRedirectNullCheck());
            }
        } catch (MotorpastBusinessException e) {
            checkForRedirect(ErrorPage.class, businessService.getRedirectNullCheck());
        }

        return carData;
    }

    Object onSelectedFromNoThankYou() {
        logger.debug("no storing of regdate requested at the moment");
        return Index.class;
    }

    Object onHandleSuccessBubbleUp(final String day, final String month, final String year) {
        final Date enteredDate = MotorUtils.createDateFromInput(Integer.valueOf(year), Integer.valueOf(month), Integer.valueOf(day));
        try {
            persistenceService.updateCarDataWithRegistrationDate(carId, enteredDate);
            motorRequestState = MotorRequestState.SimpleViewRequest;
            otherMessage = messages.get("success.regdate.storing.successful");
        } catch (MotorpastPersistenceException e) {
            errorMessage = messages.get("error.regdate.storing.impossible");
        }

        return null;
    }


    public Block getActiveBlock() { // at the moment mileageresult is null in all cases but simpleviewrequest
        switch (motorRequestState) {
            case SimpleViewRequest:
                return viewOnlyBlock;
            case SaveCompleteNewCarTupel:
                return newCarBlock;
            case UpdateCarTupelWhichDoesntAlreadyHaveRegistrationDate:
                return noRegdateStoredBeforeBlock;
            case UpdateCarTupelWithAttemptsLeft:
                return regDateStoredBeforeBlock;
            default:
                return fallbackBlock;
        }
    }

    @Cached
    public boolean isMileageResultAvailable() {
        if(sessionObj != null && sessionObj.isMileageAvailable()) {
            return true;
        } else {
            errorMessage = messages.format("error.data.notFound.carId", carId);
            return false;
        }
    }

    public Object onGotoErrorPage() {
        return ErrorPage.class;
    }

    public void onShowEnterComponent() {
        showEnterComponent = true;
    }

    void setValidStoringRequest(final boolean store) {
        this.validStoringRequest = store;
    }

    public void setPageParameter(final MotorRequestState state, final String carId, final String mileage) {
        this.motorRequestState = state;
        this.carId = carId;
        this.mileage = mileage;
    }
}
