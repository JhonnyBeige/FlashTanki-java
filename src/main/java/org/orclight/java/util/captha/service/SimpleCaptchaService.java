package org.orclight.java.util.captha.service;

import org.orclight.java.util.captha.strategy.ICaptchaStrategy;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Simple implementation of a CAPTCHA service.
 * Adds custom drawing elements to the CAPTCHA image as defined by the drawOther method.
 */
public class SimpleCaptchaService extends AbstractCaptchaService {

    public SimpleCaptchaService(int width, int height, int lineNum, float yawp,
                                Color color, ICaptchaStrategy captchaStrategy, boolean transform) {
        setWidth(width);
        setHeight(height);
        setLineNum(lineNum);
        setYawp(yawp);
        setColor(color);
        setTransform(transform);
        setCaptchaStrategy(captchaStrategy);
    }

    @Override
    public void drawOther(BufferedImage image) {
        // Implement custom drawing logic here
        // Example: Draw a simple shape or pattern on the CAPTCHA image
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.setColor(Color.RED);
            graphics.drawOval(10, 10, 50, 50); // Draw an oval as an example
        } finally {
            graphics.dispose();
        }
    }
}
