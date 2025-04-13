package filesys;

import exception.CaminhoJaExistenteException;
import exception.CaminhoNaoEncontradoException;
import exception.PermissaoException;

// Implemente nesta classe o seu código do FileSystem.
// A classe pode ser alterada.
// O construtor, argumentos do construtor podem ser modificados 
// e atributos & métodos privados podem ser adicionados
public final class FileSystemImpl implements IFileSystem {
    private static final String ROOT_USER = "root"; // pode ser necessário
    private Diretorio root;

    public FileSystemImpl() {}

    private Diretorio navegar(String caminho) throws CaminhoNaoEncontradoException {
        if (caminho.equals("/")) {
            return root;
        }

        String[] partes = caminho.split("/");
        Diretorio atual = root;

        for (String parte : partes) {
            if (parte.isEmpty()) continue;
            if (!atual.getFilhos().containsKey(parte)) {
                throw new CaminhoNaoEncontradoException("Caminho não encontrado: " + caminho);
            }
            atual = atual.getFilhos().get(parte);
        }

        return atual;
    }

    @Override
    public void mkdir(String caminho, String usuario) throws CaminhoJaExistenteException, PermissaoException, CaminhoNaoEncontradoException {
        Diretorio parent = navegar(caminho.substring(0, caminho.lastIndexOf('/')));
        String nomeDiretorio = caminho.substring(caminho.lastIndexOf('/') + 1);

        if (parent.isArquivo()) {
            throw new UnsupportedOperationException("Não é possível criar um diretório dentro de um arquivo.");
        }

        if (parent.getFilhos().containsKey(nomeDiretorio)) {
            throw new CaminhoJaExistenteException("Diretório já existe: " + nomeDiretorio);
        }

        parent.adicionarFilho(new Diretorio(nomeDiretorio, "rwx", usuario));
    }

    @Override
    public void chmod(String caminho, String usuario, String usuarioAlvo, String permissao)
            throws CaminhoNaoEncontradoException, PermissaoException {
        throw new UnsupportedOperationException("Método não implementado 'chmod'");
    }

    @Override
    public void rm(String caminho, String usuario, boolean recursivo)
            throws CaminhoNaoEncontradoException, PermissaoException {
        throw new UnsupportedOperationException("Método não implementado 'rm'");
    }

    @Override
    public void touch(String caminho, String usuario) throws CaminhoJaExistenteException, PermissaoException, CaminhoNaoEncontradoException {
        Diretorio parent = navegar(caminho.substring(0, caminho.lastIndexOf('/')));
        String nomeArquivo = caminho.substring(caminho.lastIndexOf('/') + 1);

        if (parent.isArquivo()) {
            throw new PermissaoException("Não é possível criar um arquivo dentro de um arquivo.");
        }

        if (parent.getFilhos().containsKey(nomeArquivo)) {
            throw new CaminhoJaExistenteException("Arquivo já existe: " + nomeArquivo);
        }

        parent.adicionarFilho(new Arquivo(nomeArquivo, "rw-", usuario));
    }

    @Override
    public void write(String caminho, String usuario, boolean anexar, byte[] buffer)
            throws CaminhoNaoEncontradoException, PermissaoException {
        throw new UnsupportedOperationException("Método não implementado 'write'");
    }

    @Override
    public void read(String caminho, String usuario, byte[] buffer)
            throws CaminhoNaoEncontradoException, PermissaoException {
        throw new UnsupportedOperationException("Método não implementado 'read'");
    }

    @Override
    public void mv(String caminhoAntigo, String caminhoNovo, String usuario)
            throws CaminhoNaoEncontradoException, PermissaoException {
        throw new UnsupportedOperationException("Método não implementado 'mv'");
    }

    @Override
    public void ls(String caminho, String usuario, boolean recursivo) throws CaminhoNaoEncontradoException, PermissaoException {
        throw new UnsupportedOperationException("Método não implementado 'ls'");
    }

    @Override
    public void cp(String caminhoOrigem, String caminhoDestino, String usuario, boolean recursivo)
            throws CaminhoNaoEncontradoException, PermissaoException {
        throw new UnsupportedOperationException("Método não implementado 'cp'");
    }

    public void addUser(String user) {
        throw new UnsupportedOperationException("Método não implementado 'addUser'");
    }
}
