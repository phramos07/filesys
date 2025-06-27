package filesys;

public class Usuario {
    private String nome;
    private String diretorio;
    private String permissao; 

    public Usuario(String nome, String permissao, String diretorio) {
        this.setNome(nome);
        this.setPermissao(permissao);
        this.setDiretorio(diretorio);
    }

    public String getNome() {
        return nome;
    }

    public String getPermissao() {
        return permissao;
    }

    public void setPermissao(String permissao) {
        this.permissao = permissao;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setDiretorio(String diretorio) {
        this.diretorio = diretorio;
    }

    

    @Override
    public String toString() {
        return "Usuario{" +
                "nome='" + nome + '\'' +
                ", permissao='" + permissao + '\'' +
                ", diretorio='" + diretorio + '\'' +
                '}';
    }

    public String getDiretorio() {
        return diretorio;
    }

    
    
}