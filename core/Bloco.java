package core;

public class Bloco {
    public static final int TAMANHO_PADRAO = 4096; // 4KB
    private byte[] dados;

    public Bloco() {
        this.dados = new byte[TAMANHO_PADRAO];
    }

    public Bloco(byte[] dados) {
        this.dados = dados;
    }

    public byte[] getDados() {
        return dados;
    }

    public void setDados(byte[] dados) {
        this.dados = dados;
    }
}