package filesys;

import java.util.HashMap;
import java.util.Map;

public class Diretorio {
    MetaDados meta;
    Map<String, Diretorio> subdirs = new HashMap<>();
    Map<String, Arquivo> arquivos = new HashMap<>();

    public Diretorio(String nome, String dono) {
        this.meta = new MetaDados(nome, dono, "rwx");
    }
}
