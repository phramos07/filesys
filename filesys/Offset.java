package filesys;

public class Offset {
  private int value;
  private int max;

  public Offset() {
    this.setValue(0);
    this.setMax(Integer.MAX_VALUE);
  }

  public Offset(int initialValue) {
    this.setValue(initialValue);
    this.setMax(Integer.MAX_VALUE);
  }

  public Offset(int initialValue, int max) {
    this.setValue(initialValue);
    this.setMax(max);
  }

  public int getValue() {
    return value;
  }

  public void setValue(int value) {
    if (value < 0) {
      throw new IllegalArgumentException("Offset não pode ser negativo.");
    }
    if (value > max) {
      throw new IllegalArgumentException("Offset excede o valor máximo permitido (" + max + ").");
    }
    this.value = value;
  }

  public void add(int delta) {
    setValue(this.value + delta);
  }

  public void reset() {
    this.value = 0;
  }

  public void setMax(int max) {
    if (max < 0)
      throw new IllegalArgumentException("Max deve ser positivo.");
    this.max = max;
    if (value > max)
      value = max;
  }

  public int getMax() {
    return max;
  }
}
