package filesys;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import exception.CaminhoJaExistenteException;
import exception.CaminhoNaoEncontradoException;
import exception.PermissaoException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

// Implemente nesta classe o seu código do FileSystem.
// A classe pode ser alterada.
// O construtor, argumentos do construtor podem ser modificados 
// e atributos & métodos privados podem ser adicionados
public final class FileSystemImpl implements IFileSystem {
    private static final String ROOT_USER = "root"; // pode ser necessário

    private List<Usuario> usuarios;
    private String user;
    private Map<String, List<String>> permissoesPorCaminho = new HashMap<>();

    public FileSystemImpl() {}

    public FileSystemImpl(List<Usuario> usuarios, String user) {
        if (usuarios == null) {
            throw new IllegalArgumentException("Lista de usuários não pode ser nula");
        }
        this.usuarios = usuarios;

        if (user == null) {
            throw new IllegalArgumentException("Usuário atual não recebido");
        }
        this.user = user;
    }

    @Override
    public void mkdir(String caminho, String nome) throws CaminhoJaExistenteException, PermissaoException {
        // throw new UnsupportedOperationException("Método não implementado 'mkdir'");
        if (caminho == null || nome == null) {
            throw new IllegalArgumentException("Caminho e nome não podem ser nulos");
        }
        if (caminho.isEmpty() || nome.isEmpty()) {
            throw new IllegalArgumentException("Caminho e nome não podem ser vazios");
        }

        // Aqui você deve implementar a lógica para criar um diretório
        // Verifique se o caminho existe e se o usuário tem permissão para criar o diretório
        // Se o diretório já existir, lance CaminhoJaExistenteException
        // Se o caminho não existir, lance CaminhoNaoEncontradoException
        // Se o usuário não tiver permissão, lance PermissaoException

        // OBS: o contrato para esta interface exige que caminhos intermediários sejam criados por padrão durante a chamada à mkdir. É como se a flag '-p' fosse passada por padrão na nossa interface:
        /*
            -p     Create intermediate directories as required.  If this option is not specified, 
                the full path prefix of each operand must already exist.  On the other hand, 
                with this option specified, no error will be reported if a directory given as 
                an operand already exists.  Intermediate directories are created with 
                permission bits of “rwxrwxrwx” (0777) as modified by the current umask, plus 
                write and search permission for the owner.
        */

        // Verifique se o diretório já existe
        if (diretorioExiste(caminho + nome)) {
            throw new CaminhoJaExistenteException("Diretório já existe: " + caminho + nome);
        }

        // Verificar se o usuário tem permissão para criar o diretório
        if (!usuarioPodeCriarDiretorio(caminho, nome)) {
            throw new PermissaoException("Usuário não tem permissão para criar diretório na raiz: " + nome);
        }

        if (caminho.equals("/")) {
            // Criar diretório na raiz
            criarDiretorioNaRaiz(nome);
        } else {
            // Verifique se o caminho existe
            if (!caminhoExiste(caminho)) {
                throw new IllegalStateException("Caminho não encontrado: " + caminho);
            }
            // Criar diretório no caminho especificado
            criarDiretorioNoCaminho(caminho, nome);
        }

    }

    private boolean caminhoExiste(String caminho) {
        Path path = Paths.get(caminho);
        return Files.exists(path);
    }

    private boolean diretorioExiste(String caminho) {
        Path path = Paths.get(caminho);
        return Files.isDirectory(path);
    }

    private void criarDiretorioNaRaiz(String nome) {
        Path path = Paths.get("/" + nome);
        try {
            Files.createDirectory(path);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao criar diretório na raiz: " + e.getMessage(), e);
        }
    }

    private void criarDiretorioNoCaminho(String caminho, String nome) {
        Path path = Paths.get(caminho, nome);
        try {
            Files.createDirectories(path); // Cria o diretório e quaisquer diretórios intermediários necessários
        } catch (Exception e) {
            throw new RuntimeException("Erro ao criar diretório no caminho: " + e.getMessage(), e);
        }
    }

    public boolean usuarioPodeCriarDiretorio(String caminho, String nome) {
        // Verifica se o usuário atual tem permissão para criar no diretório
        Usuario usuarioAtual = getUsuarioAtual(); // Método fictício para obter o usuário atual
        // Pega as permissões do usuário atual
        String permissoes = usuarioAtual.getPermissoes();

        // Verifica se o usuário é root ou se tem permissão de escrita
        if (ROOT_USER.equals(usuarioAtual.getNome()) || permissoes.contains("w")) {
            return true; // Usuário root ou tem permissão de escrita
        }

        return false; // Usuário não tem permissão
    }

    private Usuario getUsuarioAtual() {
        for (Usuario usuario : usuarios) {
            if (usuario.getNome().equals(user)) {
                return usuario;
            }
        }
        throw new IllegalArgumentException("Usuário não encontrado: " + user);
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

    public void addUser(String user) {
        throw new UnsupportedOperationException("Método não implementado 'addUser'");
    }
}
