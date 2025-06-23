package core;

import java.util.HashMap;
import java.util.Map;

public class Diretorio extends ElementoFS {
    private Diretorio pai;
    private final Map<String, ElementoFS> conteudo;
    private final Map<String, String> permissoesUsuario;

    public Diretorio(String nome, String permissoes, String dono) {
        super(nome, permissoes, dono);
        if (permissoes == null || permissoes.length() != 3)
            throw new IllegalArgumentException("Permissões devem ter 3 caracteres (rwx)");
        this.conteudo = new HashMap<>();
        this.permissoesUsuario = new HashMap<>();
    }

    public void definirPermissao(String usuario, String permissoes) {
        if (permissoes == null || permissoes.length() != 3)
            throw new IllegalArgumentException("Permissões devem ter 3 caracteres (rwx)");
        permissoesUsuario.put(usuario, permissoes);
    }

    public boolean possuiPermissao(String usuario, char tipo) {
        if ("root".equals(usuario)) return true;
        if (usuario.equals(donoDiretorio)) return permissoesPadrao.indexOf(tipo) >= 0;
        String perm = permissoesUsuario.get(usuario);
        if (perm != null && perm.indexOf(tipo) >= 0) return true;
        if (pai != null) return pai.possuiPermissao(usuario, tipo);
        return false;
    }

    public String permissoesDoUsuario(String usuario) {
        if ("root".equals(usuario)) return "rwx";
        if (usuario.equals(donoDiretorio)) return permissoesPadrao;
        return permissoesUsuario.getOrDefault(usuario, "---");
    }

    public void inserirElemento(ElementoFS elemento) {//adiciona FILHO
        if (conteudo.containsKey(elemento.getNomeDiretorio()))
            throw new IllegalArgumentException("Elemento já existe: " + elemento.getNomeDiretorio());
        if (elemento instanceof Diretorio) {
            ((Diretorio) elemento).setPai(this);
        }
        conteudo.put(elemento.getNomeDiretorio(), elemento);
    }

    public void excluirElemento(String nome) {//exclui FILHO
        conteudo.remove(nome);
    }

    public Map<String, ElementoFS> getConteudo() {
        return conteudo;
    }

    public Diretorio getPai() {
        return pai;
    }

    public void setPai(Diretorio pai) {
        this.pai = pai;
    }

    @Override
    public boolean isArquivo() {
        return false;
    }

    @Override
    public String toString() {
        return "[DIR] " + nomeDiretorio + " | Dono: " + donoDiretorio + " | Perm: " + permissoesPadrao;
    }
}