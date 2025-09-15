package tech.chhsich.backend.pojo;

public class ResponseMessage {
    private int code;
    private String message;
    private Object data;

    public ResponseMessage() {}
    public ResponseMessage(int code, String message, Object data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static ResponseMessage success(Object data) {
        return new ResponseMessage(0, "success", data);
    }

    public static ResponseMessage success() {
        return new ResponseMessage(0, "success", null);
    }

    public static ResponseMessage error(String message) {
        return new ResponseMessage(1, message, null);
    }

    // getter/setter 省略（或用 Lombok）
    public int getCode() { return code; }
    public void setCode(int code) { this.code = code; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public Object getData() { return data; }
    public void setData(Object data) { this.data = data; }
}