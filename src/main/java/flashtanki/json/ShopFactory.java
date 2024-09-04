package flashtanki.json;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class ShopFactory {
    public static String data;

    public static String getData() {
        String file = "shop/Shop.json";
        StringBuilder stringBuilder = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        data = stringBuilder.toString();
        return data;
    }
}