package filesys;
import java.util.HashMap;

public class MetaDados {
    private String nome;
    private int tamanho;
    private String dono;

    // <"user", "rwx">
    private HashMap<String, String> permissoes;

    public MetaDados(String nome, int tamanho, String dono) {
        this.nome = nome;
        this.tamanho = tamanho;
        this.dono = dono;
        this.permissoes = new HashMap<>();
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

    public void setPermissao(String usuario, String permissao) {
        permissoes.put(usuario, permissao);
    }

    public String getPermissao(String usuario) {
        return permissoes.get(usuario);
    }
}