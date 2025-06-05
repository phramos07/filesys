package filesys;

import java.util.ArrayList;
import java.util.List;

public class Arquivo {
    private MetaDados metaDados;
    private List<Bloco> blocos;

    public Arquivo(String nome, String dono) {
        this.metaDados = new MetaDados(nome, dono);
        this.blocos = new ArrayList<>();
    }

    public MetaDados getMetaDados() {
        return metaDados;
    }

    public List<Bloco> getBlocos() {
        return blocos;
    }

    public void adicionarBloco(Bloco bloco) {
        blocos.add(bloco);
        metaDados.setTamanho(metaDados.getTamanho() + bloco.getDados().length);
    }
}
