package filesys.core;

public class Bloco {
    private byte[] dados;

    public Bloco(int tamanho) {
        this.dados = new byte[tamanho];
    }

    public byte[] getDados() {
        return dados;
    }

    public void setDados(byte[] dados) {
        this.dados = dados;
    }
}
