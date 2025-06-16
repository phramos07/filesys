package filesys;

import java.util.ArrayList;
import java.util.List;

class Arquivo {
    private MetaDados metaDados;
    private Bloco[] bloco;
    public List<Byte> conteudo;

    public Arquivo(String nome, String dono) {
        this.metaDados = new MetaDados(nome, dono, "rwx");
        this.conteudo = new ArrayList<>();
    }
}
