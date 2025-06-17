package filesys.util;

import exception.CaminhoJaExistenteException;
import exception.OperacaoInvalidaException;
import exception.PermissaoException;
import filesys.Diretorio;

public class VerificacaoUtil {

  private VerificacaoUtil() {
  }

  public static void verificarPermissaoEscrita(Diretorio diretorio, String usuario, String caminhoAtual)
      throws PermissaoException {
    if (!diretorio.temPermissao(usuario, 'w')) {
      throw new PermissaoException("Sem permissão para criar em: " + caminhoAtual);
    }
  }

  public static void verificarPermissaoExecucao(Diretorio diretorio, String usuario, String caminhoAtual)
      throws PermissaoException {
    if (!diretorio.temPermissao(usuario, 'x')) {
      throw new PermissaoException("Sem permissão de execução para acessar: " + caminhoAtual);
    }
  }


  public static void verificarLeituraDiretorio(Diretorio dir, String usuario, String caminho)
      throws PermissaoException {
    if (!dir.temPermissao(usuario, 'r')) {
      throw new PermissaoException("Sem permissão de leitura para listar o diretório: " + caminho);
    }
  }

  public static void verificarRemocaoDiretorio(Diretorio alvo, boolean recursivo) throws PermissaoException {
    if (!recursivo && !alvo.getFilhos().isEmpty()) {
      throw new PermissaoException("Diretório não está vazio. Use recursivo=true.");
    }
  }

  public static void verificarCaminhoArquivo(String caminho) throws OperacaoInvalidaException {
    if (caminho.equals("/") || caminho.endsWith("/")) {
      throw new OperacaoInvalidaException("Nome de arquivo inválido.");
    }
  }

  public static void verificarSeEhArquivo(Diretorio parent) throws OperacaoInvalidaException {
    if (parent.isArquivo()) {
      throw new OperacaoInvalidaException("Não é possível criar um arquivo dentro de um arquivo.");
    }
  }

  public static void verificarSeArquivoExiste(Diretorio parent, String nomeArquivo)
      throws CaminhoJaExistenteException {
    if (parent.getFilhos().containsKey(nomeArquivo)) {
      throw new CaminhoJaExistenteException("Arquivo já existe: " + nomeArquivo);
    }
  }

  public static void verificarEscritaArquivo(Diretorio dir, String usuario)
      throws OperacaoInvalidaException, PermissaoException {
    if (!dir.isArquivo()) {
      throw new OperacaoInvalidaException("Não é possível escrever em um diretório.");
    }
    if (!dir.temPermissao(usuario, 'w')) {
      throw new PermissaoException("Sem permissão de escrita.");
    }
  }

  public static void verificarLeituraArquivo(Diretorio dir, String usuario)
      throws OperacaoInvalidaException, PermissaoException {
    if (!dir.isArquivo())
      throw new OperacaoInvalidaException("Não é possível ler de um diretório.");
    if (!dir.temPermissao(usuario, 'r'))
      throw new PermissaoException("Sem permissão de leitura.");
  }

  public static void verificarMovimentacaoPermitida(String origem, String destino) throws PermissaoException {
    if (origem.equals("/") || destino.equals("/")) {
      throw new PermissaoException("Não é possível mover o diretório raiz.");
    }
  }

  public static void verificarDestinoDisponivel(Diretorio paiDestino, String nomeDestino)
      throws PermissaoException {
    if (paiDestino.getFilhos().containsKey(nomeDestino)) {
      throw new PermissaoException("Já existe um item no destino com esse nome.");
    }
  }

  public static void verificarPermissoesParaCopia(Diretorio origem, Diretorio destino, String usuario)
      throws PermissaoException {
    if (!origem.temPermissao(usuario, 'r')) {
      throw new PermissaoException("Sem permissão de leitura na origem.");
    }
    if (!destino.temPermissao(usuario, 'w')) {
      throw new PermissaoException("Sem permissão de escrita no destino.");
    }
  }

}
