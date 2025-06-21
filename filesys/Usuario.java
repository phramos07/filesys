package filesys;

public class Usuario {
    private String nome;
    private String diretorio;
    private String permissoes;

    public Usuario(String nome, String diretorio, String permissoes) {
        this.nome = nome;
        this.diretorio = diretorio;
        this.permissoes = permissoes;
    }

    public String getNome() {
        return nome;
    }

    public String getDiretorio() {
        return diretorio;
    }

    public String getPermissoes() {
        return permissoes;
    }

    @Override
    public String toString() {
        return nome + " " + diretorio + " " + permissoes;
    }
}