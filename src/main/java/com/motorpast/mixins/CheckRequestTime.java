package com.motorpast.mixins;

import javax.servlet.http.HttpServletRequest;

import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.annotations.BeforeRenderTemplate;
import org.apache.tapestry5.annotations.MixinAfter;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionAttribute;
import org.apache.tapestry5.annotations.SetupRender;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.slf4j.Logger;

import com.motorpast.services.business.ValidationService;
import com.motorpast.services.security.MotorpastSecurityException;
import com.motorpast.services.security.MotorpastSecurityException.SecurityErrorCode;
import com.motorpast.services.security.SecurityService;

/**
 * creates and check timestamp for spambot-detection
 * @author dgerhardt
 */
@MixinAfter
public class CheckRequestTime
{
    private final static String TimeStampSubmitToken = "TimeStampSubmitToken";
    public final static String TimeStampSessionToken = "TimeStampSessionToken";

    @Inject
    private HttpServletRequest httpServletRequest;

    @Inject
    private Logger logger;

    @Inject
    private SecurityService securityService;

    @Inject
    private ValidationService validationService;

    @SessionAttribute(value = TimeStampSessionToken)
    private String sessionTimeStamp;

    @Property
    private String submitTimeStamp;


    @SetupRender
    void generateTimeStamp() {
        if(sessionTimeStamp == null) {
            sessionTimeStamp = submitTimeStamp = String.valueOf(securityService.generateTimestamp());
        }
    }

    @BeforeRenderTemplate
    void createHiddenField(MarkupWriter writer) {
        writer.element("input",
                       "type", "hidden",
                       "name", TimeStampSubmitToken,
                       "value", sessionTimeStamp);
        writer.end();
    }

    /**
     * method is called before enclosing formcomponents success-eventmethod
     * @throws MotorpastSecurityException
     */
    public void onSuccess() throws MotorpastSecurityException {
        final String timeStampFromSession = sessionTimeStamp;
        final String timeStampFromSubmit = httpServletRequest.getParameter(TimeStampSubmitToken);

        sessionTimeStamp = null;
        logger.debug("token from session=" + timeStampFromSession + ", token from submitRequest=" + timeStampFromSubmit);

        // check date for spambot detection
        if(timeStampFromSubmit == null || "".equals(timeStampFromSubmit)                        // field is missing -> spam
            || ! validationService.validateNumeric(timeStampFromSubmit)                         // manipulation
            || ! timeStampFromSubmit.equals(timeStampFromSession)                               // manipulation
            || Long.valueOf(timeStampFromSubmit) >= securityService.generateCheckTimestamp()    // too fast -> spam
        ) {
            throw new MotorpastSecurityException(SecurityErrorCode.error_security,
                    "security request-time doesn't match or too fast! ours=%s, theirs=%s", timeStampFromSession, timeStampFromSubmit);
        }
    }
}
