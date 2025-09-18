import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executor;

public class Main {
    public static void main(String[] args) throws IOException {
        int port = args.length == 0 ? 8080 : Integer.parseInt(args[0]);
        System.out.println("Website should start at http://localhost:" + port);
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 1);
        server.createContext("/", new Handlers());
        server.start();
    }
}
