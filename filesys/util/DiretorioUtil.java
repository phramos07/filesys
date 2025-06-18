package filesys.util;

import java.util.Map;
import java.util.Set;
import exception.OperacaoInvalidaException;
import exception.PermissaoException;
import filesys.Diretorio;
import filesys.Usuario;

public class DiretorioUtil {

  private DiretorioUtil() {
  }

  public static Diretorio criarDiretorioFilho(Diretorio atual, String nome, String caminhoAtual, String usuario,
      Set<Usuario> users)
      throws PermissaoException {
    VerificacaoUtil.verificarPermissaoEscrita(atual, usuario, caminhoAtual);
    Usuario user = UsuarioUtil.buscarUsuario(usuario, users);
    Diretorio novo = new Diretorio(nome, atual.getPermissoes(), user.getNome());
    atual.adicionarFilho(novo);
    return novo;
  }

  public static Diretorio avancarParaDiretorioFilho(Diretorio atual, String nome)
      throws OperacaoInvalidaException, PermissaoException {
    Diretorio filho = atual.getFilhos().get(nome);

    if (filho == null) {
      throw new OperacaoInvalidaException("Diretório não encontrado: " + nome);
    }

    if (filho.isArquivo()) {
      throw new OperacaoInvalidaException("Não é possível criar um diretório dentro de um arquivo.");
    }
    VerificacaoUtil.verificarPermissaoExecucao(filho, filho.getDono(), nome);

    return filho;
  }

  public static void removerRecursivo(Diretorio dir, String usuario) throws PermissaoException {
    for (Diretorio filho : dir.getFilhos().values()) {
      VerificacaoUtil.verificarPermissaoEscrita(filho, usuario, dir.getNome());
      if (!filho.isArquivo()) {
        removerRecursivo(filho, usuario);
      }
    }
    dir.getFilhos().clear();
  }

  public static String extrairNomeArquivo(String caminho) {
    return caminho.substring(caminho.lastIndexOf('/') + 1);
  }

  private static void listarDiretorioAtual(Diretorio dir, String caminho, StringBuilder output) {
    output.append(caminho).append(":\n");
    for (Map.Entry<String, Diretorio> entry : dir.getFilhos().entrySet()) {
      Diretorio filho = entry.getValue();
      output.append("  ").append(filho.toString()).append("\n");
    }
  }

  private static void listarRecursivamente(Diretorio dir, String caminho, StringBuilder output, String usuario) {
    for (Map.Entry<String, Diretorio> entry : dir.getFilhos().entrySet()) {
      Diretorio filho = entry.getValue();
      if (!filho.isArquivo()) {
        String novoCaminho = caminho.equals("/") ? "/" + filho.getNome() : caminho + "/" + filho.getNome();
        listarDiretorioAtual(filho, novoCaminho, output);
        listarRecursivamente(filho, novoCaminho, output, usuario);
      }
    }
  }

  public static String listar(Diretorio dir, String caminho, boolean recursivo, String usuario) {
    StringBuilder output = new StringBuilder();
    listarDiretorioAtual(dir, caminho, output);

    if (recursivo) {
      listarRecursivamente(dir, caminho, output, usuario);
    }

    return output.toString();
  }

}
