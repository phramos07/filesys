package filesys;

import java.util.ArrayList;
import java.util.List;

class Arquivo {
    MetaDados meta;
    List<Byte> conteudo = new ArrayList<>();

    public Arquivo(String nome, String dono) {
        this.meta = new MetaDados(nome, dono, "rwx");
    }
}
