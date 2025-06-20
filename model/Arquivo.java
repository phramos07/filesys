package model;

import java.util.ArrayList;
import java.util.List;

public class Arquivo extends ElementoFS {
    private List<byte[]> blocos; // conteúdo do arquivo
    private long tamanho;
    

    public Arquivo(String nome, String permissoes, String dono) {
        super(nome, permissoes, dono);
        this.blocos = new ArrayList<>();
        this.tamanho = 0;
    }

    public void adicionarBloco(byte[] dados) {
        blocos.add(dados);
        tamanho += dados.length;
    }

    public List<byte[]> getBlocos() {
        return blocos;
    }

    public long getTamanho() {
        return tamanho;
    }

    public void removerBloco(int index) {
        if (index < 0 || index >= blocos.size()) {
            throw new IndexOutOfBoundsException("Índice fora dos limites da lista.");
        }
        byte[] removed = blocos.remove(index);
        tamanho -= removed.length;
    }

    public void limparBlocos() {
        blocos.clear();
        tamanho = 0;
    }

    @Override
    public boolean isArquivo() {
        return true;
    }

    @Override
    public String toString() {
        return "Arquivo: " + nomeDiretorio + " | Dono: " + donoDiretorio + " | Permissões: " + permissoesPadrao + " | Tam: "
                + tamanho + " bytes";
    }

    public void adicionarFilho(ElementoFS filho) {
        throw new UnsupportedOperationException("Não é possível adicionar filhos a um arquivo.");
    }


    public void removerFilho(String nomeFilho) {
        throw new UnsupportedOperationException("Não é possível remover filhos de um arquivo.");
    }
    
    @Override
    public boolean temPermissao(String usuario, char tipoPermissao) {
        if ("root".equals(usuario)) return true;
        if (usuario.equals(donoDiretorio)) {
            return permissoesPadrao.indexOf(tipoPermissao) >= 0;
        }
        // Arquivo não tem permissões específicas para outros usuários, só o dono pode acessar
        return false;
    }
}
