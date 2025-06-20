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
    private Diretorio root;
    private List<String> users = new ArrayList<>();

    public FileSystemImpl() {
        this.root = new Diretorio(ROOT_USER, "/");
        this.users.add(ROOT_USER);
    }

    /**
     * Cria um novo diretório no caminho especificado.
     * 
     * @param caminho Caminho onde o diretório será criado
     * @param nome    Nome do novo diretório
     * @throws CaminhoJaExistenteException Se já existir um diretório com o mesmo
     *                                     nome
     * @throws PermissaoException          Se o usuário não tiver permissão para
     *                                     criar diretórios
     */
    @Override
    public void mkdir(String caminho, String usuario)
            throws CaminhoJaExistenteException, PermissaoException {

        if (caminho == null || caminho.isEmpty() || usuario == null || usuario.isEmpty()) {
            throw new IllegalArgumentException("Caminho e usuário não podem ser nulos ou vazios");
        }

        if (!users.contains(usuario)) {
            throw new PermissaoException("Usuário não existe: " + usuario);
        }

        if (caminho.equals("/")) {
            return;
        }

        try {
            String[] pathParts = splitPath(caminho);
            String parentPath = pathParts[0];
            String nome = pathParts[1];

            if (nome.isEmpty()) {
                throw new IllegalArgumentException("Nome do diretório não pode ser vazio");
            }

            Diretorio parent = navigateTo(parentPath);

            if (encontrarSubdiretorio(parent, nome) != null) {
                throw new CaminhoJaExistenteException("Diretório já existe: " + nome);
            }

            if (encontrarArquivo(parent, nome) != null) {
                throw new CaminhoJaExistenteException("Já existe um arquivo com este nome: " + nome);
            }

            verificarPermissaoEscrita(usuario, parent);

            Diretorio newDirectory = new Diretorio(usuario, nome);
            parent.addSubDiretorio(newDirectory);

        } catch (CaminhoNaoEncontradoException e) {
            throw new PermissaoException("Caminho não encontrado: " + caminho);
        }
    }

    /**
     * Altera as permissões de um arquivo ou diretório.
     * 
     * @param caminho     Caminho do arquivo ou diretório
     * @param usuario     Usuário que está alterando as permissões
     * @param usuarioAlvo Usuário alvo das permissões
     * @param permissao   Novas permissões
     * @throws CaminhoNaoEncontradoException Se o caminho não existir
     * @throws PermissaoException            Se o usuário não tiver permissão para
     *                                       alterar permissões
     */
    @Override
    public void chmod(String caminho, String usuario, String usuarioAlvo, String permissao)
            throws CaminhoNaoEncontradoException, PermissaoException {

        validarParametros(caminho, usuario, usuarioAlvo, permissao);

        verificarUsuario(usuario);
        verificarUsuario(usuarioAlvo);

        String[] pathParts = splitPath(caminho);
        String parentPath = pathParts[0];
        String itemName = pathParts[1];

        // Se o caminho for a raiz
        if (caminho.equals("/")) {
            if (!usuario.equals(ROOT_USER)) {
                throw new PermissaoException("Somente root pode alterar permissões da raiz.");
            }

            // Validar permissões
            for (char c : permissao.toCharArray()) {
                if (c != 'r' && c != 'w' && c != 'x' && c != '-') {
                    throw new IllegalArgumentException("Permissão inválida: " + c + ". Use apenas r, w, x ou -");
                }
            }

            // Configurar permissões para a raiz
            root.getMetadata().getPermissions().put(usuarioAlvo, permissao.replace("-", ""));
            return;
        }

        Diretorio parent = navigateTo(parentPath);

        Arquivo arquivo = encontrarArquivo(parent, itemName);
        if (arquivo != null) {
            if (!usuario.equals(ROOT_USER) && !usuario.equals(arquivo.getMetadata().getOwner())) {
                throw new PermissaoException("Somente root ou dono pode alterar permissões.");
            }

            for (char c : permissao.toCharArray()) {
                if (c != 'r' && c != 'w' && c != 'x' && c != '-') {
                    throw new IllegalArgumentException("Permissão inválida: " + c + ". Use apenas r, w, x ou -");
                }
            }

            arquivo.getMetadata().getPermissions().put(usuarioAlvo, permissao.replace("-", ""));
            return;
        }

        Diretorio dir = encontrarSubdiretorio(parent, itemName);
        if (dir != null) {
            if (!usuario.equals(ROOT_USER) && !usuario.equals(dir.getMetadata().getOwner())) {
                throw new PermissaoException("Somente root ou dono pode alterar permissões.");
            }

            for (char c : permissao.toCharArray()) {
                if (c != 'r' && c != 'w' && c != 'x' && c != '-') {
                    throw new IllegalArgumentException("Permissão inválida: " + c + ". Use apenas r, w, x ou -");
                }
            }

            dir.getMetadata().getPermissions().put(usuarioAlvo, permissao.replace("-", ""));
            return;
        }

        throw new CaminhoNaoEncontradoException("Item não encontrado: " + caminho);
    }

    /**
     * Remove um arquivo ou diretório.
     * 
     * @param caminho   Caminho do arquivo ou diretório a ser removido
     * @param usuario   Usuário que está realizando a operação
     * @param recursivo Se true, remove diretórios não vazios recursivamente
     * @throws CaminhoNaoEncontradoException Se o caminho não existir
     * @throws PermissaoException            Se o usuário não tiver permissão para
     *                                       remover
     */
    @Override
    public void rm(String caminho, String usuario, boolean recursivo)
            throws CaminhoNaoEncontradoException, PermissaoException {

        validarParametros(caminho, usuario);

        verificarUsuario(usuario);

        if (caminho.equals("/")) {
            throw new PermissaoException("Não é permitido remover o diretório raiz.");
        }

        String[] pathParts = splitPath(caminho);
        String parentPath = pathParts[0];
        String nome = pathParts[1];

        Diretorio parent = navigateTo(parentPath);

        verificarPermissaoEscrita(usuario, parent);

        Arquivo arquivo = encontrarArquivo(parent, nome);
        if (arquivo != null) {
            if (!usuario.equals(ROOT_USER) &&
                    !usuario.equals(arquivo.getMetadata().getOwner()) &&
                    !temPermissao(usuario, arquivo.getMetadata(), 'w')) {
                throw new PermissaoException("Sem permissão para remover o arquivo: " + nome);
            }

            parent.getArquivos().remove(arquivo);
            return;
        }

        Diretorio subDir = encontrarSubdiretorio(parent, nome);
        if (subDir != null) {
            if (!usuario.equals(ROOT_USER) &&
                    !usuario.equals(subDir.getMetadata().getOwner()) &&
                    !temPermissao(usuario, subDir.getMetadata(), 'w')) {
                throw new PermissaoException("Sem permissão para remover o diretório: " + nome);
            }

            if (!recursivo && (!subDir.getArquivos().isEmpty() || !subDir.getSubDiretorios().isEmpty())) {
                throw new PermissaoException("Diretório não está vazio. Use rm recursivo.");
            }

            parent.getSubDiretorios().remove(subDir);
            return;
        }

        throw new CaminhoNaoEncontradoException("Item não encontrado: " + nome);

    }

    /**
     * Cria um novo arquivo vazio.
     * 
     * @param caminho Caminho completo do arquivo
     * @param usuario Usuário que está criando o arquivo
     * @throws CaminhoJaExistenteException Se já existir um arquivo com o mesmo nome
     * @throws PermissaoException          Se o usuário não tiver permissão para
     *                                     criar arquivos
     */
    @Override
    public void touch(String caminho, String usuario) throws CaminhoJaExistenteException, PermissaoException {
        validarParametros(caminho, usuario);

        verificarUsuario(usuario);

        String[] pathParts = splitPath(caminho);
        String parentPath = pathParts[0];
        String fileName = pathParts[1];

        try {
            Diretorio parent = navigateTo(parentPath);

            if (encontrarArquivo(parent, fileName) != null) {
                throw new CaminhoJaExistenteException("Arquivo já existe: " + fileName);
            }

            if (encontrarSubdiretorio(parent, fileName) != null) {
                throw new CaminhoJaExistenteException("Já existe um diretório com este nome: " + fileName);
            }

            verificarPermissaoEscrita(usuario, parent);

            Arquivo novo = new Arquivo(fileName, usuario);
            parent.addFile(novo);

        } catch (CaminhoNaoEncontradoException e) {
            throw new PermissaoException("Caminho não encontrado: " + parentPath);
        }

    }

    /**
     * Escreve dados em um arquivo.
     * 
     * @param caminho Caminho do arquivo
     * @param usuario Usuário que está escrevendo
     * @param anexar  Se true, anexa os dados ao final do arquivo; se false,
     *                sobrescreve
     * @param buffer  Dados a serem escritos
     * @throws CaminhoNaoEncontradoException Se o arquivo não existir
     * @throws PermissaoException            Se o usuário não tiver permissão para
     *                                       escrever
     */
    @Override
    public void write(String caminho, String usuario, boolean anexar, byte[] buffer)
            throws CaminhoNaoEncontradoException, PermissaoException {
        validarParametros(caminho, usuario, buffer);

        verificarUsuario(usuario);

        String[] pathParts = splitPath(caminho);
        String parentPath = pathParts[0];
        String fileName = pathParts[1];

        Diretorio dir = navigateTo(parentPath);
        Arquivo arquivo = encontrarArquivo(dir, fileName);

        if (arquivo == null) {
            throw new CaminhoNaoEncontradoException("Arquivo não encontrado: " + fileName);
        }

        if (!usuario.equals(ROOT_USER) &&
                !usuario.equals(arquivo.getMetadata().getOwner()) &&
                !temPermissao(usuario, arquivo.getMetadata(), 'w')) {
            throw new PermissaoException("Sem permissão de escrita no arquivo: " + fileName);
        }

        if (!anexar) {
            arquivo.getBlocos().clear();
            arquivo.getMetadata().setSize(0);
        }

        Bloco bloco = new Bloco(buffer);
        arquivo.addBloco(bloco);
    }

    /**
     * Lê dados de um arquivo.
     * 
     * @param caminho Caminho do arquivo
     * @param usuario Usuário que está lendo
     * @param buffer  Buffer onde os dados serão armazenados
     * @throws CaminhoNaoEncontradoException Se o arquivo não existir
     * @throws PermissaoException            Se o usuário não tiver permissão para
     *                                       ler
     */
    @Override
    public void read(String caminho, String usuario, byte[] buffer)
            throws CaminhoNaoEncontradoException, PermissaoException {
        validarParametros(caminho, usuario, buffer);

        verificarUsuario(usuario);

        String[] pathParts = splitPath(caminho);
        String parentPath = pathParts[0];
        String fileName = pathParts[1];

        Diretorio dir = navigateTo(parentPath);
        Arquivo arquivo = encontrarArquivo(dir, fileName);

        if (arquivo == null) {
            throw new CaminhoNaoEncontradoException("Arquivo não encontrado: " + fileName);
        }

        if (!usuario.equals(ROOT_USER) &&
                !usuario.equals(arquivo.getMetadata().getOwner()) &&
                !temPermissao(usuario, arquivo.getMetadata(), 'r')) {
            throw new PermissaoException("Sem permissão de leitura no arquivo: " + fileName);
        }

        byte[] data = arquivo.read();
        System.arraycopy(data, 0, buffer, 0, Math.min(data.length, buffer.length));

    }

    /**
     * Move um arquivo ou diretório para outro local.
     * 
     * @param caminhoAntigo Caminho original
     * @param caminhoNovo   Novo caminho
     * @param usuario       Usuário que está realizando a operação
     * @throws CaminhoNaoEncontradoException Se o caminho original não existir
     * @throws PermissaoException            Se o usuário não tiver permissão
     */
    @Override
    public void mv(String caminhoAntigo, String caminhoNovo, String usuario)
            throws CaminhoNaoEncontradoException, PermissaoException {
        validarParametros(caminhoAntigo, caminhoNovo, usuario);

        verificarUsuario(usuario);

        if (caminhoAntigo.equals("/")) {
            throw new PermissaoException("Não é permitido mover o diretório raiz.");
        }

        String[] sourcePathParts = splitPath(caminhoAntigo);
        String sourceParentPath = sourcePathParts[0];
        String sourceName = sourcePathParts[1];

        String[] destPathParts = splitPath(caminhoNovo);
        String destParentPath = destPathParts[0];
        String destName = destPathParts[1];

        Diretorio sourceParent = navigateTo(sourceParentPath);
        Diretorio destParent = navigateTo(destParentPath);

        verificarPermissaoEscrita(usuario, sourceParent);
        verificarPermissaoEscrita(usuario, destParent);

        Arquivo arquivoExistente = encontrarArquivo(destParent, destName);
        if (arquivoExistente != null) {
            destParent.getArquivos().remove(arquivoExistente);
        }

        Diretorio dirExistente = encontrarSubdiretorio(destParent, destName);
        if (dirExistente != null) {
            destParent.getSubDiretorios().remove(dirExistente);
        }

        Arquivo arquivo = encontrarArquivo(sourceParent, sourceName);
        if (arquivo != null) {
            sourceParent.getArquivos().remove(arquivo);
            arquivo.getMetadata().setName(destName);
            destParent.addFile(arquivo);
            return;
        }

        Diretorio subDir = encontrarSubdiretorio(sourceParent, sourceName);
        if (subDir != null) {
            sourceParent.getSubDiretorios().remove(subDir);
            subDir.getMetadata().setName(destName);
            destParent.addSubDiretorio(subDir);
            return;
        }

        throw new CaminhoNaoEncontradoException("Item não encontrado no caminho: " + caminhoAntigo);

    }

    /**
     * Lista o conteúdo de um diretório.
     * 
     * @param caminho   Caminho do diretório
     * @param usuario   Usuário que está listando
     * @param recursivo Se true, lista recursivamente os subdiretórios
     * @throws CaminhoNaoEncontradoException Se o diretório não existir
     * @throws PermissaoException            Se o usuário não tiver permissão para
     *                                       listar
     */
    @Override
    public void ls(String caminho, String usuario, boolean recursivo)
            throws CaminhoNaoEncontradoException, PermissaoException {
        validarParametros(caminho, usuario);

        verificarUsuario(usuario);

        Diretorio dir = navigateTo(caminho);

        if (!usuario.equals(ROOT_USER) &&
                !usuario.equals(dir.getMetadata().getOwner()) &&
                !temPermissao(usuario, dir.getMetadata(), 'r')) {
            throw new PermissaoException("Sem permissão de leitura no diretório: " + caminho);
        }

        listarConteudo(dir, caminho, recursivo, 0);
    }

    /**
     * Copia um arquivo ou diretório.
     * 
     * @param caminhoOrigem  Caminho do arquivo ou diretório de origem
     * @param caminhoDestino Caminho de destino
     * @param usuario        Usuário que está copiando
     * @param recursivo      Se true, copia diretórios recursivamente
     * @throws CaminhoNaoEncontradoException Se o caminho de origem não existir
     * @throws PermissaoException            Se o usuário não tiver permissão
     */
    @Override
    public void cp(String caminhoOrigem, String caminhoDestino, String usuario, boolean recursivo)
            throws CaminhoNaoEncontradoException, PermissaoException {
        validarParametros(caminhoOrigem, caminhoDestino, usuario);

        verificarUsuario(usuario);

        String[] sourcePathParts = splitPath(caminhoOrigem);
        String sourceParentPath = sourcePathParts[0];
        String sourceName = sourcePathParts[1];

        String[] destPathParts = splitPath(caminhoDestino);
        String destParentPath = destPathParts[0];
        String destName = destPathParts[1];

        Diretorio sourceParent = navigateTo(sourceParentPath);
        Diretorio destParent = navigateTo(destParentPath);

        if (!usuario.equals(ROOT_USER) && !temPermissao(usuario, sourceParent, 'r')) {
            throw new PermissaoException("Sem permissão para ler do caminho: " + sourceParentPath);
        }

        if (!usuario.equals(ROOT_USER) && !temPermissao(usuario, destParent, 'w')) {
            throw new PermissaoException("Sem permissão para escrever no caminho: " + destParentPath);
        }

        if (encontrarArquivo(destParent, destName) != null) {
            throw new PermissaoException("Já existe um arquivo com este nome no destino: " + destName);
        }

        if (encontrarSubdiretorio(destParent, destName) != null) {
            throw new PermissaoException("Já existe um diretório com este nome no destino: " + destName);
        }

        Arquivo arquivo = encontrarArquivo(sourceParent, sourceName);
        if (arquivo != null) {
            Arquivo novoArquivo = new Arquivo(destName, usuario);
            novoArquivo.setBlocos(new ArrayList(arquivo.getBlocos()));
            destParent.addFile(novoArquivo);
            return;
        }

        Diretorio subDir = encontrarSubdiretorio(sourceParent, sourceName);
        if (subDir != null) {
            if (!recursivo) {
                throw new PermissaoException("Cópia de diretório requer o modo recursivo.");
            }
            Diretorio novoDiretorio = copyDiretorio(subDir, destName, usuario);
            destParent.addSubDiretorio(novoDiretorio);
            return;
        }

        throw new CaminhoNaoEncontradoException("Item não encontrado no caminho: " + caminhoOrigem);

    }

    /**
     * Adiciona um novo usuário ao sistema.
     * 
     * @param user Nome do usuário a ser adicionado
     * @throws UnsupportedOperationException Se o usuário já existir
     */
    public void addUser(String user) {
        if (users.contains(user)) {
            throw new UnsupportedOperationException("Usuário já existe: " + user);
        }
        users.add(user);
    }

    /**
     * Navega até um diretório especificado pelo caminho.
     * 
     * @param path Caminho do diretório
     * @return O diretório encontrado
     * @throws CaminhoNaoEncontradoException Se o diretório não existir
     */
    private Diretorio navigateTo(String path) throws CaminhoNaoEncontradoException {

        if (path.equals("/"))
            return root;

        String[] parts = path.split("/");
        Diretorio current = root;

        for (String part : parts) {
            if (part.isEmpty())
                continue;

            boolean found = false;
            for (Diretorio sub : current.getSubDiretorios()) {
                if (sub.getMetadata().getName().equals(part)) {
                    current = sub;
                    found = true;
                    break;
                }
            }

            if (!found) {
                throw new CaminhoNaoEncontradoException("Diretório não encontrado: " + part);
            }
        }

        return current;
    }

    /**
     * Lista o conteúdo de um diretório.
     * 
     * @param dir       Diretório a ser listado
     * @param caminho   Caminho do diretório
     * @param recursivo Se true, lista recursivamente os subdiretórios
     * @param nivel     Nível de indentação para a saída
     */
    private void listarConteudo(Diretorio dir, String caminho, boolean recursivo, int nivel) {
        String indent = "  ".repeat(nivel);
        System.out.println(indent + "[DIR] " + dir.getMetadata().getName());

        for (Arquivo arquivo : dir.getArquivos()) {
            System.out.println(indent + "  [FILE] " + arquivo.getMetadata().getName());
        }

        if (recursivo) {
            for (Diretorio sub : dir.getSubDiretorios()) {
                listarConteudo(sub, caminho + "/" + sub.getMetadata().getName(), true, nivel + 1);
            }
        }
    }

    /**
     * Copia um diretório recursivamente.
     * 
     * @param source  Diretório de origem
     * @param newName Novo nome para o diretório
     * @param usuario Usuário que está copiando
     * @return O novo diretório copiado
     */
    private Diretorio copyDiretorio(Diretorio source, String newName, String usuario) {
        Diretorio novoDiretorio = new Diretorio(usuario, newName);

        for (Arquivo arquivo : source.getArquivos()) {
            Arquivo novoArquivo = new Arquivo(arquivo.getMetadata().getName(), usuario);
            novoArquivo.setBlocos(new ArrayList<>(arquivo.getBlocos()));
            novoDiretorio.addFile(novoArquivo);
        }

        for (Diretorio subDir : source.getSubDiretorios()) {
            Diretorio novoSubDir = copyDiretorio(subDir, subDir.getMetadata().getName(), usuario);
            novoDiretorio.addSubDiretorio(novoSubDir);
        }

        return novoDiretorio;
    }

    /**
     * Verifica se um usuário existe.
     * 
     * @param usuario Nome do usuário
     * @throws PermissaoException Se o usuário não existir
     */
    private void verificarUsuario(String usuario) throws PermissaoException {
        if (!users.contains(usuario)) {
            throw new PermissaoException("Usuário não existe: " + usuario);
        }
    }

    /**
     * Valida os parâmetros para evitar NullPointerException.
     * 
     * @param params Parâmetros a serem validados
     * @throws IllegalArgumentException Se algum parâmetro for nulo
     */
    private void validarParametros(Object... params) {
        for (int i = 0; i < params.length; i++) {
            if (params[i] == null) {
                throw new IllegalArgumentException("Parâmetro " + (i + 1) + " não pode ser nulo");
            }
            if (params[i] instanceof String && ((String) params[i]).trim().isEmpty()) {
                throw new IllegalArgumentException("Parâmetro " + (i + 1) + " não pode ser vazio");
            }
        }
    }

    /**
     * Divide um caminho em diretório pai e nome do item.
     * 
     * @param caminho Caminho completo
     * @return Array com [diretório pai, nome do item]
     */
    private String[] splitPath(String caminho) {
        if (caminho == null || caminho.isEmpty()) {
            return new String[] { "/", "" };
        }

        int lastSlashIndex = caminho.lastIndexOf('/');
        if (lastSlashIndex == -1) {
            return new String[] { "/", caminho };
        }

        String parentPath = caminho.substring(0, lastSlashIndex);
        String name = caminho.substring(lastSlashIndex + 1);

        if (parentPath.isEmpty()) {
            parentPath = "/";
        }

        return new String[] { parentPath, name };
    }

    /**
     * Encontra um arquivo em um diretório pelo nome.
     * 
     * @param dir      Diretório onde procurar
     * @param fileName Nome do arquivo
     * @return O arquivo encontrado ou null se não existir
     */
    private Arquivo encontrarArquivo(Diretorio dir, String fileName) {
        for (Arquivo arquivo : dir.getArquivos()) {
            if (arquivo.getMetadata().getName().equals(fileName)) {
                return arquivo;
            }
        }
        return null;
    }

    /**
     * Encontra um subdiretório em um diretório pelo nome.
     * 
     * @param dir     Diretório onde procurar
     * @param dirName Nome do subdiretório
     * @return O subdiretório encontrado ou null se não existir
     */
    private Diretorio encontrarSubdiretorio(Diretorio dir, String dirName) {
        for (Diretorio subDir : dir.getSubDiretorios()) {
            if (subDir.getMetadata().getName().equals(dirName)) {
                return subDir;
            }
        }
        return null;
    }

    /**
     * Verifica se o usuário tem a permissão especificada em um diretório.
     * 
     * @param usuario   Nome do usuário
     * @param diretorio Diretório a ser verificado
     * @param permissao Caractere de permissão ('r', 'w' ou 'x')
     * @return true se o usuário tem a permissão, false caso contrário
     */
    private boolean temPermissao(String usuario, Diretorio diretorio, char permissao) {
        return temPermissao(usuario, diretorio.getMetadata(), permissao);
    }

    /**
     * Verifica se o usuário tem a permissão especificada.
     * 
     * @param usuario   Nome do usuário
     * @param metadata  Metadados do arquivo ou diretório
     * @param permissao Caractere de permissão ('r', 'w' ou 'x')
     * @return true se o usuário tem a permissão, false caso contrário
     */
    private boolean temPermissao(String usuario, Metadata metadata, char permissao) {
        if (usuario.equals(ROOT_USER)) {
            return true;
        }

        if (usuario.equals(metadata.getOwner())) {
            return true;
        }

        String perms = metadata.getPermissions().getOrDefault(usuario, "");
        return perms.contains(String.valueOf(permissao));
    }

    /**
     * Verifica se o usuário tem permissão para escrever no diretório.
     * 
     * @param usuario Nome do usuário
     * @param dir     Diretório a ser verificado
     * @throws PermissaoException Se o usuário não tiver permissão de escrita
     */
    private void verificarPermissaoEscrita(String usuario, Diretorio dir) throws PermissaoException {
        if (!temPermissao(usuario, dir.getMetadata(), 'w')) {
            throw new PermissaoException("Usuário " + usuario + " não tem permissão de escrita em " +
                    dir.getMetadata().getName());
        }
    }

    /**
     * Verifica se o usuário tem permissão para ler o diretório.
     * 
     * @param usuario Nome do usuário
     * @param dir     Diretório a ser verificado
     * @throws PermissaoException Se o usuário não tiver permissão de leitura
     */
    private void verificarPermissaoLeitura(String usuario, Diretorio dir) throws PermissaoException {
        if (!temPermissao(usuario, dir.getMetadata(), 'r')) {
            throw new PermissaoException("Usuário " + usuario + " não tem permissão de leitura em " +
                    dir.getMetadata().getName());
        }
    }
}
