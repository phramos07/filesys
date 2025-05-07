package model;

import java.util.*;

public class Diretorio {
    //representa uma pasta
    private String nome;
    private String caminhoCompleto;
    private String dono;
    private Map<String, Diretorio> subdiretorios;
    private Map<String, Arquivo> arquivos;
    private Permissao permissoes;

    public Diretorio(String nome, String caminhoCompleto, String dono) {
        this.nome = nome;
        this.caminhoCompleto = caminhoCompleto;
        this.dono = dono;
        this.subdiretorios = new HashMap<>();
        this.arquivos = new HashMap<>();
        this.permissoes = new Permissao();
        this.permissoes.definirPermissao(dono, "rw"); // dono pode tudo
    }

    public String getNome() { return nome; }
    public String getCaminhoCompleto() { return caminhoCompleto; }
    public String getDono() { return dono; }
    public Map<String, Diretorio> getSubdiretorios() { return subdiretorios; }
    public Map<String, Arquivo> getArquivos() { return arquivos; }
    public Permissao getPermissoes() { return permissoes; }

    public void adicionarDiretorio(Diretorio dir) {
        subdiretorios.put(dir.getNome(), dir);
    }

    public void adicionarArquivo(Arquivo arq) {
        arquivos.put(arq.getNome(), arq);
    }

    public Diretorio getSubdiretorio(String nome) {
        return subdiretorios.get(nome);
    }

    public Arquivo getArquivo(String nome) {
        return arquivos.get(nome);
    }

    public boolean contemDiretorio(String nome) {
        return subdiretorios.containsKey(nome);
    }

    public boolean contemArquivo(String nome) {
        return arquivos.containsKey(nome);
    }
}
