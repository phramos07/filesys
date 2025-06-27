package filesys;

import exception.CaminhoJaExistenteException;
import exception.CaminhoNaoEncontradoException;
import exception.PermissaoException;

// Interface principal do sistema de arquivos virtual
public interface IFileSystem {
    void chmod(String caminho, String usuario, String usuarioAlvo, String permissao) throws CaminhoNaoEncontradoException, PermissaoException;
    void mkdir(String caminho, String usuario) throws CaminhoJaExistenteException, PermissaoException;
    void rm(String caminho, String usuario, boolean recursivo) throws CaminhoNaoEncontradoException, PermissaoException;
    void touch(String caminho, String usuario) throws CaminhoJaExistenteException, PermissaoException, CaminhoNaoEncontradoException;
    void write(String caminho, String usuario, boolean anexar, byte[] buffer) throws CaminhoNaoEncontradoException, PermissaoException;
    void read(String caminho, String usuario, byte[] buffer) throws CaminhoNaoEncontradoException, PermissaoException;
    void mv(String caminhoAntigo, String caminhoNovo, String usuario) throws CaminhoNaoEncontradoException, PermissaoException;
    void ls(String caminho, String usuario, boolean recursivo) throws CaminhoNaoEncontradoException, PermissaoException;
    void cp(String caminhoOrigem, String caminhoDestino, String usuario, boolean recursivo) throws CaminhoNaoEncontradoException, PermissaoException, CaminhoJaExistenteException;
}