package filesys;

import java.util.ArrayList;
import java.util.List;

public class Diretorio {
    private MetaDados metaDados;
    private List<Arquivo> arquivos;
    private List<Diretorio> subdiretorios;

    public Diretorio(String nome, String dono) {
        this.metaDados = new MetaDados(nome, dono);
        this.arquivos = new ArrayList<>();
        this.subdiretorios = new ArrayList<>();
    }

    public MetaDados getMetaDados() {
        return metaDados;
    }

    public List<Arquivo> getArquivos() {
        return arquivos;
    }

    public List<Diretorio> getSubdiretorios() {
        return subdiretorios;
    }

    public void adicionarArquivo(Arquivo a) {
        arquivos.add(a);
    }

    public void adicionarSubdiretorio(Diretorio d) {
        subdiretorios.add(d);
    }

    public Diretorio buscarSubdiretorio(String nome) {
        return subdiretorios.stream()
            .filter(d -> d.getMetaDados().getNome().equals(nome))
            .findFirst().orElse(null);
    }

    public Arquivo buscarArquivo(String nome) {
        return arquivos.stream()
            .filter(a -> a.getMetaDados().getNome().equals(nome))
            .findFirst().orElse(null);
    }
}
