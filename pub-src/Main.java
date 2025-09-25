import java.util.HashMap;
import java.util.Map;

class Main {
    public static void main(String[] args) {
        Map<String, String> getParams = parse(args, "GET");
        Map<String, String> postParams = parse(args, "POST");
        System.out.print("<h1>GET PARAMS</h1>");
        System.out.print("<p>" + (getParams.isEmpty() ? "none" : getParams) + "</p>");
        System.out.println("Get test: " + getParams.get("test"));
        System.out.print("<a href=/?test=hello>test = hello</a>");
        System.out.print("<h1>POST PARAMS</h1>");
        System.out.print("<p>" + (postParams.isEmpty() ? "none" : postParams) + "</p>");
        System.out.print("<form method=POST>");
        System.out.println("<input type=text name=test placeholder='POST TEST'>");
        System.out.println("<input type=number name=pigeon placeholder='Set pigeon'>");
        System.out.println("<input type=submit>");
        System.out.print("</form>");
    }
    public static Map<String, String> parse(String[] args, String type) {
        type = type.toUpperCase();
        String paramsText = parseString(args, type);
        Map<String, String> map = new HashMap<>();
        if(paramsText.isEmpty())
            return map;
        String[] params = paramsText.split("&");

        for(String param : params) {
            String[] cut = param.split("=");
            if(!cut[0].isEmpty())
                map.put(cut[0], cut.length > 1 ? cut[1] : "");
        }
        return map;
    }
    public static String parseString(String[] args, String type) {
        if(args.length == 0)
            return "";
        for(String arg : args) {
            if(arg.startsWith(type))
                return arg.substring(type.length()+1);
        }
        return "";
    }
}