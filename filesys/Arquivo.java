package filesys;

import java.util.ArrayList;
import java.util.List;

class Arquivo {
    private MetaDados metaDados;
    private Bloco[] bloco; // pode ser usado depois em write()
    private List<Byte> conteudo = new ArrayList<>();

    public Arquivo(String nome, String dono) {
        this.metaDados = new MetaDados(nome, dono, "rwx");
    }

    public MetaDados getMetaDados() {
        return metaDados;
    }

    public List<Byte> getConteudo() {
        return conteudo;
    }
}
