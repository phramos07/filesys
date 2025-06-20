package filesys;

import java.util.HashMap;
import java.util.Map;

public class Permissao {
    //quem pode fazer o quê é definido aqui
    private Map<String, String> permissoesPorUsuario;

    public Permissao() {
        this.permissoesPorUsuario = new HashMap<>();
    }

    public void definirPermissao(String usuario, String permissao) {
        permissoesPorUsuario.put(usuario, permissao); // ex: "rw", "r", "-"
    }

    public boolean podeLer(String usuario) {
        String p = permissoesPorUsuario.getOrDefault(usuario, "");
        return p.contains("r");
    }

    public boolean podeEscrever(String usuario) {
        String p = permissoesPorUsuario.getOrDefault(usuario, "");
        return p.contains("w");
    }

    public String getPermissao(String usuario) {
        return permissoesPorUsuario.getOrDefault(usuario, "-");
    }
}
