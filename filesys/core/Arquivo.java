package filesys.core;

import java.util.ArrayList;
import java.util.List;

public class Arquivo {
    private MetaDados metaDados;
    private List<Bloco> blocos;
    public static int TAMANHO_BLOCO = 64;

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
        if (bloco != null && bloco.getDados() != null) {
            this.blocos.add(bloco);
            int tamanhoBloco = bloco.getDados().length;
            this.metaDados.setTamanho(this.metaDados.getTamanho() + tamanhoBloco);
        }
    }
}
