package flashtanki.system;

import flashtanki.captcha.CaptchaService;
import flashtanki.commands.Command;
import flashtanki.commands.Type;
import flashtanki.main.netty.ProtocolTransfer;

public class SystemClientMessagesHandler {
    private static SystemClientMessagesHandler instance;
    private static final CaptchaService captchaService = CaptchaService.getInstance();
    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();


    public static SystemClientMessagesHandler getInstance() {
        if (instance == null) {
            instance = new SystemClientMessagesHandler();
        }
        return instance;
    }

    private SystemClientMessagesHandler() {
    }
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }
    public void executeCommand(Command cmd, ProtocolTransfer protocolTransfer) {
        try {
            if (cmd.args[0].equals("create_captcha_session")) {
                CaptchaService.CreateCaptchaResponse createCaptchaResponse = captchaService.generateCaptcha(CaptchaService.FontStyle.PLAIN);
                if (createCaptchaResponse != null) {
                    byte[] image = createCaptchaResponse.getImage();
                    protocolTransfer.send(Type.SYSTEM, "captcha_session_created",
                            String.valueOf(createCaptchaResponse.getId()),
                            bytesToHex(image));
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
            e.getCause().printStackTrace();
        }
    }

}
