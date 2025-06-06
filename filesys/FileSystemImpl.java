package filesys;

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
    private final FileSys fileSys;
    private final Map<String, Usuario> usuarios;

    public FileSystemImpl(Map<String, Usuario> usuarios) {
        this.fileSys = new FileSys(ROOT_USER);
        this.usuarios = usuarios;
    }

    @Override
    public void mkdir(String caminho, String usuario) throws CaminhoJaExistenteException, PermissaoException {
        if (!usuarios.containsKey(usuario)) {
            throw new PermissaoException("Usuário '" + usuario + "' não encontrado.");
        }

        if (caminho.equals("/")) {
            return;
        }

        String[] partes = caminho.split("/");
        Diretorio atual = fileSys.getRaiz();

        if (!partes[0].isEmpty()) {
            throw new IllegalArgumentException("Caminho inválido: deve começar com '/'");
        }

        for (int i = 1; i < partes.length - 1; i++) {
            String nome = partes[i];
            Diretorio encontrado = atual.buscarSubdiretorio(nome);
           
            atual = encontrado;
        }

        String nomeNovo = partes[partes.length - 1];

        if (atual.buscarSubdiretorio(nomeNovo) != null) {
            throw new CaminhoJaExistenteException("O diretório '" + nomeNovo + "' já existe.");
        }

        if (!temPermissao(atual, usuario, 'w')) {
            throw new PermissaoException("Usuário '" + usuario + "' não tem permissão de escrita em '"
                    + atual.getMetaDados().getNome() + "'");
        }

        Diretorio novo = new Diretorio(nomeNovo, usuario);
        atual.adicionarSubdiretorio(novo);

        // Debug pra testes: Imprime os subdiretorios do diretorio atual
        System.out.print("Subdiretorios de '" + atual.getMetaDados().getNome() + "': ");
        for (Diretorio d : atual.getSubdiretorios()) {
            System.out.print(d.getMetaDados().getNome() + " ");
        }
        System.out.println();
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
    public void ls(String caminho, String usuario, boolean recursivo)
            throws CaminhoNaoEncontradoException, PermissaoException {
        if (!usuarios.containsKey(usuario)) {
            throw new PermissaoException("Usuário '" + usuario + "' não encontrado.");
        }

        Diretorio dir = fileSys.getRaiz();
        if (!caminho.equals("/")) {
            String[] partes = caminho.split("/");
            if (!partes[0].isEmpty()) {
                throw new IllegalArgumentException("Caminho inválido: deve começar com '/'");
            }
            for (int i = 1; i < partes.length; i++) {
                String nome = partes[i];
                if (nome.isEmpty())
                    continue;
                Diretorio encontrado = dir.buscarSubdiretorio(nome);
                if (encontrado == null) {
                    throw new CaminhoNaoEncontradoException(
                            "Diretório '" + nome + "' não encontrado em '" + caminho + "'");
                }
                dir = encontrado;
            }
        }

        if (!temPermissao(dir, usuario, 'r')) {
            throw new PermissaoException("Usuário '" + usuario + "' não tem permissão de leitura em '" + caminho + "'");
        }

        lsDiretorio(dir, caminho.equals("/") ? "/" : dir.getMetaDados().getNome(), recursivo, "");
    }

    private void lsDiretorio(Diretorio dir, String nome, boolean recursivo, String prefixo) {
        System.out.println(prefixo + nome + ":");

        for (Arquivo arq : dir.getArquivos()) {
            System.out.println(prefixo + "  " + arq.getMetaDados().getNome());
        }

        for (Diretorio sub : dir.getSubdiretorios()) {
            System.out.println(prefixo + "  " + sub.getMetaDados().getNome() + "/");
        }
        // Se recursivo, entra nos subdiretorios
        if (recursivo) {
            for (Diretorio sub : dir.getSubdiretorios()) {
                lsDiretorio(sub, sub.getMetaDados().getNome(), true, prefixo + "  ");
            }
        }
    }

    @Override
    public void cp(String caminhoOrigem, String caminhoDestino, String usuario, boolean recursivo)
            throws CaminhoNaoEncontradoException, PermissaoException {
        throw new UnsupportedOperationException("Método não implementado 'cp'");
    }

    public void addUser(String user) {
        throw new UnsupportedOperationException("Método não implementado 'addUser'");
    }

    private boolean temPermissao(Diretorio dir, String usuario, char tipo) {
        if (usuario.equals("root"))
            return true; // root sempre tem permissao

        String perm = dir.getMetaDados().getPermissao(usuario);

        if (perm.equals("---")) {
            Usuario u = usuarios.get(usuario);
            if (u != null) {
                perm = u.getPermissao(); // rwx, rw-, r--
            }
        }

        return perm.indexOf(tipo) != -1;
    }

}
