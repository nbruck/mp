package com.motorpast.components;

import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.corelib.components.TextField;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.slf4j.Logger;

import com.motorpast.additional.MotorpastException;
import com.motorpast.base.BaseRenderable;
import com.motorpast.dataobjects.UserSessionObj;
import com.motorpast.pages.ConfirmationPage;
import com.motorpast.pages.HelpPage;
import com.motorpast.services.business.MotorpastBusinessException;
import com.motorpast.services.business.MotorpastBusinessException.BusinessErrorCode;
import com.motorpast.services.business.ValidationService;
import com.motorpast.services.security.MotorpastSecurityException;
import com.motorpast.services.security.MotorpastSecurityException.SecurityErrorCode;

/**
 * text1 = fake, text2 = fake;
 * text3 = carid, text4 = mileage
 */
public class MileageEnterComponent extends BaseRenderable
{
    @Parameter
    private String carIdParameter;

    @Parameter
    private String headerLabel;

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
    private ConfirmationPage confirmationPage;

    @InjectComponent
    private MotorForm mileEnterForm;

    @InjectComponent(value = "text3")
    private TextField vinenter;

    @InjectComponent(value = "text4")
    private TextField mileageenter;

    @Property
    private String text1, text2; // both fields are fake

    @Property
    private String text3, text4; // carid and mileage

    private boolean sessionObjExists;
    private String carId, mileage;


    void onPrepareForRenderFromMileEnterForm() {
        if(carIdParameter != null) {
            text3 = carIdParameter;
        } else {
            text3 = messages.get("txt.input.vin.placeholder");
        }
        text4 = messages.get("global.txt.input.mileage.placeholder");
    }

    void onPrepareForSubmitFromMileEnterForm() throws MotorpastBusinessException {
        if(!sessionObjExists) {
            throw new MotorpastBusinessException(BusinessErrorCode.session_timeout);
        }
    }

    void onValidateFormFromMileEnterForm() throws MotorpastSecurityException {
        logger.debug("received values from mileEnterForm:  text1=" + text1 + ", text2=" + text2 + ", text3=" + text3 + 
                ", text4=" + text4 + " ...end");

        if(text1 != null || text2 != null) {
            throw new MotorpastSecurityException(SecurityErrorCode.error_security,
                    "hidden fake-textfield has been filled with value=%s and %s", text1, text2);
        }

        if(messages.get("txt.input.vin.placeholder").equals(text3)) {
            text3 = null;
        }
        if(messages.get("global.txt.input.mileage.placeholder").equals(text4)) {
            text4 = null;
        }
        carId = text3 != null ? text3.toUpperCase() : null;
        mileage = text4;

        //carid is required in both cases - search or new entry in DB
        if((carId == null || carId.isEmpty()) && (mileage == null || mileage.isEmpty())) {
            mileEnterForm.recordError(vinenter, messages.get("error.values.both-required"));
            mileEnterForm.recordError(mileageenter, ".");
            return;
        } else if(carId == null || carId.isEmpty()) {
            mileEnterForm.recordError(vinenter, messages.get("error.carId.required"));
            return;
        } else if(mileage == null || mileage.isEmpty()) {
            mileEnterForm.recordError(mileageenter, messages.get("error.mileage.required"));
            return;
        }

        if(!validationService.validateCarId(carId)) {
            mileEnterForm.recordError(vinenter, messages.get("error.carId.regex-invalid"));
            return;
        }
        if(!validationService.validateMileage(mileage)) {
            mileEnterForm.recordError(mileageenter, messages.get("error.mileage.no-number"));
            return;
        }
    }

    Object onSuccessFromMileEnterForm() throws MotorpastException {
        confirmationPage.setPageParameter(null, carId, mileage);
        return confirmationPage;
    }

    public Object[] getToolTipParameter() {
        return new Object[] {
            pageRenderLinkSource.createPageRenderLink(HelpPage.class).toURI()
        };
    }

    public String getHeaderLabel() {
        return headerLabel != null ? headerLabel : messages.get("txt.fieldset.legend.mileage-enter");
    }
}
