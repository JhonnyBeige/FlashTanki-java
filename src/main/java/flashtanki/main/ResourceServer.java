package flashtanki.main;

import com.sun.net.httpserver.HttpServer;
import flashtanki.logger.LoggerService;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import flashtanki.logger.remote.types.LogType;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class ResourceServer {
    static {
        LoggerService.getInstance();
    }

    public static void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/localization/", new LocalizationHandler());
        server.setExecutor(null);
        server.start();
        LoggerService.log(LogType.INFO, "Resource server started");
    }

    static class LocalizationHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String pathStr = "configurations" + exchange.getRequestURI().getPath();
            LoggerService.log(LogType.INFO, "Starting to download resource: " + pathStr);

            try (InputStream resourceStream = getClass().getClassLoader().getResourceAsStream(pathStr)) {
                if (resourceStream != null) {
                    byte[] response = resourceStream.readAllBytes();
                    exchange.sendResponseHeaders(200, response.length);
                    LoggerService.log(LogType.INFO, "Resource found: " + pathStr + " | Size: " + response.length + " bytes");

                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(response);
                        LoggerService.log(LogType.INFO, "Successfully sent resource: " + pathStr);
                    }
                } else {
                    String response = "404 (Not Found)";
                    exchange.sendResponseHeaders(404, response.length());
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(response.getBytes());
                    }
                    LoggerService.log(LogType.WARNING, "Resource not found: " + pathStr);
                }
            } catch (IOException e) {
                LoggerService.log(LogType.ERROR, "Error while handling resource: " + pathStr + " | Exception: " + e.getMessage());
                throw e;
            }
        }
    }
}
