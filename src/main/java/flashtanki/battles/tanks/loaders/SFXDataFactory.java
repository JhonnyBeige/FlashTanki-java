package flashtanki.battles.tanks.loaders;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class SFXDataFactory {
    public static String data;

    public static String getData() {
        String filePath = "sfx/sfx_data.json";
        StringBuilder stringBuilder = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
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