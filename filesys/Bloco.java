package filesys;

public class Bloco {
    private Byte[] dados;

    public Bloco(int tamanho) {
        dados = new Byte[tamanho];
    }

    public Byte[] getDados() {
        return dados;
    }

    public void setDados(Byte[] dados) {
        this.dados = dados;
    }
}
