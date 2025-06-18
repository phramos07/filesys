package filesys.util;

import java.util.StringTokenizer;

import exception.CaminhoNaoEncontradoException;
import exception.PermissaoException;
import filesys.Diretorio;

public class NavegacaoUtil {
  private NavegacaoUtil() {
  }

  public static Diretorio navegar(String caminho, Diretorio root, String usuario)
      throws CaminhoNaoEncontradoException, PermissaoException {

    if (caminho.equals("/") || caminho.isEmpty()) {
      VerificacaoUtil.verificarPermissaoExecucao(root, usuario, caminho);
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
      VerificacaoUtil.verificarPermissaoExecucao(atual, usuario, caminhoAtual.toString());
    }

    return atual;
  }

  public static Diretorio obterDiretorioPai(String caminho, Diretorio root, String usuario)
      throws CaminhoNaoEncontradoException, PermissaoException {
    String caminhoPai = caminho.substring(0, caminho.lastIndexOf('/'));
    return navegar(caminhoPai.isEmpty() ? "/" : caminhoPai, root, usuario);
  }

  public static Diretorio obterAlvo(Diretorio pai, String nomeAlvo, String caminho)
      throws CaminhoNaoEncontradoException {
    Diretorio alvo = pai.getFilhos().get(nomeAlvo);
    if (alvo == null) {
      throw new CaminhoNaoEncontradoException("Caminho não encontrado: " + caminho);
    }
    return alvo;
  }

  public static String extrairCaminhoPai(String caminho) {
    int lastSlash = caminho.lastIndexOf('/');
    return (lastSlash <= 0) ? "/" : caminho.substring(0, lastSlash);
  }

  public static String obterNomeAlvo(String caminho) {
    return caminho.substring(caminho.lastIndexOf('/') + 1);
  }
}
