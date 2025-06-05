package filesys;

public class Usuario {
    private final String nome;
    private final String permissao; // ex: "rwx", "rw-", "r--"

    public Usuario(String nome, String permissao) {
        this.nome = nome;
        this.permissao = permissao;
    }

    public String getNome() {
        return nome;
    }

    public boolean podeLer() {
        return permissao.contains("r");
    }

    public boolean podeEscrever() {
        return permissao.contains("w");
    }

    public boolean podeExecutar() {
        return permissao.contains("x");
    }

    public String getPermissao() {
        return permissao;
    }
}
