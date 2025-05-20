package model;

import java.util.ArrayList;
import java.util.List;

public class Arquivo extends ElementoFS {
    private List<byte[]> blocos;
    private long tamanho;

    public Arquivo(String nome, String permissoes, String dono) {
        super(nome, permissoes, dono);
        // this.arquivo = new Bloco[0];
        this.blocos = new ArrayList<>();
        this.tamanho = 0;
    }

    /*
     * public Bloco[] getArquivo() {
     * return arquivo;
     * }
     * 
     * public void setArquivo(Bloco[] arquivo) {
     * this.arquivo = arquivo;
     * }
     */

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
    
}
