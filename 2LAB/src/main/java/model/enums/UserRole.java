package model.enums;

public enum UserRole {
    USER("user"),
    ADMIN("admin");

    private final String value;

    UserRole(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static boolean isValidRole(String s) {
        for (UserRole role : values()) {
            if (role.value.equals(s)) return true;
        }
        return false;
    }
}
