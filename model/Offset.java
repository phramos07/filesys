package model;

public class Offset {
    private int valor;
    private int max;

    public Offset() {
        this.valor = 0;
        this.max = Integer.MAX_VALUE;
    }

    public Offset(int valorInicial) {
        this.valor = valorInicial;
        this.max = Integer.MAX_VALUE;
    }

    public Offset(int valorInicial, int max) {
        this.valor = valorInicial;
        this.max = max;
    }

    public int getValor() {
        return valor;
    }

    public void setValor(int valor) {
        if (valor < 0) {
            throw new IllegalArgumentException("Offset não pode ser negativo.");
        }
        if (valor > max) {
            throw new IllegalArgumentException("Offset excede o valor máximo permitido (" + max + ").");
        }
        this.valor = valor;
    }

    public void incrementar(int delta) {
        setValor(this.valor + delta);
    }

    public void resetar() {
        this.valor = 0;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        if (max < 0) throw new IllegalArgumentException("Max deve ser positivo.");
        this.max = max;
        if (valor > max) valor = max;
    }
}
