package filesys;

import java.util.HashMap;
import java.util.Map;

class MetaDados {
    String nome;
    String dono;
    Map<String, String> permissoes; // user → "rwx"

    public MetaDados(String nome, String dono, String permissoesIniciais) {
        this.nome = nome;
        this.dono = dono;
        this.permissoes = new HashMap<>();
        this.permissoes.put(dono, permissoesIniciais);
    }

    public boolean podeEscrever(String usuario) {
        if (!permissoes.containsKey(usuario)){
            return false;
        } 
        return permissoes.get(usuario).contains("w");
    }

    public boolean podeLer(String usuario) {
        if (!permissoes.containsKey(usuario)){
            return false;
        } 
        return permissoes.get(usuario).contains("r");
    }
}
