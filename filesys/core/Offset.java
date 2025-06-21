package filesys.core;

public class Offset {
    private int value;

    public Offset(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public void add(int delta) {
        value += delta;
    }
}