package filesys;

import java.util.ArrayList;
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
    public void mkdir(String caminho, String usuario)
            throws CaminhoJaExistenteException, PermissaoException, CaminhoNaoEncontradoException {
        verificaUsuarioValido(usuario);

        if (caminho.equals("/"))
            return;

        Diretorio pai = navegarParaDiretorioPai(caminho);
        String nomeNovo = extrairNomeFinal(caminho);

        if (pai.buscarSubdiretorio(nomeNovo) != null) {
            throw new CaminhoJaExistenteException("O diretório '" + nomeNovo + "' já existe em '" + caminho + "'");
        }

        verificarPermissao(obterCaminhoPai(caminho), usuario, 'w');

        Diretorio novo = new Diretorio(nomeNovo, usuario);
        pai.adicionarSubdiretorio(novo);

        // Debug pra testes: Imprime os subdiretorios do diretorio atual
        System.out.print("Subdiretorios de '" + pai.getMetaDados().getNome() + "': ");
        for (Diretorio d : pai.getSubdiretorios()) {
            System.out.print(d.getMetaDados().getNome() + " ");
        }
        System.out.println();
    }

    @Override
    public void chmod(String caminho, String usuario, String usuarioAlvo, String permissao)
            throws CaminhoNaoEncontradoException, PermissaoException {
        verificaUsuarioValido(usuario);
        verificaUsuarioValido(usuarioAlvo);
        throw new UnsupportedOperationException("Método não implementado 'chmod'");
    }

    @Override
    public void rm(String caminho, String usuario, boolean recursivo)
            throws CaminhoNaoEncontradoException, PermissaoException {
        verificaUsuarioValido(usuario);

        if (caminho.equals("/")) {
            throw new PermissaoException("Não é permitido remover o diretório raiz.");
        }

        verificarPermissao(caminho, usuario, 'r');
        verificarPermissao(obterCaminhoPai(caminho), usuario, 'w');

        Diretorio pai = navegarParaDiretorioPai(caminho);
        String nome = extrairNomeFinal(caminho);

        Arquivo arq = pai.buscarArquivo(nome);
        if (arq != null) {
            pai.removerArquivo(nome);
            return;
        }

        Diretorio dir = pai.buscarSubdiretorio(nome);
        if (dir == null) {
            throw new CaminhoNaoEncontradoException("Caminho '" + caminho + "' não encontrado.");
        }

        if (!recursivo && (!dir.getArquivos().isEmpty() || !dir.getSubdiretorios().isEmpty())) {
            throw new PermissaoException("Diretório não vazio. Use o modo recursivo.");
        }

        if (recursivo) {
            removerDiretorioRecursivamente(dir);
        }

        pai.removerSubdiretorio(nome);
    }

    @Override
    public void touch(String caminho, String usuario)
            throws CaminhoJaExistenteException, CaminhoNaoEncontradoException, PermissaoException {
        verificaUsuarioValido(usuario);

        if (caminho.equals("/") || caminho.endsWith("/")) {
            throw new CaminhoNaoEncontradoException("Caminho inválido para arquivo: " + caminho);
        }

        Diretorio pai = navegarParaDiretorioPai(caminho);
        String nomeArquivo = extrairNomeFinal(caminho);

        if (pai.buscarArquivo(nomeArquivo) != null) {
            throw new CaminhoJaExistenteException("O arquivo '" + nomeArquivo + "' já existe.");
        }

        verificarPermissao(obterCaminhoPai(caminho), usuario, 'w');

        Arquivo novoArquivo = new Arquivo(nomeArquivo, usuario);
        pai.adicionarArquivo(novoArquivo);
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
            throws CaminhoNaoEncontradoException, PermissaoException, CaminhoJaExistenteException {
        verificaUsuarioValido(usuario);

        // Verifica se os caminhos são válidos
        if (caminhoAntigo.equals("/") || caminhoNovo.equals("/")) {
            throw new CaminhoNaoEncontradoException("Não é possível mover o diretório raiz.");
        }

        verificarPermissao(caminhoAntigo, usuario, 'r');
        verificarPermissao(obterCaminhoPai(caminhoNovo), usuario, 'w');
        verificarPermissao(obterCaminhoPai(caminhoAntigo), usuario, 'w');

        // Extrai nomes e diretórios pai
        Diretorio dirOrigemPai = navegarParaDiretorioPai(caminhoAntigo);
        Diretorio dirDestinoPai = navegarParaDiretorioPai(caminhoNovo);
        String nomeOrigem = extrairNomeFinal(caminhoAntigo);
        String nomeDestino = extrairNomeFinal(caminhoNovo);

        // Verifica se já existe algo no destino
        if (dirDestinoPai.buscarArquivo(nomeDestino) != null || dirDestinoPai.buscarSubdiretorio(nomeDestino) != null) {
            throw new CaminhoJaExistenteException("O destino '" + caminhoNovo + "' já existe.");
        }

        // Verifica se é arquivo
        Arquivo arquivo = dirOrigemPai.buscarArquivo(nomeOrigem);
        if (arquivo != null) {
            dirOrigemPai.removerArquivo(nomeOrigem);
            arquivo.getMetaDados().setNome(nomeDestino);
            dirDestinoPai.adicionarArquivo(arquivo);
            return;
        }

        // Verifica se é diretório
        Diretorio subdir = dirOrigemPai.buscarSubdiretorio(nomeOrigem);
        if (subdir != null) {
            dirOrigemPai.removerSubdiretorio(nomeOrigem);
            subdir.getMetaDados().setNome(nomeDestino);
            dirDestinoPai.adicionarSubdiretorio(subdir);
            return;
        }

        // Se não encontrou nada
        throw new CaminhoNaoEncontradoException("Caminho de origem '" + caminhoAntigo + "' não encontrado.");
    }

    @Override
    public void ls(String caminho, String usuario, boolean recursivo)
            throws CaminhoNaoEncontradoException, PermissaoException {
        verificaUsuarioValido(usuario);

        // Verifica se o caminho é válido
        if (caminho == null || caminho.isEmpty() || !caminho.startsWith("/")) {
            throw new CaminhoNaoEncontradoException("Caminho inválido: " + caminho);
        }

        Diretorio dir = caminho.equals("/") ? fileSys.getRaiz() : navegarParaDiretorioCompleto(caminho);

        verificarPermissao(caminho, usuario, 'r');
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

    // Navega para o diretorio pai do caminho passado como parametro
    // Ex: "/home/user/docs" retorna "/home/user"
    private Diretorio navegarParaDiretorioPai(String caminho) throws CaminhoNaoEncontradoException {
        String[] partes = caminho.split("/");
        if (!partes[0].isEmpty()) {
            throw new IllegalArgumentException("Caminho inválido: deve começar com '/'");
        }

        Diretorio atual = fileSys.getRaiz();
        for (int i = 1; i < partes.length - 1; i++) {
            if (partes[i].isEmpty())
                continue;
            Diretorio encontrado = atual.buscarSubdiretorio(partes[i]);
            if (encontrado == null) {
                throw new CaminhoNaoEncontradoException("Diretório '" + partes[i] + "' não encontrado.");
            }
            atual = encontrado;
        }
        return atual;
    }

    // Navega para o diretorio completo do caminho passado como parametro
    // Ex: "/home/user/docs" retorna o diretorio "docs"
    private Diretorio navegarParaDiretorioCompleto(String caminho) throws CaminhoNaoEncontradoException {
        String[] partes = caminho.split("/");
        if (!partes[0].isEmpty()) {
            throw new IllegalArgumentException("Caminho inválido: deve começar com '/'");
        }

        Diretorio atual = fileSys.getRaiz();
        for (int i = 1; i < partes.length; i++) {
            if (partes[i].isEmpty())
                continue;
            Diretorio encontrado = atual.buscarSubdiretorio(partes[i]);
            if (encontrado == null) {
                throw new CaminhoNaoEncontradoException("Diretório '" + partes[i] + "' não encontrado.");
            }
            atual = encontrado;
        }
        return atual;
    }

    // Extrai o nome final do caminho, que pode ser um arquivo ou diretório
    // Ex: "/home/user/docs" retorna "docs"
    private String extrairNomeFinal(String caminho) {
        String[] partes = caminho.split("/");
        return partes[partes.length - 1];
    }

    // Obtém o caminho pai do caminho fornecido
    // Ex: "/home/user/docs" retorna "/home/user"
    private String obterCaminhoPai(String caminho) {
        int ultimoSlash = caminho.lastIndexOf('/');
        if (ultimoSlash == 0)
            return "/";
        return caminho.substring(0, ultimoSlash);
    }

    private void verificaUsuarioValido(String usuario) throws PermissaoException {
        if (!usuarios.containsKey(usuario)) {
            throw new PermissaoException("Usuário '" + usuario + "' não encontrado.");
        }
    }

    private void verificarPermissao(String caminho, String usuario, char tipo) throws PermissaoException {
        if (!temPermissao(caminho, usuario, tipo)) {
            throw new PermissaoException(
                    "Usuário '" + usuario + "' não tem permissão '" + tipo + "' em '" + caminho + "'");
        }
    }

    private boolean temPermissao(String caminho, String usuario, char tipo) {
        if (usuario.equals(ROOT_USER))
            return true; // root sempre tem permissão

        Usuario u = usuarios.get(usuario);
        if (u == null)
            return false;

        String perm = u.getPermissaoParaCaminho(caminho);
        return perm.indexOf(tipo) != -1;
    }

    private void removerDiretorioRecursivamente(Diretorio dir) {
        // Remove subdiretórios recursivamente
        for (Diretorio sub : new ArrayList<>(dir.getSubdiretorios())) {
            removerDiretorioRecursivamente(sub);
            dir.removerSubdiretorio(sub.getMetaDados().getNome());
        }

        // Remove todos os arquivos
        for (Arquivo arq : new ArrayList<>(dir.getArquivos())) {
            dir.removerArquivo(arq.getMetaDados().getNome());
        }
    }

}
