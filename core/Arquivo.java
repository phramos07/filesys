package core;

import java.util.ArrayList;
import java.util.List;

public class Arquivo extends ElementoFS {
    private final List<byte[]> blocos;
    private long bytesTotais;

    public Arquivo(String nome, String permissoes, String dono) {
        super(nome, permissoes, dono);
        this.blocos = new ArrayList<>();
        this.bytesTotais = 0;
    }

    public void adicionarBloco(byte[] dados) {
        blocos.add(dados);
        bytesTotais += dados.length;
    }

    public List<byte[]> getBlocos() {
        return blocos;
    }

    public long getBytesTotais() {
        return bytesTotais;
    }

    public void removerBloco(int idx) {
        if (idx < 0 || idx >= blocos.size())
            throw new IndexOutOfBoundsException("Índice inválido para remoção de bloco.");
        byte[] removido = blocos.remove(idx);
        bytesTotais -= removido.length;
    }

    public void limpar() {
        blocos.clear();
        bytesTotais = 0;
    }

    @Override
    public boolean isArquivo() {
        return true;
    }

    @Override
    public String toString() {
        return "[ARQ] " + nomeDiretorio + " | Dono: " + donoDiretorio + " | Perm: " + permissoesPadrao + " | Tam: " + bytesTotais + " bytes";
    }

    public void inserirElemento(ElementoFS filho) {
        throw new UnsupportedOperationException("Arquivo não pode conter elementos.");
    }

    public void excluirElemento(String nome) {
        throw new UnsupportedOperationException("Arquivo não possui elementos para remover.");
    }
}