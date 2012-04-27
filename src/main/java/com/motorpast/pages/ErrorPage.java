package com.motorpast.pages;

import org.apache.tapestry5.PersistenceConstants;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.annotations.SetupRender;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.ExceptionReporter;

import com.motorpast.additional.MotorUtils;
import com.motorpast.additional.MotorpastException;
import com.motorpast.annotations.UrlRewrite;
import com.motorpast.base.BasePage;
import com.motorpast.dataobjects.UserSessionObj;

@UrlRewrite(
    mappings={
        "/de/errorpage", "/fehler" /*"/en/errorpage", "/error"*/
    },
    putInSitemap = false
)
public class ErrorPage extends BasePage implements ExceptionReporter
{
    @SessionState
    private UserSessionObj sessionObj;

    @Inject
    private Messages messages;

    @InjectPage
    private ConfirmationPage confirmationPage;

    @InjectPage
    private ResultPage resultPage;

    @Property
    @Persist(PersistenceConstants.FLASH)
    private String message;


    public String getPageName() {
        return ErrorPage.class.getSimpleName();
    }

    @SetupRender
    void initErrorpage() {
        confirmationPage.setPageParameter(null, null, null);
        resultPage.setPageParameter(null, null, null);

        if(sessionObj != null) {
            sessionObj.setMileageResultCaptcha(null);
            sessionObj.setTrustLevel(null);
            sessionObj.setUniqueToken(null);
            sessionObj.setTimestamp(null);
        }
    }

    @Override
    public void reportException(Throwable exception) {
        if(exception instanceof MotorpastException) {
            message = messages.get(MotorUtils.errorCodeToMessageString((MotorpastException)exception));
            if(message.startsWith("[[m")) {             // if no translation found - show default
                message = messages.get("error.unexpected");
            }
        } else {
            message = messages.get("error.unexpected"); // simply default message
        }
    }
}
