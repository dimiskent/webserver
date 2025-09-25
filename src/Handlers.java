import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Scanner;
import java.nio.file.FileSystems;
import java.nio.file.Path;

public class Handlers implements HttpHandler {
    public Path path = FileSystems.getDefault().getPath(System.getProperty("user.dir"), "pub");
    public void handle(HttpExchange exchange) throws IOException {
        String fullUrlPath = exchange.getRequestURI().toString();
        String[] maker = fullUrlPath.split("\\?");

        if(maker[0].length() == 1) {
            // index.html by default
            Path path = FileSystems.getDefault().getPath(this.path.toString(), "Main.class");
            File indexClass = new File(path.toUri());
            maker[0] = indexClass.exists() ? "/Main.class" : "/index.html";
        }
        String safeParam = maker.length != 2 ? null : maker[1];
        System.out.println("Obtaining " + maker[0]);
        Response response;
        Headers responseHeaders = exchange.getResponseHeaders();

        Scanner post = new Scanner(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8));
        StringBuilder postVars = new StringBuilder();
        while (post.hasNextLine()) {
            postVars.append(post.nextLine()).append("&");
        }
        if(!postVars.isEmpty()) {
            postVars = new StringBuilder(postVars.substring(0, postVars.length() - 1));
        }

        String redirectTest = tryRedirect(maker[0]);
        if(redirectTest.isEmpty()) {
            response = getContents(maker[0], safeParam, postVars.toString());
        } else {
            System.out.printf("Redirecting user from %s towards %s\n", maker[0], redirectTest);
            response = new Response(301, "Redirecting to <a href='" + redirectTest  + "'>", "text/html");
            responseHeaders.set("Location", redirectTest);
        }
        responseHeaders.set("Content-Type", response.contentType);
        String reply = response.response;
        exchange.sendResponseHeaders(response.code, reply.length());
        OutputStream stream = exchange.getResponseBody();
        stream.write(reply.getBytes());
        stream.close();
    }
    private String tryRedirect(String path) throws FileNotFoundException {
        File redirectsFile = new File("cnf/redirects.txt");
        if(!redirectsFile.exists()) {
            return "";
        }
        StringBuilder redirectsText = new StringBuilder();
        Scanner fileScanner = new Scanner(redirectsFile);
        while (fileScanner.hasNextLine()) {
            redirectsText.append(fileScanner.nextLine()).append("\n");
        }
        String[] redirects = redirectsText.toString().split("\n");
        String[][] paths = new String[redirects.length][2];
        for(int i = 0; i < redirects.length; i++) {
            paths[i] = redirects[i].split(" ");
            if(path.toLowerCase().startsWith(paths[i][0].toLowerCase())) {
                return paths[i][1];
            }
        }
        return "";
    }
    private Response getContents(String filePath, String getParams, String postParams) {
        Response res = new Response(200, "", "text/plain");
        Path path = FileSystems.getDefault().getPath(this.path.toString(), filePath);
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
                res = evalClass(path, getParams, postParams);
            }
            return res;
        } catch (Exception e) {
            // System.out.println(e.getMessage());
            String errorType = e.getClass().getName();
            System.out.println("Error: " + errorType);
            res.contentType = "text/plain";
            switch (errorType) {
                case "java.io.FileNotFoundException":
                    res.code = 403;
                    res.response = "Forbidden >:(";
                    break;
                default:
                    res.code = 500;
                    res.response = "Unknown error :0";
            }
            return res;
        }

    }
    private Response evalClass(Path path, String getParams, String postParams) throws InterruptedException, IOException {
        String fileName = path.getFileName().toString();
        fileName = fileName.substring(0, fileName.length() - 6); // better implementation
        String command = "java -cp \"" + path.getParent().toString() + "\" " + fileName;
        if(getParams != null) {
            command += " GET:" + getParams;
        }
        if(!postParams.isEmpty()) {
            command += " POST:" + postParams;
        }
        Process p2 = Runtime.getRuntime().exec(command.split(" "));
        BufferedReader br=new BufferedReader(new InputStreamReader(p2.getInputStream()));
        BufferedReader errorbr=new BufferedReader(new InputStreamReader(p2.getErrorStream()));
        Response res = new Response(200, "", "text/html");

        String out, err, errorString = null;
        while( (out=br.readLine())!=null)
        {
            if(out.startsWith("${JAVA_CONTENT_TYPE}"))
                res.contentType = out.substring(21);
            else if(out.startsWith("${JAVA_HTTP_RESPONSE_CODE}"))
                res.code = Integer.parseInt(out.substring(out.length() - 3));
            else
                res.response += out + (res.contentType.equals("text/html") ? "<br>" : "\n");
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
        return res;
    }
}
