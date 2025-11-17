package model;

public class Function {
    private long id;
    private long userId;
    private String name;
    private String type;
    private String source;

    public Function(long userId, String name, String type, String source) {
        this.userId = userId;
        this.name = name;
        this.type = type;
        this.source = source;
    }

    public Function(long id, long userId, String name, String type, String source) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.type = type;
        this.source = source;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
