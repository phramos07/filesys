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

        if (caminho == null || caminho.isEmpty() || usuario == null || usuario.isEmpty() ||
                usuarioAlvo == null || usuarioAlvo.isEmpty() || permissao == null || permissao.isEmpty()) {
            throw new IllegalArgumentException("Parâmetros inválidos");
        }

        if (!users.contains(usuario)) {
            throw new PermissaoException("Usuário não existe: " + usuario);
        }

        if (!users.contains(usuarioAlvo)) {
            users.add(usuarioAlvo);
        }

        if (permissao.length() != 3 && permissao.length() != 3) {
            throw new IllegalArgumentException("Formato de permissão inválido: " + permissao);
        }

        if (caminho.equals("/")) {
            if (!usuario.equals(ROOT_USER)) {
                throw new PermissaoException("Somente root pode alterar permissões da raiz.");
            }

            String cleanPermission = "";
            for (int i = 0; i < permissao.length(); i++) {
                char c = permissao.charAt(i);
                if (c != '-') {
                    cleanPermission += c;
                }
            }
            root.getMetadata().getPermissions().put(usuarioAlvo, cleanPermission);
            return;
        }

        try {
            String[] pathParts = splitPath(caminho);
            String parentPath = pathParts[0];
            String itemName = pathParts[1];

            Diretorio parent = navigateTo(parentPath);

            Arquivo arquivo = encontrarArquivo(parent, itemName);
            if (arquivo != null) {
                if (!usuario.equals(ROOT_USER) && !usuario.equals(arquivo.getMetadata().getOwner())) {
                    throw new PermissaoException("Somente root ou dono pode alterar permissões.");
                }

                String cleanPermission = "";
                for (int i = 0; i < permissao.length(); i++) {
                    char c = permissao.charAt(i);
                    if (c != '-') {
                        cleanPermission += c;
                    }
                }
                arquivo.getMetadata().getPermissions().put(usuarioAlvo, cleanPermission);
                return;
            }

            Diretorio dir = encontrarSubdiretorio(parent, itemName);
            if (dir != null) {
                if (!usuario.equals(ROOT_USER) && !usuario.equals(dir.getMetadata().getOwner())) {
                    throw new PermissaoException("Somente root ou dono pode alterar permissões.");
                }

                String cleanPermission = "";
                for (int i = 0; i < permissao.length(); i++) {
                    char c = permissao.charAt(i);
                    if (c != '-') {
                        cleanPermission += c;
                    }
                }
                dir.getMetadata().getPermissions().put(usuarioAlvo, cleanPermission);
                return;
            }

            throw new CaminhoNaoEncontradoException("Item não encontrado: " + caminho);
        } catch (CaminhoNaoEncontradoException e) {
            throw e;
        }
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

        if (fileName.isEmpty()) {
            throw new IllegalArgumentException("Nome do arquivo não pode ser vazio");
        }

        if (!fileName.contains(".")) {
            throw new PermissaoException("O arquivo deve ter uma extensão (ex: .txt, .doc)");
        }

        String[] fileNameParts = fileName.split("\\.");
        if (fileNameParts.length < 2 || fileNameParts[fileNameParts.length - 1].isEmpty()) {
            throw new PermissaoException("A extensão do arquivo não pode ser vazia");
        }

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

        System.out.println("Dados escritos com sucesso no arquivo: " + fileName);
        if (anexar) {
            System.out.println("Conteúdo anexado ao final do arquivo");
        } else {
            System.out.println("Arquivo sobrescrito");
        }
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
        int length = Math.min(data.length, buffer.length);
        System.arraycopy(data, 0, buffer, 0, length);

        System.out.println("Conteúdo lido do arquivo " + fileName + ":");
        System.out.println(new String(buffer, 0, length));
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

        while (caminhoAntigo.length() > 1 && caminhoAntigo.endsWith("/")) {
            caminhoAntigo = caminhoAntigo.substring(0, caminhoAntigo.length() - 1);
        }
        while (caminhoNovo.length() > 1 && caminhoNovo.endsWith("/")) {
            caminhoNovo = caminhoNovo.substring(0, caminhoNovo.length() - 1);
        }

        String[] sourcePathParts = splitPath(caminhoAntigo);
        String sourceParentPath = sourcePathParts[0];
        String sourceName = sourcePathParts[1];

        Diretorio sourceParent = navigateTo(sourceParentPath);

        Arquivo sourceFile = encontrarArquivo(sourceParent, sourceName);
        Diretorio sourceDir = null;

        if (sourceFile == null) {
            sourceDir = encontrarSubdiretorio(sourceParent, sourceName);
            if (sourceDir == null) {
                throw new CaminhoNaoEncontradoException("Item não encontrado no caminho: " + caminhoAntigo);
            }
        }

        verificarPermissaoEscrita(usuario, sourceParent);

        try {
            Diretorio destDir = navigateTo(caminhoNovo);

            verificarPermissaoEscrita(usuario, destDir);

            if (sourceFile != null) {
                if (encontrarArquivo(destDir, sourceName) != null) {
                    throw new PermissaoException("Já existe um arquivo com este nome no destino: " + sourceName);
                }

                sourceParent.getArquivos().remove(sourceFile);
                destDir.addFile(sourceFile);
            } else {
                if (encontrarSubdiretorio(destDir, sourceName) != null) {
                    throw new PermissaoException("Já existe um diretório com este nome no destino: " + sourceName);
                }

                sourceParent.getSubDiretorios().remove(sourceDir);
                destDir.addSubDiretorio(sourceDir);
            }

            return;
        } catch (CaminhoNaoEncontradoException e) {
            String[] destPathParts = splitPath(caminhoNovo);
            String destParentPath = destPathParts[0];
            String destName = destPathParts[1];

            Diretorio destParent = navigateTo(destParentPath);
            verificarPermissaoEscrita(usuario, destParent);

            if (encontrarArquivo(destParent, destName) != null) {
                throw new PermissaoException("Já existe um arquivo com este nome no destino: " + destName);
            }

            if (encontrarSubdiretorio(destParent, destName) != null) {
                throw new PermissaoException("Já existe um diretório com este nome no destino: " + destName);
            }

            if (sourceFile != null) {
                sourceParent.getArquivos().remove(sourceFile);
                sourceFile.getMetadata().setName(destName);
                destParent.addFile(sourceFile);
            } else {
                sourceParent.getSubDiretorios().remove(sourceDir);
                sourceDir.getMetadata().setName(destName);
                destParent.addSubDiretorio(sourceDir);
            }
        }
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

        if (caminhoOrigem.equals(caminhoDestino)) {
            throw new PermissaoException("Origem e destino não podem ser iguais");
        }

        while (caminhoOrigem.length() > 1 && caminhoOrigem.endsWith("/")) {
            caminhoOrigem = caminhoOrigem.substring(0, caminhoOrigem.length() - 1);
        }
        while (caminhoDestino.length() > 1 && caminhoDestino.endsWith("/")) {
            caminhoDestino = caminhoDestino.substring(0, caminhoDestino.length() - 1);
        }

        String[] sourcePathParts = splitPath(caminhoOrigem);
        String sourceParentPath = sourcePathParts[0];
        String sourceName = sourcePathParts[1];

        Diretorio sourceParent = navigateTo(sourceParentPath);

        Arquivo sourceFile = encontrarArquivo(sourceParent, sourceName);
        Diretorio sourceDir = null;

        if (sourceFile == null) {
            sourceDir = encontrarSubdiretorio(sourceParent, sourceName);
            if (sourceDir == null) {
                throw new CaminhoNaoEncontradoException("Item não encontrado no caminho: " + caminhoOrigem);
            }

            if (!recursivo) {
                throw new PermissaoException("Cópia de diretório requer o modo recursivo.");
            }
        }

        if (sourceFile != null) {
            if (!usuario.equals(ROOT_USER) &&
                    !usuario.equals(sourceFile.getMetadata().getOwner()) &&
                    !temPermissao(usuario, sourceFile.getMetadata(), 'r')) {
                throw new PermissaoException("Sem permissão para ler o arquivo: " + sourceName);
            }
        } else {
            if (!usuario.equals(ROOT_USER) &&
                    !usuario.equals(sourceDir.getMetadata().getOwner()) &&
                    !temPermissao(usuario, sourceDir.getMetadata(), 'r')) {
                throw new PermissaoException("Sem permissão para ler o diretório: " + sourceName);
            }
        }

        Diretorio destDir = null;
        String destName = "";

        try {
            destDir = navigateTo(caminhoDestino);

            destName = sourceName;

        } catch (CaminhoNaoEncontradoException e) {
            String[] destPathParts = splitPath(caminhoDestino);
            String destParentPath = destPathParts[0];
            destName = destPathParts[1];

            try {
                destDir = navigateTo(destParentPath);
            } catch (CaminhoNaoEncontradoException ex) {
                throw new CaminhoNaoEncontradoException("Diretório de destino não encontrado: " + destParentPath);
            }
        }

        verificarPermissaoEscrita(usuario, destDir);

        if (encontrarArquivo(destDir, destName) != null) {
            throw new PermissaoException("Já existe um arquivo com este nome no destino: " + destName);
        }

        if (encontrarSubdiretorio(destDir, destName) != null) {
            throw new PermissaoException("Já existe um diretório com este nome no destino: " + destName);
        }

        if (sourceFile != null) {
            Arquivo novoArquivo = new Arquivo(destName, usuario);

            for (Bloco bloco : sourceFile.getBlocos()) {
                byte[] data = bloco.getDados(); 
                byte[] newData = new byte[data.length];
                System.arraycopy(data, 0, newData, 0, data.length);
                novoArquivo.addBloco(new Bloco(newData));
            }

            destDir.addFile(novoArquivo);
        } else {
            Diretorio novoDiretorio = copyDiretorio(sourceDir, destName, usuario);
            destDir.addSubDiretorio(novoDiretorio);
        }
    }

    /**
     * Adiciona um novo usuário ao sistema.
     * 
     * @param user Nome do usuário a ser adicionado
     * @throws IllegalArgumentException Se o nome do usuário for nulo ou vazio
     */

    @Override
    public void addUser(String user) {
        if (user == null || user.isEmpty()) {
            throw new IllegalArgumentException("Nome de usuário não pode ser nulo ou vazio");
        }

        if (!users.contains(user)) {
            users.add(user);
            System.out.println("Usuário adicionado: " + user);
        } else {
            System.out.println("Usuário já existe: " + user);
        }
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

        for (String user : source.getMetadata().getPermissions().keySet()) {
            novoDiretorio.getMetadata().getPermissions().put(
                    user, source.getMetadata().getPermissions().get(user));
        }

        for (Arquivo arquivo : source.getArquivos()) {
            if (!usuario.equals(ROOT_USER) &&
                    !usuario.equals(arquivo.getMetadata().getOwner()) &&
                    !temPermissao(usuario, arquivo.getMetadata(), 'r')) {
                continue;
            }

            Arquivo novoArquivo = new Arquivo(arquivo.getMetadata().getName(), usuario);

            for (Bloco bloco : arquivo.getBlocos()) {
                byte[] data = bloco.getDados();
                byte[] newData = new byte[data.length];
                System.arraycopy(data, 0, newData, 0, data.length);
                novoArquivo.addBloco(new Bloco(newData));
            }

            novoDiretorio.addFile(novoArquivo);
        }

        for (Diretorio subDir : source.getSubDiretorios()) {
            if (!usuario.equals(ROOT_USER) &&
                    !usuario.equals(subDir.getMetadata().getOwner()) &&
                    !temPermissao(usuario, subDir.getMetadata(), 'r')) {
                continue;
            }

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
