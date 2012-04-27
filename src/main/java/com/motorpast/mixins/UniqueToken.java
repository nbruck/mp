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
    private final static String UniqueSubmitToken = "UniqueSubmitToken";
    public final static String UniqueSessionToken = "UniqueSessionToken";

    @Inject
    private HttpServletRequest httpServletRequest;

    @Inject
    private Logger logger;

    @Inject
    private SecurityService securityService;

    @SessionAttribute(value = UniqueSessionToken)
    private String sessionToken;

    @Property
    private String submitToken;


    @SetupRender
    void generateToken() {
        if(sessionToken == null) {
            sessionToken = submitToken = securityService.generateToken();
        }
    }

    @BeforeRenderTemplate
    void createHiddenField(MarkupWriter writer) {
        writer.element("input",
                       "type", "hidden",
                       "name", UniqueSubmitToken,
                       "value", sessionToken);
        writer.end();
    }

    /**
     * validate is called before enclosing formcomponents validate-eventmethod
     * @throws MotorpastSecurityException
     */
    void onValidateForm() throws MotorpastSecurityException {
        final String tokenFromSession = sessionToken;
        final String tokenFromSubmit = httpServletRequest.getParameter(UniqueSubmitToken);

        sessionToken = null;

        logger.debug("token from session=" + tokenFromSession + ", token from submitRequest=" + tokenFromSubmit);

        if(tokenFromSession == null || tokenFromSubmit == null || ! tokenFromSubmit.equals(tokenFromSession)) {
            throw new MotorpastSecurityException(SecurityErrorCode.error_security,
                    "unique token doesn't match! ours=%s, theirs=%s", tokenFromSession, tokenFromSubmit);
        }
    }
}
