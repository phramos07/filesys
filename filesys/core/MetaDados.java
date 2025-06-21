package filesys.core;

import java.util.HashMap;

public class MetaDados {
    private String nome;
    private int tamanho;
    private String dono;
    private HashMap<String, String> permissoes;

    public MetaDados(String nome, String dono) {
        this.nome = nome;
        this.dono = dono;
        this.tamanho = 0;
        this.permissoes = new HashMap<>();
        this.permissoes.put(dono, "rwx");
    }

    public String getNome() {
        return nome;
    }

    public int getTamanho() {
        return tamanho;
    }

    public String getDono() {
        return dono;
    }

    public void setTamanho(int tamanho) {
        this.tamanho = tamanho;
    }

    public void setPermissao(String usuario, String perm) {
        permissoes.put(usuario, perm);
    }

    public String getPermissao(String usuario) {
        return permissoes.getOrDefault(usuario, "---");
    }

    public boolean checarPermissao(String usuario, char tipo) {
        return getPermissao(usuario).indexOf(tipo) != -1;
    }

     public void setNome(String nomeDestino) {
        this.nome = nomeDestino;
    }
}
