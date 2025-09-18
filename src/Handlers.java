import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import jdk.jfr.ContentType;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Scanner;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

public class Handlers implements HttpHandler {
    public void handle(HttpExchange exchange) throws IOException {
        String fullUrlPath = exchange.getRequestURI().toString();
        String[] maker = fullUrlPath.split("\\?");
        String safeParam = maker.length != 2 ? null : maker[1];
        Response response = getContents(maker[0], safeParam);
        String reply = response.response;
        System.out.println(exchange.getRequestURI());
        Headers responseHeaders = exchange.getResponseHeaders();
        responseHeaders.set("Content-Type", response.contentType);
        exchange.sendResponseHeaders(response.code, reply.length());
        OutputStream stream = exchange.getResponseBody();
        stream.write(reply.getBytes());
        stream.close();
    }
    private Response getContents(String filePath, String getParams) {
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
            if(filePath.endsWith("class")) {
                res.contentType = "text/html";
                String fileName = path.getFileName().toString().split("\\.")[0];
                String command = "java -cp \"" + path.getParent().toString() + "\" " + fileName;
                if(getParams != null) {
                    command += " " + getParams;
                }
                System.out.println(fileName);
                System.out.println(command);
                Process p2 = Runtime.getRuntime().exec(command);
                BufferedReader br=new BufferedReader(new InputStreamReader(p2.getInputStream()));
                BufferedReader errorbr=new BufferedReader(new InputStreamReader(p2.getErrorStream()));

                String out, err, errorString = null;
                while( (out=br.readLine())!=null)
                {
                    res.response += out + "<br>";
                }
                while( (err=errorbr.readLine())!=null)
                {
                    errorString += err + "<br>";
                }
                if(errorString != null) {
                    // goes to exception!
                    res.code = 500;
                    res.response = "<h1><font color=red>ERROR</font></h1>" + errorString;
                    return res;
                }
                p2.waitFor();
            }
            return res;
        } catch (Exception e) {
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
