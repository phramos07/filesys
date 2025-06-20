package filesys;

public class Bloco {
    private byte[] dados;

    public Bloco(byte[] dados) {
        this.dados = dados;
    }

    public Bloco(int size) {
        this.dados = new byte[size];
    }
    
    public Bloco() {
        this.dados = new byte[0];
    }

    public byte[] getDados() {
        return dados;
    }

    public void setDados(byte[] dados) {
        this.dados = dados;
    }

    public int getSize() {
        return dados.length;
    }

}
