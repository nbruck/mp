package com.motorpast.services.captcha;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;

import org.apache.tapestry5.services.ApplicationStateManager;

import com.motorpast.dataobjects.UserSessionObj;


public class MileageResultCaptchaService implements CaptchaService
{
    private final Color white;
    private final Font captchaFont;
    private final ApplicationStateManager applicationStateManager;


    public MileageResultCaptchaService(final ApplicationStateManager applicationStateManager) {
        this.applicationStateManager = applicationStateManager;
        white = new Color(1.0f, 1.0f, 1.0f);
        captchaFont = new Font("ARIAL", Font.CENTER_BASELINE , captchaFontSize);
    }


    public void createCaptcha(final HttpServletResponse response) {
        BufferedImage image = new BufferedImage(captchaWidth, captchaHeight, BufferedImage.TYPE_INT_RGB); 
        Graphics2D graphics2D = image.createGraphics();
        graphics2D.setColor(white);

        final String storedMileage = applicationStateManager.get(UserSessionObj.class).getMileageResultCaptcha();

        graphics2D.setFont(captchaFont);
        graphics2D.drawString(storedMileage, 4, captchaFontSize);
        graphics2D.dispose();

        writeImage(response, image);
    }

    private void writeImage(final HttpServletResponse response, final BufferedImage bufferedImage) {
        response.setHeader("Cache-Control", "private,no-cache,no-store");
        response.setContentType("image/png");   // PNGs allow for transparency. JPGs do not.
        try {
            writeImage(response.getOutputStream(), bufferedImage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeImage(final OutputStream outputStream, final BufferedImage bufferedImage) {
        try {
            ImageIO.write(bufferedImage, "png", outputStream);
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
