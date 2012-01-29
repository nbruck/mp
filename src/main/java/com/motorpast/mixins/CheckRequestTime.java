package com.motorpast.mixins;

import javax.servlet.http.HttpServletRequest;

import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.annotations.BeforeRenderTemplate;
import org.apache.tapestry5.annotations.MixinAfter;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.annotations.SetupRender;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.slf4j.Logger;

import com.motorpast.dataobjects.UserSessionObj;
import com.motorpast.services.business.ValidationService;
import com.motorpast.services.security.MotorpastSecurityException;
import com.motorpast.services.security.SecurityService;
import com.motorpast.services.security.MotorpastSecurityException.SecurityErrorCode;

/**
 * creates and check timestamp for spambot-detection
 * @author dgerhardt
 */
@MixinAfter
public class CheckRequestTime
{
    private final static String DateTokenKey = "timeStampToken";


    @SessionState
    private UserSessionObj sessionObj;

    @Inject
    private HttpServletRequest httpServletRequest;

    @Inject
    private Logger logger;

    @Inject
    private SecurityService securityService;

    @Inject
    private ValidationService validationService;

    private String timeStamp;


    @SetupRender
    void generateTimeStamp() {
        if(sessionObj.getTimestamp() == null) {
            timeStamp = String.valueOf(securityService.generateTimestamp());
            sessionObj.setTimestamp(timeStamp);
        } else {
            timeStamp = sessionObj.getTimestamp();
        }
    }

    @BeforeRenderTemplate
    void createHiddenField(MarkupWriter writer) {
        writer.element("input",
                       "type", "hidden",
                       "name", DateTokenKey,
                       "value", timeStamp);
        writer.end();
    }

    /**
     * validate is called before enclosing formcomponents validate-eventmethod
     * @throws MotorpastSecurityException
     */
    //void onValidateForm() throws MotorpastSecurityException {
    public void validateRequestTime() throws MotorpastSecurityException {
        final String timeStampFromSession = sessionObj.getTimestamp();
        final String timeStampFromSubmit = httpServletRequest.getParameter(DateTokenKey);

        sessionObj.setTimestamp(null);                        //reset
        httpServletRequest.setAttribute(DateTokenKey, null);  //reset
        logger.debug("token from session=" + timeStampFromSession + ", token from submitRequest=" + timeStampFromSubmit);

        // check date for spambot detection
        if(timeStampFromSubmit == null || "".equals(timeStampFromSubmit)                        // field is missing -> spam
            || ! validationService.validateNumeric(timeStampFromSubmit)                         // manipulation
            || ! timeStampFromSubmit.equals(timeStampFromSession)                               // manipulation
            || Long.valueOf(timeStampFromSubmit) >= securityService.generateCheckTimestamp()    // too fast -> spam
        ) {
            throw new MotorpastSecurityException(SecurityErrorCode.error_security);
        }
    }
}
