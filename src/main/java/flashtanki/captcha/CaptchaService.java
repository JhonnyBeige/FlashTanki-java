package flashtanki.captcha;

import flashtanki.logger.RemoteDatabaseLogger;
import flashtanki.services.hibernate.HibernateService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.orclight.java.util.captha.CaptchaClient;
import org.orclight.java.util.captha.bean.CaptchaBean;
import org.orclight.java.util.captha.strategy.SimpleCaptchaStrategy;

import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Random;

public class CaptchaService {
    private static CaptchaService instance;
    private Random r = new Random();

    public static CaptchaService getInstance() {
        if (instance == null) {
            instance = new CaptchaService();
        }
        return instance;
    }

    private CaptchaService() {
    }

    public CreateCaptchaResponse generateCaptcha() {

        int MIN_LENGTH = 4;
        int MAX_LENGTH = 5;
        int lengthCaptcha = r.nextInt(MAX_LENGTH - MIN_LENGTH + 1) + MIN_LENGTH;

        CaptchaClient captchaClient = CaptchaClient.create()
                .captchaStrategy(new SimpleCaptchaStrategy(lengthCaptcha))
                .build();

        CaptchaBean generatedCaptcha = captchaClient.generate();

        Captcha captcha = savetoDb(generatedCaptcha);
        if (captcha == null) {
            return null;
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(generatedCaptcha.getBufferedImage(), "jpg", baos);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return CreateCaptchaResponse.builder()
                .id(captcha.getId())
                .image(baos.toByteArray())
                .build();
    }

    public boolean checkCaptcha(String code, Long id) {
        code = code.toUpperCase();
        Session session = null;
        Transaction tx = null;
        boolean result = false;
        try {
            session = HibernateService.getSessionFactory().getCurrentSession();
            tx = session.beginTransaction();
            List<Captcha> captchas = session.createQuery("from Captcha where id = :id and code = :code")
                    .setParameter("id", id)
                    .setParameter("code", code)
                    .list();

            if (!captchas.isEmpty()) {
                session.delete(captchas.get(0));
                result = true;
            }

            tx.commit();
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            try {
                tx.rollback();
            } catch (Exception ex) {
            }
            RemoteDatabaseLogger.error(e);
            return false;
        }
    }

    private static Captcha savetoDb(CaptchaBean generatedCaptcha) {
        Captcha captcha = new Captcha();
        captcha.setCode(generatedCaptcha.getResult());
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateService.getSessionFactory().getCurrentSession();
            tx = session.beginTransaction();
            Long id = (Long) session.save(captcha);
            captcha.setId(id);
            tx.commit();
        } catch (Exception e) {
            e.printStackTrace();
            try {
                tx.rollback();
            } catch (Exception ex) {
            }
            RemoteDatabaseLogger.error(e);
            return null;
        }
        return captcha;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CreateCaptchaResponse {
        private Long id;
        private byte[] image;
    }
}
