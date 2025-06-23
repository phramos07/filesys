package filesys;

import java.util.HashMap;
import java.util.Map;

public class Diretorio {
  protected String nome;
  protected String permissoes;
  protected String dono;
  protected Diretorio pai;
  protected Map<String, Diretorio> filhos;
  protected Map<String, String> permissoesEspecificas;

  public Diretorio(String nome, String permissoes, String dono) {
    this.nome = nome;
    this.permissoes = permissoes;
    this.dono = dono;
    this.filhos = new HashMap<>();
    this.permissoesEspecificas = new HashMap<>();
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
    if ("root".equals(usuario))
      return true;

    // Dono tem todas as permissões
    if (usuario.equals(dono))
      return true;

    // Verifica permissões específicas do usuário no diretório atual
    String permissoesUsuario = permissoesEspecificas.get(usuario);
    if (permissoesUsuario != null && permissoesUsuario.indexOf(permissaoNecessaria) != -1) {
      return true;
    }

    // Se não houver permissões específicas, verifica permissões herdadas do pai
    Diretorio pai = getPai(); // Método para obter o diretório pai
    if (pai != null) {
      return pai.temPermissao(usuario, permissaoNecessaria);
    }

    return false; // Sem permissões
  }

  public String getPermissoesUsuario(String usuario) {
    if ("root".equals(usuario))
      return "rwx";
    if (usuario.equals(dono))
      return permissoes;
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

  public Diretorio getPai() {
    return pai;
  }

  protected void setPai(Diretorio pai) {
    this.pai = pai;
  }

  public void adicionarFilho(Diretorio filho) {
    filho.setPai(this);
    filhos.put(filho.getNome(), filho);
  }

  public void removerFilho(String nome) {
    filhos.remove(nome);
  }

  public boolean isArquivo() {
    return false; // Por padrão, um `Diretorio` não é um arquivo
  }

  @Override
  public String toString() {
    return "D " + permissoes + " " + dono + " " + nome;
  }
}