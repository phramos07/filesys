package filesys;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class File extends Dir {
    public static final int TAMANHO_BYTES_BLOCO = 1024; // Tamanho do arquivo em bytes

    // Atributos específicos de um arquivo
    private List<BlocoDeDados> blocos; // Lista de blocos de dados que compõem o arquivo
    private long tamanho; // Tamanho atual do arquivo em bytes

    // Construtor
    public File(String nome, String dono, String permissoes) {
        super(nome, dono, permissoes);

        this.blocos = new ArrayList<>();
        this.tamanho = 0;

        this.pai = null;
        this.filhos = null; // Um arquivo não pode ter filhos, então setamos como null
    }

    // Blocos
    public List<BlocoDeDados> getBlocos() {
        return blocos;
    }

    public void setBlocos(List<BlocoDeDados> blocos) {
        this.blocos = blocos;
        this.tamanho = blocos.stream().mapToLong(bloco -> bloco.getDados().length).sum(); // Atualiza o tamanho do arquivo
    }

    public void addBloco(BlocoDeDados bloco) {
        this.blocos.add(bloco);
        this.tamanho += bloco.getDados().length; // Atualiza o tamanho do arquivo
    }

    public void removeBloco(BlocoDeDados bloco) {
        if (this.blocos.remove(bloco)) {
            this.tamanho -= bloco.getDados().length; // Atualiza o tamanho do arquivo
        }
    }

    public void clearBlocos() {
        this.blocos.clear();
        this.tamanho = 0; // Reseta o tamanho do arquivo
    }


    // Tamanho
    public long getTamanho() {
        return tamanho;
    }

    public void setTamanho(long tamanho) {
        this.tamanho = tamanho;
    }

    public void addTamanho(long tamanho) {
        this.tamanho += tamanho;
    }

    public void removeTamanho(long tamanho) {
        this.tamanho -= tamanho;
        if (this.tamanho < 0) {
            this.tamanho = 0; // Garante que o tamanho não fique negativo
        }
    }


    // É um arquivo?
    @Override
    public boolean isArquivo() {
        return true; // Essa classe representa um diretório do tipo arquivo
    }


    // Filhos
    @Override
    public Dir getFilho(String nome) {
        throw new UnsupportedOperationException("Um arquivo não tem filhos.");
    }

    @Override
    public Map<String, Dir> getFilhos() {
        throw new UnsupportedOperationException("Um arquivo não tem filhos.");
    }

    @Override
    public void setFilhos(Map<String, Dir> filhos) {
        throw new UnsupportedOperationException("Um arquivo não pode ter filhos.");
    }

    @Override
    public void addFilho(Dir filho) {
        throw new UnsupportedOperationException("Um arquivo não pode ter filhos.");
    }

    @Override
    public void removeFilho(String nome) {
        throw new UnsupportedOperationException("Um arquivo não tem filhos.");
    }


    // Transformando em string
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("Arquivo: ").append(getNome()).append("\n")
        .append("  - Dono: ").append(getDono()).append("\n")
        .append("  - Permissões: ").append(getPermissoes()).append("\n")
        .append("  - Tamanho: ").append(tamanho).append(" bytes\n")
        .append("  - Blocos: ").append(blocos.size()).append("\n");
        return sb.toString();
    }
}
