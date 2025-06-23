package filesys.util;

import exception.PermissaoException;
import filesys.Arquivo;
import filesys.Diretorio;
import filesys.Offset;
import filesys.Usuario;

public class ArquivoUtil {

  private ArquivoUtil() {
  }

  public static Arquivo criarNovoArquivo(String nomeArquivo, Usuario user) {
    return new Arquivo(nomeArquivo, user.getPermissao(), user.getNome());
  }

  public static void escreverBufferNoArquivo(Arquivo arquivo, byte[] buffer, boolean anexar) {
    if (!anexar) {
      arquivo.clearBlocos();
    }

    int offset = 0;
    int tamMax = arquivo.getTamMaxBloco();

    while (offset < buffer.length) {
      int length = Math.min(buffer.length - offset, tamMax);
      byte[] dados = new byte[length];
      System.arraycopy(buffer, offset, dados, 0, length);

      arquivo.addBloco(new Arquivo.Bloco(dados));
      arquivo.incrementTamnho(length);
      offset += length;
    }
  }

  public static void atualizarOffsetLimite(Offset offset, long fileSize) {
    offset.setMax((int) fileSize);
  }

  public static void lerDadosDoArquivo(Arquivo arquivo, byte[] buffer, Offset offset) {
    int arquivoOffset = offset.getValue();
    int bufferOffset = 0;

    for (Arquivo.Bloco bloco : arquivo.getBlocos()) {
      if (arquivoOffset >= bloco.getDados().length) {
        arquivoOffset -= bloco.getDados().length;
        continue;
      }

      int bytesDisponiveis = bloco.getDados().length - arquivoOffset;
      int bytesParaLer = Math.min(buffer.length - bufferOffset, bytesDisponiveis);
      System.arraycopy(bloco.getDados(), arquivoOffset, buffer, bufferOffset, bytesParaLer);

      bufferOffset += bytesParaLer;
      arquivoOffset = 0;

      if (bufferOffset >= buffer.length)
        break;
    }

    offset.add(bufferOffset);
  }

  public static void executarMovimentacao(Diretorio paiAntigo, Diretorio paiNovo, String nomeAntigo, String nomeNovo,
      Diretorio alvo) {
    paiAntigo.removerFilho(nomeAntigo);
    alvo.setNome(nomeNovo);
    paiNovo.adicionarFilho(alvo);
  }

  public static void copiarArquivo(Arquivo origem, Diretorio destino) {
    Arquivo copia = new Arquivo(origem.getNome(), origem.getPermissoes(), origem.getDono());
    for (Arquivo.Bloco bloco : origem.getBlocos()) {
      copia.addBloco(bloco);
      copia.incrementTamnho(bloco.getDados().length);
    }
    destino.adicionarFilho(copia);
  }

  public static void copiarDiretorio(Diretorio origem, Diretorio destino, String usuario) throws PermissaoException {
    Diretorio copia = new Diretorio(origem.getNome(), origem.getPermissoes(), origem.getDono());
    destino.adicionarFilho(copia);
    for (Diretorio filho : origem.getFilhos().values()) {
      if (filho.isArquivo()) {
        copiarArquivo((Arquivo) filho, copia);
      } else {
        copiarDiretorio(filho, copia, usuario);
      }
    }
  }

}