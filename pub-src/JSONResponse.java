class JSONResponse {
	public static void main(String[] args) {
        System.out.println("${JAVA_CONTENT_TYPE} application/json");
        System.out.println("${JAVA_HTTP_RESPONSE_CODE} 418");
        System.out.println("{");
        System.out.println("\"x\": 1,");
        System.out.println("\"y\": 2,");
        System.out.println("}");
    }
}