import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.util.Scanner;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import javax.script.ScriptEngine;

public class Handlers implements HttpHandler {
    public void handle(HttpExchange exchange) throws IOException {
        Response response = getContents(exchange.getRequestURI().toString());
        String reply = response.response;
        exchange.sendResponseHeaders(response.code, reply.length());
        OutputStream stream = exchange.getResponseBody();
        stream.write(reply.getBytes());
        stream.close();
    }
    private Response getContents(String filePath) {
        Response res = new Response(200, "");
        Path path = FileSystems.getDefault().getPath(System.getProperty("user.dir"), "pub", filePath);
        File myFile = new File(path.toUri());
        if(!myFile.exists()) {
            res.code = 404;
            res.response = "Not found :(";
            return res;
        }
        try {
            Scanner scan = new Scanner(myFile);
            res.response = scan.nextLine();
            scan.close();
            return res;
        } catch (Exception e) {
            // todo handle 500 for java eval
            String errorType = e.getClass().toString().split(" ")[1];
            switch (errorType) {
                case "java.io.FileNotFoundException":
                    res.code = 403;
                    res.response = "Forbidden >:(";
                    break;
                default:
                    res.code = 500;
                    res.response = "Unknown Error :0";
            }
            return res;
        }
    }
}
