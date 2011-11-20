package com.motorpast.services.captcha;

import java.awt.Color;
import java.awt.Font;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.tapestry5.services.ApplicationStateManager;

import com.motorpast.dataobjects.UserSessionObj;

import nl.captcha.Captcha;
import nl.captcha.servlet.CaptchaServletUtil;
import nl.captcha.text.producer.TextProducer;
import nl.captcha.text.renderer.ColoredEdgesWordRenderer;

public class MileageResultCaptchaService implements CaptchaService
{
    private final ApplicationStateManager applicationStateManager;

    private final List<Color> COLORS;
    private final List<Font> FONTS;


    public MileageResultCaptchaService(final ApplicationStateManager applicationStateManager) {
        this.applicationStateManager = applicationStateManager;

        COLORS = Arrays.asList(
            new Color[] {
                Color.BLACK/*,
                Color.BLUE*/
            }
        );

        FONTS = Arrays.asList(
            new Font[] {
                new Font("Arial", Font.BOLD, 42)
            }
        );
    }

    public void createCaptcha(final HttpServletResponse response) {
        final String storedMileage = applicationStateManager.get(UserSessionObj.class).getMileageResultCaptcha();

        ColoredEdgesWordRenderer wordRenderer = new ColoredEdgesWordRenderer(COLORS, FONTS);

        Captcha captcha = new Captcha.Builder(captchaWidth, captchaHeight)
            .addText(new TextProducer() {
                public String getText() {
                    return storedMileage;
                }
            }, wordRenderer)
            .gimp()
            //.addBackground(new GradiatedBackgroundProducer())
            .build();

        CaptchaServletUtil.writeImage(response, captcha.getImage());
    }
}
