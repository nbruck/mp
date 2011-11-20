package com.motorpast.services.additional;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.apache.tapestry5.TapestryFilter;
import org.apache.tapestry5.ioc.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.motorpast.annotations.UserInputCaptcha;
import com.motorpast.services.captcha.CaptchaService;

/**
 * set to deprecated because its not sure, if we ever need a sessionlistener
 */
@Deprecated
public class MotorSessionListener implements HttpSessionListener
{
    private final Logger logger = LoggerFactory.getLogger(MotorSessionListener.class);


    public void sessionCreated(final HttpSessionEvent event) {
        logger.debug("session {} created", event.getSession().getId());
    }

    public void sessionDestroyed(final HttpSessionEvent event) {
        final HttpSession session = event.getSession();
        final String sessionId = session.getId();

        logger.debug("session {} destroyed", sessionId);

        final ServletContext context = session.getServletContext();
        final Registry registry = (Registry)context.getAttribute(TapestryFilter.REGISTRY_CONTEXT_NAME);

        if(registry == null) {
            logger.debug("Tapestry Registry could not be obtained from the ServletContext. This is a normal case during server shutdown.");
            return;
        }

        final CaptchaService captchaService = registry.getService(UserInputCaptcha.serviceId, CaptchaService.class);

        if(captchaService == null) {
            logger.warn("FrontendDelegate could not be obtained from the ServletContext. SessionListener does could not complete invalidation of the user's SessionData.");
            return;
        }
    }
}
