package model.enums;

public enum FunctionType {
    LINKED_LIST_TABULATED("linked_list_tabulated"),
    ARRAY_TABULATED("array_tabulated"),
    ANALYTICAL("analytical");

    private final String value;

    FunctionType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static boolean isValidType(String s) {
        for (FunctionType type : values()) {
            if (type.value.equals(s)) return true;
        }
        return false;
    }
}
