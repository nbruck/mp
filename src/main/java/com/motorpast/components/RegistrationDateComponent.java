package com.motorpast.components;

import java.util.Calendar;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.apache.tapestry5.Block;
import org.apache.tapestry5.ComponentEventCallback;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.Symbol;

import com.motorpast.additional.MotorApplicationConstants;
import com.motorpast.dataobjects.UserSessionObj;
import com.motorpast.services.business.MotorpastBusinessException;
import com.motorpast.services.business.MotorpastBusinessException.BusinessErrorCode;
import com.motorpast.services.security.MotorpastSecurityException;
import com.motorpast.services.security.MotorpastSecurityException.SecurityErrorCode;
import com.motorpast.services.security.SecurityService;

public class RegistrationDateComponent
{
    @SessionState(create = false)
    private UserSessionObj sessionObj;

    @Inject @Symbol(value = MotorApplicationConstants.MaximumRegistrationYearRange)
    private int yearRange;

    @Inject
    private ComponentResources componentResources;

    @Inject
    private HttpServletRequest httpServletRequest;

    @Inject
    private Messages messages;

    @Inject
    private SecurityService securityService;

    @InjectComponent
    private MotorForm regDateForm;

    @Parameter(required = true)
    @Property(write = false)
    private Block buttonBlock;

    @Property
    private String day, month, year, text1;

    private Object navigate;
    private boolean sessionObjExists;


    void onPrepareForRenderFromRegDateForm() {
        final String token = securityService.generateToken(httpServletRequest);
        text1 = token;
        sessionObj.setUniqueToken(token);
    }

    void onPrepareForSubmitFromRegDateForm() throws MotorpastBusinessException {
        if(!sessionObjExists) {
            throw new MotorpastBusinessException(BusinessErrorCode.session_timeout);
        }
    }

    void onValidateFormFromRegDateForm() throws MotorpastSecurityException {
        if(!text1.equals(sessionObj.getUniqueToken())) {
            throw new MotorpastSecurityException(SecurityErrorCode.error_security);
        }

        if(day == null || "".equals(day) || month == null || "".equals(month) || year == null || "".equals(year)) {
            regDateForm.recordError(messages.get("validation.error.date-not-complete"));
            final ComponentEventCallback<Void> callback = new ComponentEventCallback<Void>()
            {
                @Override
                public boolean handleResult(Void result) {
                    return true;
                }
            };
            componentResources.triggerEvent("handleFailureBubbleUp", new Object[] {}, callback);
            return;
        }

        final ComponentEventCallback<String> callback = new ComponentEventCallback<String>()
        {
            @Override
            public boolean handleResult(final String message) {
                if(message != null) {
                    regDateForm.recordError(message);
                }
                return true;
            }
        };

        componentResources.triggerEvent("handleValidateBubbleUp", new Object[] {day, month, year}, callback);
    }

    Object onSuccessFromRegDateForm() {
        final ComponentEventCallback<Object> callback = new ComponentEventCallback<Object>()
        {
            @Override
            public boolean handleResult(final Object result) {
                navigate = result;
                return true;
            }
        };

        componentResources.triggerEvent("handleSuccessBubbleUp", new Object[] {day, month, year}, callback);
        return navigate;
    }

    public final String[] getDayModel() {
        return createRangeArray(1, 31);
    }
    public final String[] getMonthModel() {
        return createRangeArray(1, 12);
    }
    public final String[] getYearModel() {
        return createYearRangeArray(yearRange);
    }

    final private String[] createRangeArray(final int from, final int to) {
        final String[] rangeArray = new String[to - from  + 1];

        for(int i = from, j = 0; i <= to; i++, j++) {
            rangeArray[j] = String.valueOf(i);
        }

        return rangeArray;
    }

    final private String[] createYearRangeArray(final int yearRange) {
        final Calendar cal = Calendar.getInstance();
        cal.setTime(new Date()); //now

        final int currentyear = cal.get(Calendar.YEAR);

        final String[] years = new String[yearRange + 1];

        for(int i = 0; i <= yearRange; i++) {
            years[i] = String.valueOf(currentyear - i);
        }

        return years;
    }
}
