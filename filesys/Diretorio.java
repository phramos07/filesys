package filesys;

import java.util.HashMap;
import java.util.Map;

public class Diretorio {
    protected String nome;
    protected String permissoes;
    protected String dono;
    protected Map<String, Diretorio> filhos;

    public Diretorio(String nome, String permissoes, String dono) {
        this.nome = nome;
        this.permissoes = permissoes;
        this.dono = dono;
        this.filhos = new HashMap<>();
    }

    public String getNome() {
        return nome;
    }

    public String getPermissoes() {
        return permissoes;
    }

    public void setPermissoes(String permissoes) {
        this.permissoes = permissoes;
    }

    public String getDono() {
        return dono;
    }

    public void setDono(String dono) {
        this.dono = dono;
    }

    public Map<String, Diretorio> getFilhos() {
        return filhos;
    }

    public void adicionarFilho(Diretorio filho) {
        filhos.put(filho.getNome(), filho);
    }

    public void removerFilho(String nome) {
        filhos.remove(nome);
    }

    public boolean isArquivo() {
        return false; // Por padrão, um `Diretorio` não é um arquivo
    }
}