package com.motorpast.services.captcha;

import javax.servlet.http.HttpServletResponse;


public interface CaptchaService
{
    final static int captchaWidth = 170;
    final static int captchaHeight = 50;

    /**
     * creates captcha and stores answer into session
     * @param response gets the image
     */
    public void createCaptcha(final HttpServletResponse response);
}
