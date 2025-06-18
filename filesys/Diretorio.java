package filesys;

import java.util.ArrayList;

public class Diretorio {
    private MetaDados metaDados;
    private ArrayList<Arquivo> arquivos;
    private ArrayList<Diretorio> subDiretorios;

    public Diretorio(String nome, String dono) {
        this.metaDados = new MetaDados(nome, dono);
        this.arquivos = new ArrayList<>();
        this.subDiretorios = new ArrayList<>();
    }

    public MetaDados getMetaDados() {
        return metaDados;
    }

    public ArrayList<Arquivo> getArquivos() {
        return arquivos;
    }

    public ArrayList<Diretorio> getSubDiretorios() {
        return subDiretorios;
    }

    public void adicionarArquivo(Arquivo arquivo) {
        arquivos.add(arquivo);
    }

    public void adicionarSubDiretorio(Diretorio dir) {
        subDiretorios.add(dir);
    }
}
