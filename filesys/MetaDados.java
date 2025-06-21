package filesys;

import java.util.HashMap;
import java.util.Map;

public class MetaDados {
  private String nome;
  private int tamanho;
  private String dono;
  // <usuario, "rwx">
  private Map<String, String> permissoes;

  public MetaDados(String nome, String dono) {
      this.nome = nome;
      this.dono = dono;
      this.tamanho = 0;
      this.permissoes = new HashMap<>();
      // Dono tem permissão total por padrão
      this.permissoes.put(dono, "rwx");
  }

  public String getNome() { return nome; }
  public void setNome(String nome) { this.nome = nome; }

  public int getTamanho() { return tamanho; }
  public void setTamanho(int tamanho) { this.tamanho = tamanho; }

  public String getDono() { return dono; }
  public void setDono(String dono) { this.dono = dono; }

  public Map<String, String> getPermissoes() { return permissoes; }

  public void setPermissao(String usuario, String permissao) {
      permissoes.put(usuario, permissao);
  }

  public String getPermissao(String usuario) {
      return permissoes.getOrDefault(usuario, "---");
  }

  public boolean hasPermissao(String usuario, char tipo) {
      if (usuario.equals("root")) return true;
      String p = permissoes.get(usuario);
      if (p == null) return false;
      switch (tipo) {
          case 'r': return p.charAt(0) == 'r';
          case 'w': return p.charAt(1) == 'w';
          case 'x': return p.charAt(2) == 'x';
          default: return false;
      }
  }
}