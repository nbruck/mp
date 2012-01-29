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
import com.motorpast.services.security.MotorpastSecurityException;
import com.motorpast.services.security.MotorpastSecurityException.SecurityErrorCode;
import com.motorpast.services.security.SecurityService;

/**
 * mixin can be only used on forms to add a field for preventing CSRF
 * @author dgerhardt
 */
@MixinAfter
public class UniqueToken
{
    private final static String UniqueTokenKey = "motorUniqueToken";


    @SessionState
    private UserSessionObj sessionObj;

    @Inject
    private HttpServletRequest httpServletRequest;

    @Inject
    private Logger logger;

    @Inject
    private SecurityService securityService;

    private String token;


    @SetupRender
    void generateToken() {
        if(sessionObj.getUniqueToken() == null) {
            token = securityService.generateToken(httpServletRequest);
            sessionObj.setUniqueToken(token);
        } else {
            token = sessionObj.getUniqueToken();
        }
    }

    @BeforeRenderTemplate
    void createHiddenField(MarkupWriter writer) {
        writer.element("input",
                       "type", "hidden",
                       "name", UniqueTokenKey,
                       "value", token);
        writer.end();
    }

    /**
     * validate is called before enclosing formcomponents validate-eventmethod
     * @throws MotorpastSecurityException
     */
    void onValidateForm() throws MotorpastSecurityException {
        final String tokenFromSession = sessionObj.getUniqueToken();
        final String tokenFromSubmit = httpServletRequest.getParameter(UniqueTokenKey);

        sessionObj.setUniqueToken(null);                        //reset
        httpServletRequest.setAttribute(UniqueTokenKey, null);  //reset
        logger.debug("token from session=" + tokenFromSession + ", token from submitRequest=" + tokenFromSubmit);

        if(tokenFromSession == null || tokenFromSubmit == null || ! tokenFromSubmit.equals(tokenFromSession)) {
            throw new MotorpastSecurityException(SecurityErrorCode.error_security);
        }
    }
}
