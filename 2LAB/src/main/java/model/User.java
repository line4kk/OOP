package model;

public class User {
    private long id;
    private String username;
    private String passwordHash;
    private String role;
    private String factoryType;

    public User(long id, String username, String passwordHash, String role, String factoryType) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.factoryType = factoryType;
    }

    public User(String username, String passwordHash, String role, String factoryType) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.factoryType = factoryType;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getFactoryType() {
        return factoryType;
    }

    public void setFactoryType(String factoryType) {
        this.factoryType = factoryType;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", role='" + role + '\'' +
                ", factoryType='" + factoryType + '\'' +
                '}';
    }
}
