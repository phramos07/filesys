package filesys;

public class Arquivo extends Diretorio {
    private byte[] conteudo;

    public Arquivo(String nome, String permissoes, String dono) {
        super(nome, permissoes, dono);
        this.conteudo = new byte[0];
        this.filhos = null; // Arquivos não podem ter filhos
    }

    public byte[] getConteudo() {
        return conteudo;
    }

    public void setConteudo(byte[] conteudo) {
        this.conteudo = conteudo;
    }

    @Override
    public boolean isArquivo() {
        return true; // Um `File` é um arquivo
    }

    @Override
    public void adicionarFilho(Diretorio filho) {
        throw new UnsupportedOperationException("Arquivos não podem conter filhos.");
    }

    @Override
    public void removerFilho(String nome) {
        throw new UnsupportedOperationException("Arquivos não podem conter filhos.");
    }
}