package filesys;

public class Usuario {
  private String nome;
  private String permissao;
  private String dir;

  public Usuario(String nome, String permissao, String dir) {
    this.setNome(nome);
    this.setPermissao(permissao);
    this.setDir(dir);
  }

  public String getNome() {
    return nome;
  }

  public void setNome(String nome) {
    this.nome = nome;
  }

  public String getPermissao() {
    return permissao;
  }

  public void setPermissao(String permissao) {
    this.permissao = permissao;
  }

  public String getDir() {
    return dir;
  }

  public void setDir(String dir) {
    this.dir = dir;
  }

  @Override
  public String toString() {
    return "Usuario{" +
            "nome='" + nome + '\'' +
            ", permissao='" + permissao + '\'' +
            ", dir='" + dir + '\'' +
            '}';
  }
}