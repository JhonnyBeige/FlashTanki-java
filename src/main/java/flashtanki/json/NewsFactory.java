package flashtanki.json;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class NewsFactory {
    public static String data;

    public static String getData() {
        String filePath = "news/LobbyNews.json";
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