package filesys;

import java.util.HashMap;

public class MetaDados {
    private String nome;
    private int tamanho;
    private String dono;
    private HashMap<String, String> permissoes; // Ex: <"joao", "rwx">

    public MetaDados(String nome, String dono) {
        this.nome = nome;
        this.dono = dono;
        this.tamanho = 0;
        this.permissoes = new HashMap<>();
        permissoes.put(dono, "rwx");
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public int getTamanho() {
        return tamanho;
    }

    public void setTamanho(int tamanho) {
        this.tamanho = tamanho;
    }

    public String getDono() {
        return dono;
    }

    public void setDono(String dono) {
        this.dono = dono;
    }

    public HashMap<String, String> getPermissoes() {
        return permissoes;
    }

    public void setPermissao(String usuario, String permissao) {
        permissoes.put(usuario, permissao);
    }

    public String getPermissao(String usuario) {
        return permissoes.getOrDefault(usuario, "---");
    }
}