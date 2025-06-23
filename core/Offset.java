package core;

public class Offset {
    private int posicao;
    private int limite;

    public Offset() {
        this.posicao = 0;
        this.limite = Integer.MAX_VALUE;
    }

    public Offset(int inicial) {
        this.posicao = inicial;
        this.limite = Integer.MAX_VALUE;
    }

    public Offset(int inicial, int limite) {
        this.posicao = inicial;
        this.limite = limite;
    }

    public int getPosicao() {
        return posicao;
    }

    public void setPosicao(int posicao) {
        if (posicao < 0) {
            throw new IllegalArgumentException("Offset não pode ser negativo.");
        }
        if (posicao > limite) {
            throw new IllegalArgumentException("Offset excede o limite permitido (" + limite + ").");
        }
        this.posicao = posicao;
    }

    public void avancar(int quantidade) {
        setPosicao(this.posicao + quantidade);
    }

    public void zerar() {
        this.posicao = 0;
    }

    public int getLimite() {
        return limite;
    }

    public void setLimite(int limite) {
        if (limite < 0) throw new IllegalArgumentException("Limite deve ser positivo.");
        this.limite = limite;
        if (posicao > limite) posicao = limite;
    }
}