package model;

import java.util.HashMap;
import java.util.Map;

public abstract class ElementoFS {
    protected String nome;
    protected String caminho;
    protected String dono;
    protected Map<String, String> permissoes; // Ex: usuario → "rw"

    public ElementoFS(String nome, String caminho, String dono) {
        this.nome = nome;
        this.caminho = caminho;
        this.dono = dono;
        this.permissoes = new HashMap<>();
        this.permissoes.put(dono, "rw"); // Dono começa com permissão total
    }

    public String getNome() {
        return nome;
    }

    public String getCaminho() {
        return caminho;
    }

    public String getDono() {
        return dono;
    }

    public void setPermissao(String usuario, String permissao) {
        permissoes.put(usuario, permissao);
    }

    public String getPermissao(String usuario) {
        return permissoes.getOrDefault(usuario, "");
    }

    public boolean temPermissaoLeitura(String usuario) {
        return getPermissao(usuario).contains("r") || usuario.equals("root");
    }

    public boolean temPermissaoEscrita(String usuario) {
        return getPermissao(usuario).contains("w") || usuario.equals("root");
    }

    public abstract boolean ehDiretorio(); // Pra saber se é pasta ou não
}
