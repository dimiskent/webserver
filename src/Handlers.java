import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import jdk.jfr.ContentType;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.Scanner;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

public class Handlers implements HttpHandler {
    public void handle(HttpExchange exchange) throws IOException {
        Response response = getContents(exchange.getRequestURI().toString());
        String reply = response.response;
        Headers responseHeaders = exchange.getResponseHeaders();
        responseHeaders.set("Content-Type", response.contentType);
        exchange.sendResponseHeaders(response.code, reply.length());
        OutputStream stream = exchange.getResponseBody();
        stream.write(reply.getBytes());
        stream.close();
    }
    private Response getContents(String filePath) {
        Response res = new Response(200, "", "text/plain");
        Path path = FileSystems.getDefault().getPath(System.getProperty("user.dir"), "pub", filePath);
        File myFile = new File(path.toUri());
        if(!myFile.exists()) {
            res.code = 404;
            res.response = "Not found :(";
            return res;
        }
        try {
            res.contentType = Files.probeContentType(path);
            Scanner scan = new Scanner(myFile);
            while (scan.hasNextLine()) {
                res.response += scan.nextLine() + "\n";
            }
            scan.close();
            return res;
        } catch (Exception e) {
            // todo handle 500 for java eval
            System.out.println(e.getMessage());
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
