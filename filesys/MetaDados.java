package filesys;

import java.util.HashMap;

public class MetaDados {
     private String nome;
    private int tamanho;
    private String dono;
    private HashMap<String, String> permissoes;

    public MetaDados(String nome, int tamanho, String dono) {
        this.setNome(nome);
        this.setTamanho(tamanho);
        this.setDono(dono);
        this.permissoes = new HashMap<>();
    }

    public MetaDados(String nome, String dono) {
        this(nome, 0, dono);
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public int getTamanho() {
        return tamanho;
    }

    public void setTamanho(int tamanho) {
        this.tamanho = tamanho;
    }

    public String getDono() {
        return dono;
    }

    public void setDono(String dono) {
        this.dono = dono;
    }

    public HashMap<String, String> getPermissoes() {
        return permissoes;
    }

    public void setPermissoes(HashMap<String, String> permissoes) {
        this.permissoes = permissoes;
    }

    public void addPermissao(String usuario, String permissao) {
        this.permissoes.put(usuario, permissao);
    }

    public String getPermissao(String usuario) {
        return this.permissoes.getOrDefault(usuario, "nenhuma");
    }

    public boolean hasPermissao(String usuario, String permissao) {
        String perm = this.permissoes.get(usuario);
        return perm != null && (perm.equals("leitura") || perm.equals("escrita") || perm.equals("leitura-escrita"));
    }

    public boolean isDono(String usuario) {
        return this.dono.equals(usuario);
    }

    @Override
    public String toString() {
        return "MetaDados{" +
                "nome='" + nome + '\'' +
                ", tamanho=" + tamanho +
                ", dono='" + dono + '\'' +
                ", permissoes=" + permissoes +
                '}';
    }
    
}
