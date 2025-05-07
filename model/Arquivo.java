package model;

public class Arquivo {
    //representa um arquivo com conteúdo
    private String nome;
    private String caminhoCompleto;
    private String dono;
    private String conteudo;
    private Permissao permissoes;

    public Arquivo(String nome, String caminhoCompleto, String dono) {
        this.nome = nome;
        this.caminhoCompleto = caminhoCompleto;
        this.dono = dono;
        this.conteudo = "";
        this.permissoes = new Permissao();
        this.permissoes.definirPermissao(dono, "rw");
    }

    public String getNome() { return nome; }
    public String getCaminhoCompleto() { return caminhoCompleto; }
    public String getDono() { return dono; }
    public String getConteudo() { return conteudo; }
    public Permissao getPermissoes() { return permissoes; }

    public void escreverConteudo(String novoConteudo) {
        this.conteudo = novoConteudo;
    }
}
