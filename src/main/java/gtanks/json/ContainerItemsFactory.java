package gtanks.json;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class ContainerItemsFactory {
    public static String data;

    public static String getData() {
        String file = "container/Container.json";
        StringBuilder stringBuilder = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line).append("");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        data = stringBuilder.toString();
        return data;
    }
}