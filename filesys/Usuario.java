package filesys;

public class Usuario {
    
    private String diretorio;
    private String nome;
    private String permissoes;

    public Usuario(String nome, String diretorio, String permissoes) {
        this.nome = nome;
        this.diretorio = diretorio;
        this.permissoes = permissoes;
    }

    public String getDiretorio() {
        return diretorio;
    }

    public String getNome() {
        return nome;
    }

    public String getPermissoes() {
        return permissoes;
    }

    public void setDiretorio(String diretorio) {
        this.diretorio = diretorio;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setPermissoes(String permissoes) {
        this.permissoes = permissoes;
    }

    @Override
    public String toString() {
        return "Usuario{" +
                "diretorio='" + diretorio + '\'' +
                ", nome='" + nome + '\'' +
                ", permissoes='" + permissoes + '\'' +
                '}';
    }
}
