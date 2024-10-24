package flashtanki.main;

import com.sun.net.httpserver.HttpServer;
import flashtanki.logger.LoggerService;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import flashtanki.logger.remote.types.LogType;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;

public class ResourceServer {

    public static void start() throws IOException {
        HttpServer server = HttpServer.create(new java.net.InetSocketAddress(8080), 0);
        server.createContext("/resources/", new LocalizationHandler());
        server.setExecutor(null);
        server.start();
        LoggerService.log(LogType.INFO,"Resource server started");
    }

    static class LocalizationHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String pathStr = "configurations" + exchange.getRequestURI().getPath();
            System.out.println(pathStr);
            File file = new File(pathStr);

            if (file.exists()) {
                byte[] response = Files.readAllBytes(file.toPath());
                exchange.sendResponseHeaders(200, response.length);
                OutputStream os = exchange.getResponseBody();
                os.write(response);
                os.close();
                LoggerService.log(LogType.INFO,"The localization file (" + file.getPath() + ") was successfully sent");
            } else {
                String response = "404 (Not Found)";
                exchange.sendResponseHeaders(404, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
                LoggerService.log(LogType.INFO,"The localization file (" + file.getPath() + ") not found");
            }
        }
    }
}
