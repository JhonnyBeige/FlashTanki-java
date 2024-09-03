package flashtanki.captcha;

import flashtanki.services.hibernate.HibernateService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.Session;
import org.hibernate.Transaction;

import javax.imageio.ImageIO;
import javax.persistence.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

public class CaptchaService {

    private static volatile CaptchaService instance;
    private final Random random = new Random();
    private static final List<BufferedImage> bufferedImages = new CopyOnWriteArrayList<>();
    private static final String CAPTCHA_IMAGE_DIR = "captchas";
    private static final String FONT_PATH = "captcha_font/Khula-Regular.ttf";

    public static CaptchaService getInstance() {
        if (instance == null) {
            synchronized (CaptchaService.class) {
                if (instance == null) {
                    instance = new CaptchaService();
                }
            }
        }
        return instance;
    }

    private CaptchaService() {
        loadCaptchas();
    }

    public CreateCaptchaResponse generateCaptcha(FontStyle fontStyle) {
        String text = generateRandomText();
        byte[] imageBytes = getCaptcha(text, fontStyle);
        if (imageBytes == null) {
            return null;
        }

        Captcha captcha = saveToDb(text);
        if (captcha == null) {
            return null;
        }

        return CreateCaptchaResponse.builder()
                .id(captcha.getId())
                .image(imageBytes)
                .build();
    }

    public boolean checkCaptcha(String code, Long id) {
        code = code.toUpperCase();
        try (Session session = HibernateService.getSessionFactory().getCurrentSession()) {
            Transaction tx = session.beginTransaction();
            List<Captcha> captchas = session.createQuery("FROM flashtanki.captcha.CaptchaService$Captcha WHERE id = :id AND code = :code", Captcha.class)
                    .setParameter("id", id)
                    .setParameter("code", code)
                    .list();

            boolean result = !captchas.isEmpty();
            if (result) {
                session.delete(captchas.get(0));
            }

            tx.commit();
            return result;
        } catch (Exception e) {
            System.err.println("Captcha validation failed: " + e.getMessage());
            return false;
        }
    }

    private Captcha saveToDb(String code) {
        Captcha captcha = new Captcha();
        captcha.setCode(code);
        try (Session session = HibernateService.getSessionFactory().getCurrentSession()) {
            Transaction tx = session.beginTransaction();
            Long id = (Long) session.save(captcha);
            captcha.setId(id);
            tx.commit();
            return captcha;
        } catch (Exception e) {
            System.err.println("Failed to save captcha to database: " + e.getMessage());
            return null;
        }
    }

    private void loadCaptchas() {
        File dir = new File(CAPTCHA_IMAGE_DIR);
        if (!dir.exists() && !dir.mkdirs()) {
            System.err.println("Failed to create captcha image directory");
        }
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                try {
                    BufferedImage image = ImageIO.read(file);
                    if (image != null) {
                        bufferedImages.add(image);
                    }
                } catch (IOException e) {
                    System.err.println("Failed to load captcha image: " + e.getMessage());
                }
            }
        }
    }

    private int getRandomIni(int min, int max) {
        return random.nextInt((max - min) + 1) + min;
    }

    private BufferedImage deepCopy(BufferedImage bufferedImage) {
        ColorModel colorModel = bufferedImage.getColorModel();
        boolean isAlphaPremultiplied = colorModel.isAlphaPremultiplied();
        WritableRaster raster = bufferedImage.copyData(null);
        return new BufferedImage(colorModel, raster, isAlphaPremultiplied, null);
    }

    private byte[] getCaptcha(String text, FontStyle fontStyle) {
        try (ByteArrayOutputStream byteArray = new ByteArrayOutputStream()) {
            BufferedImage image = deepCopy(bufferedImages.get(getRandomIni(0, bufferedImages.size() - 1)));
            Graphics2D g = (Graphics2D) image.getGraphics();
            try {
                File fontFile = new File(FONT_PATH);
                Font baseFont = Font.createFont(Font.TRUETYPE_FONT, fontFile);
                Font styledFont = baseFont.deriveFont(fontStyle.getStyle(), 40f);
                g.setFont(styledFont);
                g.setColor(Color.BLACK);

                FontMetrics fontMetrics = g.getFontMetrics();
                int textWidth = fontMetrics.stringWidth(text);
                int textHeight = fontMetrics.getHeight();

                int x = (image.getWidth() - textWidth) / 3;
                int y = (image.getHeight() + textHeight) / 3;

                drawText(g, text, x, y);
                drawRandomLines(g, image);

                g.setTransform(new AffineTransform());
            } finally {
                g.dispose();
            }

            ImageIO.write(image, "png", byteArray);
            return byteArray.toByteArray();
        } catch (IOException | FontFormatException e) {
            System.err.println("Error generating captcha: " + e.getMessage());
            return null;
        }
    }

    private void drawText(Graphics2D g, String text, int x, int y) {
        g.setColor(Color.BLACK);

        final double offset = 0.5;
        drawBoldText(g, text, x, y, offset);
    }

    private void drawBoldText(Graphics2D g, String text, int x, int y, double offset) {
        g.setColor(Color.BLACK);
        for (double dx = -offset; dx <= offset; dx += 0.5) {
            for (double dy = -offset; dy <= offset; dy += 0.5) {
                g.drawString(text, (float) (x + dx), (float) (y + dy));
            }
        }
    }

    private void drawRandomLines(Graphics2D g, BufferedImage image) {
        int stickLength = getRandomIni(200, 200);
        double stickWidth = 2.5;

        g.setColor(Color.BLACK);

        int x1 = getRandomIni(0, image.getWidth() - stickLength);
        int y1 = getRandomIni(0, image.getHeight() - (int) stickWidth);
        int x2 = x1 + stickLength + getRandomIni(200, 200);
        int y2 = getRandomIni(0, image.getHeight() - (int) stickWidth);

        if (x2 + stickLength > image.getWidth()) {
            x2 = getRandomIni(0, image.getWidth() - stickLength);
        }

        double stick1Angle = Math.toRadians(getRandomIni(-5, 5));
        double stick2Angle = Math.toRadians(getRandomIni(-5, 5));

        AffineTransform stick1Transform = new AffineTransform();
        stick1Transform.rotate(stick1Angle, x1, y1 + stickWidth / 2);
        g.setTransform(stick1Transform);
        g.fillRect(x1, y1, stickLength, (int) stickWidth);

        AffineTransform stick2Transform = new AffineTransform();
        stick2Transform.rotate(stick2Angle, x2, y2 + stickWidth / 2);
        g.setTransform(stick2Transform);
        g.fillRect(x2, y2, stickLength, (int) stickWidth);

        g.setTransform(new AffineTransform());
    }

    private String generateRandomText() {
        final String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        final int length = 6;

        StringBuilder captchaText = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(characters.length());
            captchaText.append(characters.charAt(index));
        }

        return captchaText.toString();
    }

    public enum FontStyle {
        PLAIN(Font.PLAIN),
        BOLD(Font.BOLD),
        ITALIC(Font.ITALIC),
        BOLD_ITALIC(Font.BOLD | Font.ITALIC);

        private final int style;

        FontStyle(int style) {
            this.style = style;
        }

        public int getStyle() {
            return style;
        }
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CreateCaptchaResponse {
        private Long id;
        private byte[] image;
    }

    @Entity
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Table(name = "captcha")
    public static class Captcha {
        @Id
        @GeneratedValue(generator = "increment")
        //@GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "id")
        private Long id;

        @Column(name = "code")
        private String code;
    }
}
