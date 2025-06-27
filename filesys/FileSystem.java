package filesys;

import exception.CaminhoJaExistenteException;
import exception.CaminhoNaoEncontradoException;
import exception.PermissaoException;

// Essa classe deve servir apenas como proxy para o FileSystemImpl
// Ela não deve conter a lógica de negócio do sistema de arquivos, apenas delegar.
final public class FileSystem implements IFileSystem {


    public final IFileSystem fileSystemImpl; 


    public FileSystem() {
        fileSystemImpl = new FileSystemImpl();
    }

    @Override
    public void chmod(String caminho, String usuario, String usuarioAlvo, String permissao)
            throws CaminhoNaoEncontradoException, PermissaoException {
        fileSystemImpl.chmod(caminho, usuario, usuarioAlvo, permissao);
    }

    @Override
    public void mkdir(String caminho, String usuario) throws CaminhoJaExistenteException, PermissaoException {
        fileSystemImpl.mkdir(caminho, usuario);
    }

    @Override
    public void rm(String caminho, String usuario, boolean recursivo)
            throws CaminhoNaoEncontradoException, PermissaoException {
        fileSystemImpl.rm(caminho, usuario, recursivo);
    }

    @Override
    // CORREÇÃO: Adicionado CaminhoNaoEncontradoException à assinatura.
    public void touch(String caminho, String usuario) throws CaminhoJaExistenteException, PermissaoException, CaminhoNaoEncontradoException {
        fileSystemImpl.touch(caminho, usuario);
    }

    @Override
    public void write(String caminho, String usuario, boolean anexar, byte[] buffer)
            throws CaminhoNaoEncontradoException, PermissaoException {
        fileSystemImpl.write(caminho, usuario, anexar, buffer);
    }

    @Override
    public void read(String caminho, String usuario, byte[] buffer)
            throws CaminhoNaoEncontradoException, PermissaoException {
        fileSystemImpl.read(caminho, usuario, buffer);
    }

    @Override
    public void mv(String caminhoAntigo, String caminhoNovo, String usuario)
            throws CaminhoNaoEncontradoException, PermissaoException {
        fileSystemImpl.mv(caminhoAntigo, caminhoNovo, usuario);
    }

    @Override
    public void ls(String caminho, String usuario, boolean recursivo)
            throws CaminhoNaoEncontradoException, PermissaoException {
        fileSystemImpl.ls(caminho, usuario, recursivo);
    }

    @Override
    public void cp(String caminhoOrigem, String caminhoDestino, String usuario, boolean recursivo)
            throws CaminhoNaoEncontradoException, PermissaoException, CaminhoJaExistenteException {
        fileSystemImpl.cp(caminhoOrigem, caminhoDestino, usuario, recursivo);
    }
}
