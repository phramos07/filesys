package filesys;

import java.util.ArrayList;
import java.util.List;

import exception.CaminhoJaExistenteException;
import exception.CaminhoNaoEncontradoException;
import exception.PermissaoException;

// Implemente nesta classe o seu código do FileSystem.
// A classe pode ser alterada.
// O construtor, argumentos do construtor podem ser modificados 
// e atributos & métodos privados podem ser adicionados
public final class FileSystemImpl implements IFileSystem {
    private static final String ROOT_USER = "root";
    private List<Usuario> usuarios = new ArrayList<>();
    private Diretorio raiz;

    public FileSystemImpl() {
        raiz = new Diretorio("/", "rwx", ROOT_USER);
        adicionarUsuario(new Usuario(ROOT_USER, "rwx", "/"));
    }

    private void adicionarUsuario(Usuario user){
        for(Usuario usuario : usuarios){
            if(usuario.getNome().trim().equals(user.getNome()))
                throw new IllegalArgumentException("Não é possível existir dois usuários com mesmo nome");
        }
        usuarios.add(user);
    }

    private MetaDados navegar(String caminho) throws CaminhoNaoEncontradoException {
        if (caminho.equals("/")) return raiz;
        String[] partes = caminho.split("/");
        Diretorio temp = raiz;
        for (String parte : java.util.Arrays.stream(partes).filter(p -> p != null && !p.isEmpty()).toArray(String[]::new)) {
            if (!temp.getFilhos().containsKey(parte)) {
                throw new CaminhoNaoEncontradoException("Não foi possível encontrar o caminho: " + caminho);
            }
            temp = temp.getFilhos().get(parte);
        }
        return temp;
    }


    @Override
    public void mkdir(String caminho, String nome) throws CaminhoJaExistenteException, PermissaoException {
        throw new UnsupportedOperationException("Método não implementado 'mkdir'");
    }

    /*
     * Validar a permissão de acordo com as possíveis formas de permissão 'r w x' ou 'n',
     * caso seja nulo, vazio ou não tenha nenhuma dessas letras, lança exceção.
     * Ao ser validado, chama o método navegar
     */
    @Override
    public void chmod(String caminho, String usuario, String usuarioAlvo, String permissao)
            throws CaminhoNaoEncontradoException, PermissaoException {

        validarPermissao(permissao);

        MetaDados objAlvo = navegar(caminho);
        String dono = objAlvo.getDono();

        if (!usuario.equals("root") && !usuario.equals(dono)) {
            throw new PermissaoException(
                    "Usuário '" + usuario + "' não tem permissão para alterar direitos em: " + caminho);
        }

        objAlvo.alterarPermissao(usuarioAlvo, permissao);
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

    private void validarPermissao(String permissao) {
        if (permissao == null || permissao.length() != 3)
            throw new IllegalArgumentException("Permissão inválida");
        for (char c : permissao.toCharArray()) {
            if ("rwxn".indexOf(c) == -1)
                throw new IllegalArgumentException("Permissão inválida");
        }
    }

}
