package filesys;

import java.util.HashMap;
import java.util.Map;

public class Diretorio {
    MetaDados metaDados;
    Map<String, Diretorio> subdirs = new HashMap<>();
    Map<String, Arquivo> arquivos = new HashMap<>();

    public Diretorio(String nome, String dono) {
        this.metaDados = new MetaDados(nome, dono, "rwx");
    }
}
