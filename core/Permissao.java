package core;

import java.util.HashMap;
import java.util.Map;

public class Permissao {
    // Mapeia usuários para suas permissões
    private Map<String, String> mapaPermissoes;

    public Permissao() {
        this.mapaPermissoes = new HashMap<>();
    }

    public void atribuirPermissao(String usuario, String permissao) {
        mapaPermissoes.put(usuario, permissao); // exemplos: "rw", "r", "-"
    }

    public boolean leituraPermitida(String usuario) {
        String p = mapaPermissoes.getOrDefault(usuario, "");
        return p.contains("r");
    }

    public boolean escritaPermitida(String usuario) {
        String p = mapaPermissoes.getOrDefault(usuario, "");
        return p.contains("w");
    }

    public String consultarPermissao(String usuario) {
        return mapaPermissoes.getOrDefault(usuario, "-");
    }
}