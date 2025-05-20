package model;

import java.util.HashMap;
import java.util.Map;

public class Diretorio extends ElementoFS {
    private Diretorio diretorioPai;
    private Map<String, ElementoFS> filhos;
    private Map<String, String> acessosPorUsuario; // permissões específicas

    public Diretorio(String nome, String permissoes, String dono) {
        super(nome, permissoes, dono);
        if (permissoes == null || permissoes.length() != 3) {
            throw new IllegalArgumentException("Permissões devem conter 3 caracteres (rwx)");
        }
        this.filhos = new HashMap<>();
        this.acessosPorUsuario = new HashMap<>();
    }

    public void setPermissaoUsuario(String nomeUsuario, String permissoes) {
        if (permissoes == null || permissoes.length() != 3) {
            throw new IllegalArgumentException("Permissões devem conter 3 caracteres (rwx)");
        }
        acessosPorUsuario.put(nomeUsuario, permissoes);
    }

    public boolean temPermissao(String usuario, char tipoPermissao) {
        if ("root".equals(usuario)) return true;
        if (usuario.equals(donoDiretorio)) return permissoesPadrao.indexOf(tipoPermissao) >= 0;
        String permissoesUsuario = acessosPorUsuario.get(usuario);
        if (permissoesUsuario != null && permissoesUsuario.indexOf(tipoPermissao) >= 0) return true;
        if (diretorioPai != null) return diretorioPai.temPermissao(usuario, tipoPermissao);
        return false;
    }

    public String obterPermissoesDoUsuario(String usuario) {
        if ("root".equals(usuario)) return "rwx";
        if (usuario.equals(donoDiretorio)) return permissoesPadrao;
        return acessosPorUsuario.getOrDefault(usuario, "---");
    }

    public void adicionarFilho(ElementoFS filho) {
        if (filhos.containsKey(filho.getNomeDiretorio())) {
            throw new IllegalArgumentException("Já existe um filho com este nome: " + filho.getNomeDiretorio());
        }
        if (filho instanceof Diretorio) {
            ((Diretorio) filho).setDiretorioPai(this);
        }
        filhos.put(filho.getNomeDiretorio(), filho);
    }

    public void removerFilho(String nomeFilho) {
        filhos.remove(nomeFilho);
    }

    public Map<String, ElementoFS> getFilhos() {
        return filhos;
    }

    public Diretorio getDiretorioPai() {
        return diretorioPai;
    }

    public void setDiretorioPai(Diretorio diretorioPai) {
        this.diretorioPai = diretorioPai;
    }

    @Override
    public boolean isArquivo() {
        return false;
    }

    @Override
    public String toString() {
        return "Dir: " + nomeDiretorio + " | Owner: " + donoDiretorio + " | Perms: " + permissoesPadrao;
    }
}