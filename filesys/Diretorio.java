package filesys;

import java.util.HashMap;
import java.util.Map;

public class Diretorio {
  protected String nome;
  protected String permissoes;
  protected String dono;
  protected Map<String, Diretorio> filhos;
  protected Map<String, String> permissoesEspecificas = new HashMap<>();

  public Diretorio(String nome, String permissoes, String dono) {
    this.nome = nome;
    this.permissoes = permissoes;
    this.dono = dono;
    this.filhos = new HashMap<>();
  }

  public void setPermissaoUsuario(String usuario, String permissao) {
    // Valida o formato da permissão (rwx com 3 caracteres)
    if (permissao == null || permissao.length() != 3) {
      throw new IllegalArgumentException("Permissão deve ter 3 caracteres (rwx)");
    }
    permissoesEspecificas.put(usuario, permissao);
  }

  public boolean temPermissao(String usuario, char permissaoNecessaria) {
    // Root tem todas as permissões
    if ("root".equals(usuario)) return true;

    // Dono tem todas as permissões
    if (usuario.equals(dono)) return true;

    // Verifica permissões específicas do usuário
    String permissoesUsuario = permissoesEspecificas.get(usuario);
    if (permissoesUsuario != null && permissoesUsuario.indexOf(permissaoNecessaria) != -1) {
      return true;
    }

    return false;
  }

  public String getPermissoesUsuario(String usuario) {
    if ("root".equals(usuario)) return "rwx";
    if (usuario.equals(dono)) return permissoes;
    return permissoesEspecificas.getOrDefault(usuario, "---");
  }

  public String getNome() {
    return nome;
  }

  public String getPermissoes() {
    return permissoes;
  }

  public void setPermissoes(String permissoes) {
    this.permissoes = permissoes;
  }

  public String getDono() {
    return dono;
  }

  public void setNome(String nome) {
    this.nome = nome;
  }

  public void setDono(String dono) {
    this.dono = dono;
  }

  public Map<String, Diretorio> getFilhos() {
    return filhos;
  }

  public void adicionarFilho(Diretorio filho) {
    filhos.put(filho.getNome(), filho);
  }

  public void removerFilho(String nome) {
    filhos.remove(nome);
  }

  public boolean isArquivo() {
    return false; // Por padrão, um `Diretorio` não é um arquivo
  }
}
