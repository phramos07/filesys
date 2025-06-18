package filesys.core;

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

    public void adicionarArquivo(Arquivo arquivo) {
        if (arquivo != null) {
            this.arquivos.add(arquivo);
        }
    }

    public void adicionarSubdiretorio(Diretorio diretorio) {
        if (diretorio != null) {
            this.subdiretorios.add(diretorio);
        }
    }

    public Diretorio buscarSubdiretorio(String nome) {
        return this.subdiretorios.stream()
                .filter(d -> nome.equals(d.getMetaDados().getNome()))
                .findFirst()
                .orElse(null);
    }

    public Arquivo buscarArquivo(String nome) {
        return this.arquivos.stream()
                .filter(a -> nome.equals(a.getMetaDados().getNome()))
                .findFirst()
                .orElse(null);
    }

    public void removerArquivo(String nome) {
        this.arquivos.removeIf(a -> nome.equals(a.getMetaDados().getNome()));
    }

    public void removerSubdiretorio(String nome) {
        this.subdiretorios.removeIf(d -> nome.equals(d.getMetaDados().getNome()));
    }

}
