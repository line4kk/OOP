package model.enums;

public enum FactoryType {
    LINKED_LIST("linked_list"),
    ARRAY("array");

    private final String value;

    FactoryType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static boolean isValidType(String s) {
        for (FactoryType type : values()) {
            if (type.value.equals(s)) return true;
        }
        return false;
    }
}

