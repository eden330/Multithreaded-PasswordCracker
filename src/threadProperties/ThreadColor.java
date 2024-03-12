package threadProperties;

public enum ThreadColor {

    ANSI_RESET("\u001B[0m"),
    CONSUMER("\u001B[34m"), // blue
    FIRST_PRODUCER("\u001B[32m"), // green
    SECOND_PRODUCER("\u001B[35m"), // purple
    THIRD_PRODUCER("\u001B[31m"); // red

    private final String color;

    ThreadColor(String color) {
        this.color = color;
    }

    public String color() {
        return color;
    }
}
