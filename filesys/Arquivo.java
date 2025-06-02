package filesys;

public class Arquivo {
    private MetaDados metaDados;
    private Bloco[] arquivo;

    public Arquivo(String nome, String dono, int tamanhoBloco) {
        this.metaDados = new MetaDados(nome, 0, dono);
        this.arquivo = new Bloco[1]; // Inicializa com um bloco
        this.arquivo[0] = new Bloco(tamanhoBloco);
    }

    public MetaDados getMetaDados() {
        return metaDados;
    }

    public void setMetaDados(MetaDados metaDados) {
        this.metaDados = metaDados;
    }

    public Bloco[] getArquivo() {
        return arquivo;
    }

    public void setArquivo(Bloco[] arquivo) {
        this.arquivo = arquivo;
    }

    public void addBloco(Bloco bloco) {
        Bloco[] novoArquivo = new Bloco[this.arquivo.length + 1];
        System.arraycopy(this.arquivo, 0, novoArquivo, 0, this.arquivo.length);
        novoArquivo[this.arquivo.length] = bloco;
        this.arquivo = novoArquivo;
        this.metaDados.setTamanho(this.metaDados.getTamanho() + bloco.getTamanho());
    }

    public void removeBloco(int index) {
        if (index < 0 || index >= this.arquivo.length) {
            throw new IndexOutOfBoundsException("Índice fora dos limites do arquivo");
        }
        Bloco[] novoArquivo = new Bloco[this.arquivo.length - 1];
        System.arraycopy(this.arquivo, 0, novoArquivo, 0, index);
        System.arraycopy(this.arquivo, index + 1, novoArquivo, index, this.arquivo.length - index - 1);
        this.arquivo = novoArquivo;
        this.metaDados.setTamanho(this.metaDados.getTamanho() - this.arquivo[index].getTamanho());
    }

    public void clear() {
        this.arquivo = new Bloco[0];
        this.metaDados.setTamanho(0);
    }
}
