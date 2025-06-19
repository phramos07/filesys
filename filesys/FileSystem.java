package filesys;

import exception.CaminhoJaExistenteException;
import exception.CaminhoNaoEncontradoException;
import exception.OperacaoInvalidaException;
import exception.PermissaoException;

/**
 * Classe {@code FileSystem} atua como um proxy para a implementação real do
 * sistema de arquivos {@link FileSystemImpl}.
 * 
 * <p>
 * Essa classe encapsula a lógica interna e fornece uma interface padronizada
 * para operações em sistemas de arquivos,
 * delegando todas as chamadas de método à instância de {@code FileSystemImpl}.
 *
 * <p>
 * Inicializa com um diretório raiz padrão ("/") e um usuário padrão ("root")
 * com permissões completas.
 */
final public class FileSystem implements IFileSystem {

    private final IFileSystem fileSystemImpl;

    public FileSystem() {
        fileSystemImpl = new FileSystemImpl();
    }

    @Override
    public void chmod(String caminho, String usuario, String usuarioAlvo, String permissao)
            throws CaminhoNaoEncontradoException, PermissaoException {
        fileSystemImpl.chmod(caminho, usuario, usuarioAlvo, permissao);
    }

    @Override
    public void mkdir(String caminho, String usuario)
            throws CaminhoJaExistenteException, PermissaoException, OperacaoInvalidaException {
        fileSystemImpl.mkdir(caminho, usuario);
    }

    @Override
    public void rm(String caminho, String usuario, boolean recursivo)
            throws CaminhoNaoEncontradoException, PermissaoException {
        fileSystemImpl.rm(caminho, usuario, recursivo);
    }

    @Override
    public void touch(String caminho, String usuario) throws CaminhoJaExistenteException, PermissaoException,
            CaminhoNaoEncontradoException, OperacaoInvalidaException {
        fileSystemImpl.touch(caminho, usuario);
    }

    @Override
    public void write(String caminho, String usuario, boolean anexar, byte[] buffer)
            throws CaminhoNaoEncontradoException, PermissaoException, OperacaoInvalidaException {
        fileSystemImpl.write(caminho, usuario, anexar, buffer);
    }

    @Override
    public void read(String caminho, String usuario, byte[] buffer, Offset offset)
            throws CaminhoNaoEncontradoException, PermissaoException, OperacaoInvalidaException {
        fileSystemImpl.read(caminho, usuario, buffer, offset);
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
            throws CaminhoNaoEncontradoException, PermissaoException {
        fileSystemImpl.cp(caminhoOrigem, caminhoDestino, usuario, recursivo);
    }

    @Override
    public void addUser(Usuario user) {
        fileSystemImpl.addUser(user);
    }

    @Override
    public void removeUser(String username) {
        fileSystemImpl.removeUser(username);
    }
}