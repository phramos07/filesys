package filesys.util;

import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import exception.CaminhoNaoEncontradoException;
import exception.OperacaoInvalidaException;
import exception.PermissaoException;
import filesys.Diretorio;
import filesys.Usuario;

public class DiretorioUtil {

  private DiretorioUtil() {
  }

  public static Diretorio obterDiretorioPai(String caminho, Diretorio root, String usuario)
      throws CaminhoNaoEncontradoException, PermissaoException {
    String caminhoPai = caminho.substring(0, caminho.lastIndexOf('/'));
    return navegar(caminhoPai.isEmpty() ? "/" : caminhoPai, root, usuario);
  }

  

  public static Diretorio navegar(String caminho, Diretorio root, String usuario)
      throws CaminhoNaoEncontradoException, PermissaoException {

    if (caminho.equals("/") || caminho.isEmpty()) {
      return root;
    }

    Diretorio atual = root;
    StringTokenizer tokenizer = new StringTokenizer(caminho, "/");
    StringBuilder caminhoAtual = new StringBuilder("/");

    while (tokenizer.hasMoreTokens()) {
      String parte = tokenizer.nextToken();

      if (!atual.getFilhos().containsKey(parte)) {
        throw new CaminhoNaoEncontradoException("Caminho não encontrado: " + caminho);
      }

      atual = atual.getFilhos().get(parte);
      caminhoAtual.append(parte).append("/");
    }

    return atual;
  }

  public static Diretorio obterAlvo(Diretorio pai, String nomeAlvo, String caminho)
      throws CaminhoNaoEncontradoException {
    Diretorio alvo = pai.getFilhos().get(nomeAlvo);
    if (alvo == null) {
      throw new CaminhoNaoEncontradoException("Caminho não encontrado: " + caminho);
    }
    return alvo;
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

  public static Diretorio avancarParaDiretorioFilho(Diretorio atual, String nome) throws OperacaoInvalidaException {
    Diretorio filho = atual.getFilhos().get(nome);
    if (filho.isArquivo()) {
      throw new OperacaoInvalidaException("Não é possível criar um diretório dentro de um arquivo.");
    }
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

  public static String extrairCaminhoPai(String caminho) {
    int lastSlash = caminho.lastIndexOf('/');
    return (lastSlash <= 0) ? "/" : caminho.substring(0, lastSlash);
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

  public static String obterNomeAlvo(String caminho) {
    return caminho.substring(caminho.lastIndexOf('/') + 1);
  }
}
