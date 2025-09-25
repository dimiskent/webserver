import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;

public class Main {
    public static void main(String[] args) throws IOException {
        int port = args.length == 0 ? 8080 : Integer.parseInt(args[0]);
        System.out.println("Website should start at http://localhost:" + port);
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 1);
        server.createContext("/", new Handlers());
        server.start();
    }
}
