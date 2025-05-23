package filesys;

import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import exception.CaminhoJaExistenteException;
import exception.CaminhoNaoEncontradoException;
import exception.PermissaoException;

// Implemente nesta classe o seu código do FileSystem.
// A classe pode ser alterada.
// O construtor, argumentos do construtor podem ser modificados 
// e atributos & métodos privados podem ser adicionados

public final class FileSystemImpl implements IFileSystem {
    
    private static final String ROOT_USER = "root"; // pode ser necessário

    private Map<String, Map<String, String>> permissoes;

    public FileSystemImpl(Map<String, Map<String, String>> permissoes) {
        this.permissoes = permissoes;
        // Inicialização dos diretórios, arquivos, etc.
    }

    public FileSystemImpl() {
        this.permissoes = new HashMap<>();
        // Inicialização de diretórios, etc.
    }

    @Override
    public void mkdir(String caminho, String usuario) throws CaminhoJaExistenteException, PermissaoException {

        if (!usuarioExiste(usuario)) {
            throw new PermissaoException("Usuário não fornecido ou inexistente.");
        }

        //... restante da lógica
        // Ta dando pau

        throw new UnsupportedOperationException("Método não implementado 'mkdir'");
    }

    @Override
    public void chmod(String caminho, String usuario, String usuarioAlvo, String permissao)
            throws CaminhoNaoEncontradoException, PermissaoException {
        // throw new UnsupportedOperationException("Método não implementado 'chmod'");

        // Verifica se o caminho existe (permissoes contem o caminho)
        if (!permissoes.containsKey(caminho)) {
            throw new CaminhoNaoEncontradoException("Caminho não encontrado: " + caminho);
        }

        // Verfica se o usuário que está tentando alterar é root ou tem permissão rwx
        if (!ROOT_USER.equals(usuario)) {

            Map<String, String> permissoesDoCaminho = permissoes.get(caminho);
            String permUsuario = permissoesDoCaminho.get(usuario);

            if (permUsuario == null || ! permUsuario.startsWith("rw")) {
                throw new PermissaoException("Usuário " + usuario + " não tem permissão para alterar permissões em " + caminho);
            }
        }

        // Verificar se o usuarioAlvo existe
        if (!usuarioExiste(usuarioAlvo)) {
            throw new PermissaoException("Usuário alvo não existe: " + usuarioAlvo);
        }

        // Atualiza a permissão para o usuarioAlvo no caminho
        Map<String, String> permissoesDoCaminho = permissoes.get(caminho);
        permissoesDoCaminho.put(usuarioAlvo, permissao);

        // imprimir a mudança de permissão
        System.out.println("Permissão para " + usuarioAlvo + " no caminho " + caminho + " alterada para " + permissao);

    }

    @Override
    public void rm(String caminho, String usuario, boolean recursivo)
            throws CaminhoNaoEncontradoException, PermissaoException {
        throw new UnsupportedOperationException("Método não implementado 'rm'");
    }

    @Override
    public void touch(String caminho, String usuario) throws CaminhoJaExistenteException, PermissaoException {
        throw new UnsupportedOperationException("Método não implementado 'touch'");
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

    // Função para adicionar meu usuário
    private final Set<String> usuarios = new HashSet<>();

    public void addUser(String user) {
        
        if (user == null || user.isEmpty()) {
            throw new IllegalArgumentException("Usuário inválido");
        }
        usuarios.add(user);
    }

    public boolean usuarioExiste(String user) {
        return usuarios.contains(user);
    }

}
