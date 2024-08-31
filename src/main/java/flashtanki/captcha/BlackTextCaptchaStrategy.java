package flashtanki.captcha;

import java.awt.image.BufferedImage;

public interface BlackTextCaptchaStrategy {
    BufferedImage renderImage(String captchaText);
}
