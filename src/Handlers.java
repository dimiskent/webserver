import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.*;
import java.nio.file.Files;
import java.util.Scanner;
import java.nio.file.FileSystems;
import java.nio.file.Path;

public class Handlers implements HttpHandler {
    public void handle(HttpExchange exchange) throws IOException {
        String fullUrlPath = exchange.getRequestURI().toString();
        String[] maker = fullUrlPath.split("\\?");
        String safeParam = maker.length != 2 ? null : maker[1];
        System.out.println("Obtaining " + maker[0]);
        Response response = getContents(maker[0], safeParam);
        String reply = response.response;
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
                // TODO: Change exec to something not deprecated?
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
                System.out.printf("--- Java Output ---\n%s\n--- Java Error ---\n%s\n", res.response, errorString);
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
            // System.out.println(e.getMessage());
            String errorType = e.getClass().getName();
            System.out.println("Error: " + errorType);
            if(errorType.equals("java.io.FileNotFoundException")) {
                res.code = 403;
                res.response = "Forbidden >:(";
            } else {
                res.code = 500;
                res.response = "Unknown error :0";
            }
            return res;
        }

    }
}
