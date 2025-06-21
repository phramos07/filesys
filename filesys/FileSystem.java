package filesys;

import java.util.List;

import exception.CaminhoJaExistenteException;
import exception.CaminhoNaoEncontradoException;
import exception.PermissaoException;

/**
 * Classe proxy para o sistema de arquivos. Encaminha todas as operações para
 * uma implementação de IFileSystem.
 *
 * <p>
 * Permite trocar facilmente a implementação do sistema de arquivos sem alterar
 * o restante do código.
 * </p>
 *
 * <p>
 * Todos os métodos apenas delegam as chamadas para a instância de
 * FileSystemImpl.
 * </p>
 *
 * @author SeuNome
 */
final public class FileSystem implements IFileSystem {

    /** Implementação real do sistema de arquivos. */
    private final IFileSystem fileSystemImpl;

    /**
     * Construtor padrão. Cria o sistema de arquivos apenas com o usuário root.
     */
    public FileSystem() {
        fileSystemImpl = new FileSystemImpl();
    }

    /**
     * Construtor que inicializa o sistema de arquivos com uma lista de usuários.
     * 
     * @param usuarios Lista de usuários
     */
    public FileSystem(List<Usuario> usuarios) {
        fileSystemImpl = new FileSystemImpl(usuarios);
    }

    /** {@inheritDoc} */
    @Override
    public void chmod(String caminho, String usuario, String usuarioAlvo, String permissao)
            throws CaminhoNaoEncontradoException, PermissaoException {
        fileSystemImpl.chmod(caminho, usuario, usuarioAlvo, permissao);
    }

    /** {@inheritDoc} */
    @Override
    public void mkdir(String caminho, String usuario)
            throws CaminhoJaExistenteException, PermissaoException, CaminhoNaoEncontradoException {
        fileSystemImpl.mkdir(caminho, usuario);
    }

    /** {@inheritDoc} */
    @Override
    public void rm(String caminho, String usuario, boolean recursivo)
            throws CaminhoNaoEncontradoException, PermissaoException {
        fileSystemImpl.rm(caminho, usuario, recursivo);
    }

    /** {@inheritDoc} */
    @Override
    public void touch(String caminho, String usuario)
            throws CaminhoJaExistenteException, PermissaoException, CaminhoNaoEncontradoException {
        fileSystemImpl.touch(caminho, usuario);
    }

    /** {@inheritDoc} */
    @Override
    public void write(String caminho, String usuario, boolean anexar, Offset offset, byte[] buffer)
            throws CaminhoNaoEncontradoException, PermissaoException {
        fileSystemImpl.write(caminho, usuario, anexar, offset, buffer);
    }

    /** {@inheritDoc} */
    @Override
    public void read(String caminho, String usuario, byte[] buffer)
            throws CaminhoNaoEncontradoException, PermissaoException {
        fileSystemImpl.read(caminho, usuario, buffer);
    }

    /** {@inheritDoc} */
    @Override
    public void mv(String caminhoAntigo, String caminhoNovo, String usuario)
            throws CaminhoNaoEncontradoException, PermissaoException {
        fileSystemImpl.mv(caminhoAntigo, caminhoNovo, usuario);
    }

    /** {@inheritDoc} */
    @Override
    public void ls(String caminho, String usuario, boolean recursivo)
            throws CaminhoNaoEncontradoException, PermissaoException {
        fileSystemImpl.ls(caminho, usuario, recursivo);
    }

    /** {@inheritDoc} */
    @Override
    public void cp(String caminhoOrigem, String caminhoDestino, String usuario, boolean recursivo)
            throws CaminhoNaoEncontradoException, PermissaoException {
        fileSystemImpl.cp(caminhoOrigem, caminhoDestino, usuario, recursivo);
    }

    /** {@inheritDoc} */
    @Override
    public void addUser(String nome, String diretorio, String permissoes) throws CaminhoNaoEncontradoException {
        fileSystemImpl.addUser(nome, diretorio, permissoes);
    }

    /** {@inheritDoc} */
    @Override
    public void removeUser(String nome) throws CaminhoNaoEncontradoException, PermissaoException {
        fileSystemImpl.removeUser(nome);
    }

}