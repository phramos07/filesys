package filesys;

import java.util.HashMap;
import java.util.Map;

class MetaDados {
    private String nome;
    private int tamanho;
    private String dono;
    private Map<String, String> permissoes; // user → "rwx"

    public MetaDados(String nome, String dono, String permissoesIniciais) {
        this.nome = nome;
        this.dono = dono;
        this.permissoes = new HashMap<>();
        this.permissoes.put(dono, permissaoPadrao(permissoesIniciais));
        this.tamanho = 0;
    }

    private String permissaoPadrao(String p) {
        return (p == null || p.isEmpty()) ? "---" : p;
    }

    public boolean podeEscrever(String usuario) {
        if (!permissoes.containsKey(usuario)) {
            return false;
        }
        return permissoes.get(usuario).contains("w");
    }

        return permissoes.containsKey(usuario) && permissoes.get(usuario).contains("w");
    }

    public boolean podeLer(String usuario) {
        return permissoes.containsKey(usuario) && permissoes.get(usuario).contains("r");
    }

    public boolean podeExecutar(String usuario) {
        return permissoes.containsKey(usuario) && permissoes.get(usuario).contains("x");
    }

    public void setPermissao(String usuario, String permissao) {
        permissoes.put(usuario, permissao);
    }

    public String getPermissao(String usuario) {
        return permissoes.get(usuario);
    }

    public String getDono() {
        return dono;
    }

    public int getTamanho() {
        return tamanho;
    }

    public void setTamanho(int tamanho) {
        this.tamanho = tamanho;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

}
