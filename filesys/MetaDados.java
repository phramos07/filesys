package filesys;

/*classe base que tem o nome, permissões básicas e o dono, tanto Diretorio quanto Arquivo a implementam */
public abstract class MetaDados {
    
    protected String nome;
    protected String permissoesBasicas;
    protected String dono;

    public String getNome() {
        return nome;
    }
    public void setNome(String nome) {
        this.nome = nome;
    }
    public String getPermissoesBasicas() {
        return permissoesBasicas;
    }
    public void setPermissoesBasicas(String permissoesBasicas) {
        this.permissoesBasicas = permissoesBasicas;
    }
    public String getDono() {
        return dono;
    }
    public void setDono(String dono) {
        this.dono = dono;
    }

    public MetaDados(String nome, String permissoesBasicas, String dono) {
        setNome(nome);
        setPermissoesBasicas(permissoesBasicas);
        setDono(dono);
    }
    
    public abstract void alterarPermissao(String usuarioAlvo, String permissao);
       
}
