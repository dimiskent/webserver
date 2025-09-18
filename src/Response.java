public class Response {
    public int code;
    public String response;
    public String contentType;
    public Response(int code, String response, String contentType) {
        this.code = code;
        this.response = response;
        this.contentType = contentType;
    }
}
