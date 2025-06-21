package filesys;

public class BlocoDeDados {
    private byte[] dados; // Dados do bloco

    // Construtores
    public BlocoDeDados() {
        this.dados = new byte[File.TAMANHO_BYTES_BLOCO]; // Inicializa o bloco com o tamanho especificado
    }

    public BlocoDeDados(byte[] dados) {
        setDados(dados);
    }


    // Dados
    public byte[] getDados() {
        return dados;
    }

    public void setDados(byte[] dados) {
        if (dados.length <= File.TAMANHO_BYTES_BLOCO) {
            this.dados = new byte[dados.length];
            System.arraycopy(dados, 0, this.dados, 0, dados.length); // Copia os novos dados para o bloco
        } else {
            throw new IllegalArgumentException("Dados excedem o tamanho do bloco.");
        }
    }
}
