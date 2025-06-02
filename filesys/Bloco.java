package filesys;

public class Bloco {
    private byte[] dados;
    private int tamanho;

    public Bloco(int tamanho) {
        if (tamanho <= 0) {
            throw new IllegalArgumentException("Tamanho do bloco deve ser maior que zero");
        }
        this.tamanho = tamanho;
        this.dados = new byte[tamanho];
    }

    public byte[] getDados() {
        return dados;
    }

    public void setDados(byte[] dados) {
        if (dados.length <= tamanho) {
            this.dados = dados;
        } else {
            throw new IllegalArgumentException("Dados excedem o tamanho do bloco");
        }
    }

    public int getTamanho() {
        return tamanho;
    }
}
