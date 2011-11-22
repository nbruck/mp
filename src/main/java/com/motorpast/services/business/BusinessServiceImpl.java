package com.motorpast.services.business;

import static com.motorpast.additional.MotorRequestState.CarTupelNoAttemptsLeftAndBlocked;
import static com.motorpast.additional.MotorRequestState.SaveCompleteNewCarTupel;
import static com.motorpast.additional.MotorRequestState.UpdateCarTupelWhichDoesntAlreadyHaveRegistrationDate;
import static com.motorpast.additional.MotorRequestState.UpdateCarTupelWithAttemptsLeft;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.tapestry5.services.ApplicationStateManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.motorpast.additional.MotorRequestState;
import com.motorpast.additional.MotorUtils;
import com.motorpast.dataobjects.CarData;
import com.motorpast.dataobjects.CarMileage;
import com.motorpast.dataobjects.UserSessionObj;
import com.motorpast.dataobjects.hibernate.CarDataEntity;
import com.motorpast.services.business.MotorpastBusinessException.BusinessErrorCode;
import com.motorpast.services.business.trustrules.RuleSet;
import com.motorpast.services.business.trustrules.fiftyone.FiftyOnePercentRuleSet;
import com.motorpast.services.persistence.MotorpastPersistenceException;
import com.motorpast.services.persistence.PersistenceService;

public class BusinessServiceImpl implements BusinessService<CarData, HttpServletRequest>
{
    private final ApplicationStateManager applicationStateManager;
    private final PersistenceService<CarData> persistenceService;
    private final Logger logger;

    private final int initialAttemptCount;
    private final boolean showTrustLevel;

    private final Map<String, String> dateFormatStrings;
    private final Object[] dummyArray;


    public BusinessServiceImpl(
        final PersistenceService<CarData> persistenceService,
        final ApplicationStateManager applicationStateManager,
        final int initialAttemptCount,
        final boolean showTrustLevel
    ) {
        this.persistenceService = persistenceService;
        this.applicationStateManager = applicationStateManager;
        this.initialAttemptCount = initialAttemptCount;
        this.showTrustLevel = showTrustLevel;
        this.logger = LoggerFactory.getLogger(BusinessServiceImpl.class);

        dateFormatStrings = new HashMap<String, String>(2);
        dateFormatStrings.put("de", "dd.MM.yyyy");
        dateFormatStrings.put("en", "yyyy-MM-dd");

        dummyArray = new Object[] {null};
    }

    @Override
    public CarData doSystemRequest(final String carId, final String miles, final HttpServletRequest request) throws MotorpastPersistenceException, MotorpastBusinessException {
        logger.info("doSystemRequest called with values: carId=" + carId + ", mileage=" + miles);

        // both values have been set - store or update
        if(isStoringRequest(carId, miles)) {
            final int mileage = Integer.valueOf(miles);
            // first check for existence
            CarData selectedCarData;
            try {
                selectedCarData = getDataForCarId(carId);
            } catch(MotorpastPersistenceException e) {
                logger.debug("setting cardata to null for carid=" + carId);
                selectedCarData = null; // logging is done in persistence-method
            }

            final String ip = MotorUtils.getIP(request);
            final String hostInfo = MotorUtils.getUserAgent(request);
            logger.debug("save cardata -> ip=" + ip);
            logger.debug("save cardata -> hostInfo=" + hostInfo);

            // simpliest case - create new car entry
            if(selectedCarData == null) {
                final CarData newCardata = saveNewCarData(new CarDataEntity(
                      carId, mileage,
                      new Date(), null, null,
                      initialAttemptCount,
                      false
                ),ip, hostInfo);

                setLastMileageForDisplaying(newCardata.getLastMileage());
                return newCardata;
            } else /*if(selectedCarData.getRegistrationdate() == null)*/ { // car exists but no regdate formerly stored //TODO: remove uncomment code if it works
                final CarData carData = persistenceService.updateCarData(selectedCarData, new Date(), mileage, ip, hostInfo);
                setLastMileageForDisplaying(carData.getLastMileage());
                return carData;
            }
        // only carId has been set so we return the mileage for given carId
        } else if(isViewRequest(carId, miles)) {
            final CarData carData = getDataForCarId(carId);

            setLastMileageForDisplaying(carData.getLastMileage());

            //set display trustlevel
            final UserSessionObj sso = applicationStateManager.get(UserSessionObj.class);
            sso.setTrustLevel(getTrustLevel(carData, carId));

            return carData;
        } else {
            logger.error("doSystemRequest() is called with wrong parameter settings");
            throw new MotorpastBusinessException(BusinessErrorCode.system_error);
        }
    }

    private void setLastMileageForDisplaying(final int lastMileage) {
        final UserSessionObj sso = applicationStateManager.get(UserSessionObj.class);
        sso.setMileageResultCaptcha(String.valueOf(lastMileage));
    }

    private<P extends CarData> CarData saveNewCarData(P carData, final String ip, final String hostName) throws MotorpastPersistenceException {
        return persistenceService.saveNewCarData(carData, ip, hostName);
    }

    private CarData getDataForCarId(final String carId) throws MotorpastPersistenceException {
        return persistenceService.getDataForCarId(carId);
    }

    @Override
    public String getTrustLevel(final CarData carData, final String carId) throws MotorpastPersistenceException {
        if(!showTrustLevel && !carData.isShowTrustLevel()) {
            return "";
        }

        List<CarMileage> mileages = persistenceService.getAllMileageDataForCar(carId);
        RuleSet ruleSet = new FiftyOnePercentRuleSet(carData, mileages);

        ruleSet.calculateTrustLevel();
        final String trustLevel = ruleSet.getFormattedTrustLevel();

        logger.debug("received trustlevel=" + trustLevel + " for carId=" + carId);
        return trustLevel;
    }

    @Override
    public boolean isViewRequest(final String carId, final String mileage) {
        return carId != null && !"".equals(carId) && mileage == null || "".equals(mileage);
    }

    @Override
    public boolean isStoringRequest(final String carId, final String mileage) {
        return carId != null && !"".equals(carId) && mileage != null && !"".equals(mileage);
    }

    @Override
    public MotorRequestState getMotorRequestStateByCarData(final CarData carData) {
        if(carData == null) {
            return SaveCompleteNewCarTupel;
        } else if(carData.getRegistrationdate() == null) {
            return UpdateCarTupelWhichDoesntAlreadyHaveRegistrationDate;
        } else if(carData.getAttemptsLeft() > 0) { // here regdate always exists
            return UpdateCarTupelWithAttemptsLeft;
        } else if(carData.getAttemptsLeft() == 0) {
            return CarTupelNoAttemptsLeftAndBlocked;
        } else {
            return null;
        }
    }

    @Override
    public String getFormattedDate(final Date dateToFormat, final String languageIso2) {
        DateFormat formatter = new SimpleDateFormat(dateFormatStrings.get(languageIso2));
        return formatter.format(dateToFormat);
    }

    @Override
    public Object[] getRedirectNullCheck() {
        return dummyArray;
    }
}
