package filesys;

import java.util.ArrayList;
import java.util.List;

public class Diretorio {

    private MetaDados metaDados;
    // Lista de arquivos neste diretório
    private List<Arquivo> arquivos = new ArrayList<>();
    // Lista de subdiretórios (filhos)
    private List<Diretorio> subDirs = new ArrayList<>();

    public Diretorio(MetaDados metaDados) {
        this.metaDados = metaDados;
    }

    public Diretorio(String nome, String dono) {
        this.metaDados = new MetaDados(nome, dono);
    }

    public MetaDados getMetaDados() {
        return metaDados;
    }

    public void setMetaDados(MetaDados metaDados) {
        this.metaDados = metaDados;
    }

    public List<Arquivo> getArquivos() {
        return arquivos;
    }

    public List<Diretorio> getSubDirs() {
        return subDirs;
    }

    /**
     * Procura um subdiretório de nome exato dentro deste diretório.
     * Retorna o objeto Diretorio se encontrado, ou null caso não exista.
     */
    public Diretorio pegarSubDirPeloNome(String nome) {
        for (Diretorio d : subDirs) {
            if (d.getMetaDados().getNome().equals(nome)) {
                return d;
            }
        }
        return null;
    }

    /**
     * Adiciona um subdiretório filho a este diretório.
     */
    public void adicionarSubDir(Diretorio novo) {
        subDirs.add(novo);
    }

    /**
     * Adiciona um arquivo a este diretório.
     */


    public void adicionarArquivo(Arquivo novo) {
        arquivos.add(novo);
    }

    /**
     * Remove um arquivo deste diretório.
     * Retorna true se o arquivo foi encontrado e removido, ou false se não existia.
     */
    public boolean removerArquivo(Arquivo arquivo) {
        return arquivos.remove(arquivo);
    }
}
