package com.motorpast.services.captcha;

import javax.servlet.http.HttpServletResponse;

import nl.captcha.Captcha;
import nl.captcha.servlet.CaptchaServletUtil;
import nl.captcha.text.producer.TextProducer;

import org.apache.tapestry5.services.ApplicationStateManager;

import com.motorpast.dataobjects.UserSessionObj;


public class MileageResultCaptchaService implements CaptchaService
{
    private final ApplicationStateManager applicationStateManager;


    public MileageResultCaptchaService(final ApplicationStateManager applicationStateManager) {
        this.applicationStateManager = applicationStateManager;
    }


    public void createCaptcha(final HttpServletResponse response) {
        final String storedMileage = applicationStateManager.get(UserSessionObj.class).getMileageResultCaptcha();

        Captcha captcha = new Captcha.Builder(captchaWidth, captchaHeight)
            .addText(new TextProducer() {
                public String getText() {
                    return storedMileage;
                }
            })
            .build();

        CaptchaServletUtil.writeImage(response, captcha.getImage());
    }
}
