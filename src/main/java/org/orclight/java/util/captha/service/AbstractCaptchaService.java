package org.orclight.java.util.captha.service;

import org.orclight.java.util.captha.bean.CaptchaBean;
import org.orclight.java.util.captha.strategy.ICaptchaStrategy;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Random;

/**
 * Abstract base class for generating CAPTCHAs with customizable features.
 * Provides default implementations for generating CAPTCHAs with noise and lines.
 * Subclasses should implement additional drawing logic in the `drawOther` method.
 */
public abstract class AbstractCaptchaService implements ICaptchaService {

    // Default configuration values
    private static final int DEFAULT_WIDTH = 285;
    private static final int DEFAULT_HEIGHT = 50;
    private static final int DEFAULT_LINE_NUM = 2;
    private static final float DEFAULT_YAWP = 0.01f;
    private static final Color DEFAULT_COLOR = new Color(83, 168, 177, 255);
    private static final float NOISE_RATE = 0.05f;

    // Random instance for generating random values
    private Random random = new Random();

    // Configuration fields
    private ICaptchaStrategy captchaStrategy;
    private int width = DEFAULT_WIDTH;
    private int height = DEFAULT_HEIGHT;
    private int lineNum = DEFAULT_LINE_NUM;
    private float yawp = DEFAULT_YAWP;
    private Color color = DEFAULT_COLOR;
    private boolean transform = false;

    @Override
    public CaptchaBean generateCaptcha() {
        if (captchaStrategy == null) {
            throw new IllegalStateException("CaptchaStrategy must be set before generating CAPTCHA.");
        }

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics graphics = image.getGraphics();
        try {
            graphics.setColor(color);
            graphics.fillRect(0, 0, width, height);

            mixSource(image);
            CaptchaBean result = drawCode(graphics);
            result.setBufferedImage(image);
            return result;
        } finally {
            graphics.dispose();
        }
    }

    private void mixSource(BufferedImage image) {
        drawPoint(image);
        drawLine(image);
        drawOther(image);
    }

    private void drawLine(BufferedImage image) {
        Graphics graphics = image.getGraphics();
        try {
            for (int i = 0; i < lineNum; i++) {
                int xs = random.nextInt(width);
                int ys = random.nextInt(height);
                int xe = xs + random.nextInt(width);
                int ye = ys + random.nextInt(height);
                graphics.setColor(getRandColor(1, 255));
                graphics.drawLine(xs, ys, xe, ye);
            }
        } finally {
            graphics.dispose();
        }
    }

    private void drawPoint(BufferedImage image) {
        int area = (int) (NOISE_RATE * width * height);
        for (int i = 0; i < area; i++) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            image.setRGB(x, y, random.nextInt(255));
        }
    }

    /**
     * Abstract method for drawing additional elements on the CAPTCHA image.
     * Subclasses should implement this method to add custom elements.
     *
     * @param image The CAPTCHA image on which to draw.
     */
    public abstract void drawOther(BufferedImage image);

    private CaptchaBean drawCode(Graphics graphics) {
        CaptchaBean captcha = captchaStrategy.generateCode();
        Font font = getFont(20);
        graphics.setFont(font);

        if (captcha != null && captcha.getCodeArray() != null && captcha.getResult() != null && captcha.getCodeArray().length > 0) {
            for (int i = 0; i < captcha.getCodeArray().length; i++) {
                String code = String.valueOf(captcha.getCodeArray()[i]);

                if (transform) {
                    AffineTransform fontAT = new AffineTransform();
                    int rotate = random.nextInt(25);
                    fontAT.rotate(random.nextBoolean() ? Math.toRadians(rotate) : -Math.toRadians(rotate / 2));
                    Font fx = new Font(new String[]{"Times New Roman", "Verdana", "Arial"}[random.nextInt(3)], random.nextInt(5),
                            18 + random.nextInt(8)).deriveFont(fontAT);
                    graphics.setFont(fx);
                }

                graphics.setColor(getRandColor(1, 255));
                graphics.drawString(code, (i * width / captcha.getCodeArray().length) + 5, height / 2 + random.nextInt(height / 4));
            }
        }
        return captcha;
    }

    private Color getRandColor(int fc, int bc) {
        if (fc > 255) fc = 255;
        if (bc > 255) bc = 255;
        int r = fc + random.nextInt(bc - fc);
        int g = fc + random.nextInt(bc - fc);
        int b = fc + random.nextInt(bc - fc);
        return new Color(r, g, b);
    }

    private Font getFont(int size) {
        Font[] fonts = {
                new Font("Ravie", Font.BOLD, size),
                new Font("Antique Olive Compact", Font.BOLD, size),
                new Font("Fixedsys", Font.BOLD, size),
                new Font("Wide Latin", Font.BOLD, size),
                new Font("Gill Sans Ultra Bold", Font.BOLD, size)
        };
        return fonts[random.nextInt(fonts.length)];
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getLineNum() {
        return lineNum;
    }

    public void setLineNum(int lineNum) {
        this.lineNum = lineNum;
    }

    public float getYawp() {
        return yawp;
    }

    public void setYawp(float yawp) {
        this.yawp = yawp;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public boolean isTransform() {
        return transform;
    }

    public void setTransform(boolean transform) {
        this.transform = transform;
    }

    public ICaptchaStrategy getCaptchaStrategy() {
        return captchaStrategy;
    }

    public void setCaptchaStrategy(ICaptchaStrategy captchaStrategy) {
        this.captchaStrategy = captchaStrategy;
    }
}
