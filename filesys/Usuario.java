package filesys;

public class Usuario {
    private String nome;
    private String diretorio;
    private String permissao;

    public String getNome() {
        return nome;
    }
    public void setNome(String nome) {
        this.nome = nome;
    }
    public String getDiretorio() {
        return diretorio;
    }
    public void setDiretorio(String diretorio) {
        this.diretorio = diretorio;
    }
    public String getPermissao() {
        return permissao;
    }
    public void setPermissao(String permissao) {
        this.permissao = permissao;
    }
    
    public Usuario(String nome, String diretorio, String permissao) {
        setDiretorio(diretorio);
        setNome(nome);
        setPermissao(permissao);
    }

    @Override
    public String toString(){
        return("Nome: "+ nome +"\n" 
                + "permissão: " + permissao + "\n"
                + "diretorio: " + diretorio + "\n");
    }
    
}
