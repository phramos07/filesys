package filesys;

import java.util.ArrayList;

public class Arquivo {
    private MetaDados metaDados;
    private ArrayList<Bloco> blocos;

    public Arquivo(String nome, String dono) {
        this.metaDados = new MetaDados(nome, dono);
        this.blocos = new ArrayList<>();
    }

    public MetaDados getMetaDados() {
        return metaDados;
    }

    public ArrayList<Bloco> getBlocos() {
        return blocos;
    }

    public void addBloco(Bloco bloco) {
        blocos.add(bloco);
        metaDados.setTamanho(metaDados.getTamanho() + bloco.getDados().length);
    }
}
