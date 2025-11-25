package model.enums;

public enum SourceType {
    BASE("base"),
    OPERATION("operation"),
    COMPOSITE("composite");

    private final String value;

    SourceType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static boolean isValidSourceType(String s) {
        for (SourceType sourceType : values()) {
            if (sourceType.value.equals(s)) return true;
        }
        return false;
    }
}
