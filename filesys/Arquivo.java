package filesys;

import java.util.ArrayList;
import java.util.List;

public class Arquivo {
    public MetaDados metaDados;
    private Bloco[] bloco;

class Arquivo {
    private MetaDados metaDados;
    private Bloco[] bloco; // pode ser usado depois em write()

    private List<Byte> conteudo = new ArrayList<>();

    public Arquivo(String nome, String dono) {
        this.metaDados = new MetaDados(nome, dono, "rwx");
    }


    public List<Byte> getConteudo() {
        return conteudo;
    }

    public void limparConteudo() {
        conteudo.clear();
    }

    public void adicionarByte(byte b) {
        conteudo.add(b);
    }

}

    public MetaDados getMetaDados() {
        return metaDados;
    }
}

