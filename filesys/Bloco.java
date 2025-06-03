package filesys;

public class Bloco {
    private static final int TAMANHO_BLOCO = 4096; // 4KB por bloco
    private byte[] dados;


    public Bloco(int tamanho) {
        this.dados = new byte[TAMANHO_BLOCO];
    }

    public byte[] getDados() {
        return dados;
    }

    public void setDados(byte[] dados) {
        this.dados = dados;
    }
}