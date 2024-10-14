package flashtanki.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

public class ResourceServer {
    private static final int PORT = 7070;
    private static final String RESOURCE_DIR = "resources";

    public static void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/resources", new ResourceHandler());
        server.setExecutor(null);
        System.out.println("ResourceServer started on port: " + PORT);
        server.start();
    }

    static class ResourceHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String requestURI = exchange.getRequestURI().toString();
            String filePath = RESOURCE_DIR + requestURI.replace("/resources", "");

            System.out.println("Requested URI: " + requestURI);
            System.out.println("Full file path: " + filePath);

            File file = new File(filePath);
            if (!file.exists() || file.isDirectory()) {
                System.out.println("File not found: " + filePath);
                exchange.sendResponseHeaders(404, -1);
                return;
            }

            try (FileInputStream fis = new FileInputStream(file);
                 OutputStream os = exchange.getResponseBody()) {
                exchange.sendResponseHeaders(200, file.length());
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
            } catch (IOException e) {
                exchange.sendResponseHeaders(500, -1);
                e.printStackTrace();
            }
        }
    }
}