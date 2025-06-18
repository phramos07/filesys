package filesys;

import exception.CaminhoJaExistenteException;
import exception.CaminhoNaoEncontradoException;
import exception.PermissaoException;

import java.nio.charset.StandardCharsets; // Import para manipulação de bytes de string
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors; // Import para usar streams e coletar listas

/**
 * Implementa a interface IFileSystem, contendo a lógica central
 * para as operações do sistema de arquivos virtual.
 * Lida com a estrutura de diretórios e arquivos, e gerencia permissões
 * e exceções conforme especificado.
 */
public final class FileSystemImpl implements IFileSystem {
    // O diretório raiz do sistema de arquivos, ponto de partida para todas as operações.
    private Directory root;
    // O usuário atualmente "logado" no sistema, usado para verificações de permissão.
    private String currentUser;
    // Constante para o nome do usuário com privilégios de root.
    private static final String ROOT_USER = "root";

    /**
     * Construtor para inicializar o sistema de arquivos.
     * Cria o diretório raiz "/" e define "root" como o usuário inicial.
     */
    public FileSystemImpl() {
        // Inicializa o diretório raiz com o nome "/" e o dono "root".
        this.root = new Directory("/", ROOT_USER);
        // Define o usuário atual como "root" por padrão.
        this.currentUser = ROOT_USER;
        System.out.println("Sistema de arquivos inicializado. Usuário atual: " + currentUser);
    }

    /**
     * Método auxiliar para dividir um caminho em seus componentes (nomes de diretórios/arquivos).
     * Exemplo: "/home/user/file.txt" resulta em ["home", "user", "file.txt"].
     * Ignora barras vazias resultantes de barras duplas ou inicial/final.
     * @param path O caminho a ser dividido.
     * @return Uma lista de strings, cada uma representando um componente do caminho.
     */
    private List<String> getPathComponents(String path) {
        // Divide o caminho por barras e filtra strings vazias.
        return Arrays.stream(path.split("/"))
                     .filter(s -> !s.isEmpty())
                     .collect(Collectors.toList());
    }

    /**
     * Método auxiliar para resolver um caminho e retornar o caminho para o diretório pai e o nome do nó alvo.
     * Para o caminho "/", retorna {"", ""}.
     * @param path O caminho completo.
     * @return Um array de String onde [0] é o caminho para o diretório pai e [1] é o nome do nó alvo.
     */
    private String[] resolveParentAndName(String path) {
        // Se o caminho é a raiz, não há diretório pai nomeável.
        if ("/".equals(path)) {
            return new String[]{"", ""};
        }
        List<String> components = getPathComponents(path);
        if (components.isEmpty()) {
            return new String[]{"", ""};
        }
        // O último componente é o nome do nó alvo.
        String name = components.get(components.size() - 1);
        // Os componentes anteriores formam o caminho do diretório pai.
        List<String> parentComponents = components.subList(0, components.size() - 1);
        String parentPath = "/" + String.join("/", parentComponents);
        // Garante que o caminho pai seja "/" se for a raiz (e.g., para "/file.txt").
        if (parentPath.isEmpty()) {
            parentPath = "/";
        }
        return new String[]{parentPath, name};
    }

    /**
     * Encontra e retorna o nó (Directory ou File) no caminho especificado.
     * @param path O caminho para o nó.
     * @return O objeto Directory ou File no caminho, ou null se não for encontrado.
     */
    private Object getNodeAtPath(String path) {
        // Se o caminho é a raiz, retorna o objeto root.
        if ("/".equals(path) || path.isEmpty()) {
            return root;
        }

        List<String> components = getPathComponents(path);
        Directory currentDir = root;

        for (int i = 0; i < components.size(); i++) {
            String component = components.get(i);
            if (i == components.size() - 1) { // Último componente pode ser arquivo ou diretório
                // Verifica se é um subdiretório
                if (currentDir.containsSubdirectory(component)) {
                    return currentDir.getSubdirectory(component);
                }
                // Verifica se é um arquivo
                else if (currentDir.containsFile(component)) {
                    return currentDir.getFile(component);
                } else {
                    return null; // Não encontrado
                }
            } else { // Componente é um diretório intermediário
                // Navega para o próximo subdiretório
                if (currentDir.containsSubdirectory(component)) {
                    currentDir = currentDir.getSubdirectory(component);
                } else {
                    return null; // Caminho não existe
                }
            }
        }
        return null; // Deve ser a raiz ou um caminho inválido se chegar aqui sem retorno
    }

    /**
     * Encontra e retorna o diretório pai do caminho especificado.
     * @param path O caminho para o nó cujo pai se deseja encontrar.
     * @param currentUser O usuário que está tentando acessar.
     * @return O objeto Directory pai.
     * @throws PermissaoException Se o usuário não tem permissão de execução no diretório pai.
     * @throws CaminhoNaoEncontradoException Se o caminho pai não existe ou não é um diretório.
     */
    private Directory getParentDirectory(String path, String currentUser) throws CaminhoNaoEncontradoException, PermissaoException {
        // A raiz não tem diretório pai navegável.
        if ("/".equals(path) || path.isEmpty()) {
            return null; // Sinaliza que não há pai para a raiz
        }
        String[] parentAndName = resolveParentAndName(path);
        String parentPath = parentAndName[0];

        Object node = getNodeAtPath(parentPath);
        if (node instanceof Directory) {
            Directory parentDir = (Directory) node;
            // Verifica permissão de execução (navegação) no diretório pai.
            if (!parentDir.getMetaData().canExecute(currentUser)) {
                throw new PermissaoException("Permissão negada: não pode navegar em '" + parentPath + "' para o usuário '" + currentUser + "'.");
            }
            return parentDir;
        }
        // Se o caminho pai não é um diretório ou não existe.
        throw new CaminhoNaoEncontradoException("Caminho pai '" + parentPath + "' não existe ou não é um diretório.");
    }

    /**
     * Altera as permissões de um arquivo ou diretório.
     * @param caminho O caminho do arquivo/diretório.
     * @param usuario O usuário que está tentando acessar (executando o chmod).
     * @param usuarioAlvo O usuário para quem as permissões serão alteradas.
     * @param permissao A nova string de permissões (e.g., "rwx", "r--").
     * @throws CaminhoNaoEncontradoException Se o caminho não for encontrado.
     * @throws PermissaoException Se o usuário atual não tiver permissão para alterar as permissões.
     */
    @Override
    public void chmod(String caminho, String usuario, String usuarioAlvo, String permissao)
            throws CaminhoNaoEncontradoException, PermissaoException {
        if (caminho == null || caminho.isEmpty()) {
            throw new IllegalArgumentException("Caminho inválido para chmod.");
        }

        Object node = getNodeAtPath(caminho);
        if (node == null) {
            throw new CaminhoNaoEncontradoException("chmod: '" + caminho + "': Nenhum arquivo ou diretório encontrado.");
        }

        MetaData metaDataToChange;
        if (node instanceof File) {
            metaDataToChange = ((File) node).getMetaData();
        } else if (node instanceof Directory) {
            metaDataToChange = ((Directory) node).getMetaData();
        } else {
            // Este caso não deveria acontecer com Directory e File, mas para segurança.
            throw new CaminhoNaoEncontradoException("chmod: '" + caminho + "': Tipo de nó desconhecido.");
        }

        // A regra do PDF: "Apenas o usuario root ou que tenha permissão de rw do caminho podem alterar as permissões."
        // Isso significa que o 'usuario' (o que está executando o comando) deve ser o root OU o dono do item E ter permissão de escrita ('w') no item.
        if (!ROOT_USER.equals(usuario) && !metaDataToChange.getOwner().equals(usuario)) {
            throw new PermissaoException("Permissão negada: Somente o dono ou '" + ROOT_USER + "' pode alterar permissões de '" + caminho + "'.");
        }
        // Além de ser o dono ou root, o usuário que está fazendo o chmod também precisa ter permissão de escrita no próprio item.
        if (!metaDataToChange.canWrite(usuario)) {
             throw new PermissaoException("Permissão negada: O usuário '" + usuario + "' não tem permissão de escrita para alterar as permissões de '" + caminho + "'.");
        }

        // Permissões devem ser uma string de 3 caracteres (e.g., "rwx", "r-x", "---")
        if (permissao == null || !permissao.matches("[r-][w-][x-]")) {
            throw new IllegalArgumentException("Formato de permissão inválido. Use 'rwx', 'rw-', 'r-x', '---', etc.");
        }

        metaDataToChange.setPermission(usuarioAlvo, permissao);
        System.out.println("Permissões de '" + caminho + "' para o usuário '" + usuarioAlvo + "' alteradas para '" + permissao + "'.");
    }

    /**
     * Cria um novo diretório no caminho especificado.
     * Implementa o comportamento do `mkdir -p` por padrão, criando diretórios intermediários.
     * @param caminho O caminho completo para o novo diretório.
     * @param usuario O usuário que está tentando criar o diretório.
     * @throws CaminhoJaExistenteException Se o diretório (ou um arquivo) já existe no caminho final.
     * @throws PermissaoException Se o usuário não tiver permissão para criar o diretório.
     */
    @Override
    public void mkdir(String caminho, String usuario) throws CaminhoJaExistenteException, PermissaoException {
        if (caminho == null || caminho.isEmpty() || "/".equals(caminho)) {
            // Para a raiz, não há "criação" no sentido de mkdir, ela já existe.
            throw new CaminhoJaExistenteException("Caminho inválido ou raiz já existente para mkdir.");
        }

        List<String> components = getPathComponents(caminho);
        Directory currentDir = root; // Começa sempre da raiz

        for (int i = 0; i < components.size(); i++) {
            String component = components.get(i);
            
            // Permissão de escrita no diretório pai é verificada antes de criar um novo subdiretório.
            // Para a raiz, o root sempre pode escrever. Para outros diretórios, verifica a permissão do usuário.
            if (!currentDir.getMetaData().canWrite(usuario)) {
                throw new PermissaoException("Permissão negada: não pode criar diretório em '" + currentDir.getMetaData().getName() + "' para o usuário '" + usuario + "'.");
            }

            // Se o componente já existe como um arquivo, não pode criar um diretório com o mesmo nome.
            if (currentDir.containsFile(component)) {
                throw new CaminhoJaExistenteException("Não é possível criar diretório '" + component + "': já existe um arquivo com este nome.");
            }

            // Se o componente já existe como um diretório, navega para ele (comportamento de -p).
            if (currentDir.containsSubdirectory(component)) {
                currentDir = currentDir.getSubdirectory(component);
            } else {
                // Se não existe, cria o novo diretório (intermediário ou final).
                Directory newDir = new Directory(component, usuario);
                // Permissões padrão para diretórios intermediários (0777 modificado pelo umask, aqui simplificado):
                // O dono é o 'usuario', e 'other' recebe 'r-x' para navegação.
                newDir.getMetaData().setPermission(usuario, "rwx"); // Dono tem permissão total
                newDir.getMetaData().setPermission("other", "r-x"); // Outros podem listar e navegar
                currentDir.addSubdirectory(newDir);
                currentDir = newDir; // Atualiza o diretório atual para o recém-criado.
                System.out.println("Diretório '" + component + "' criado em '" + (i == 0 ? "/" : "/" + String.join("/", components.subList(0, i))) + "'.");
            }
        }
        System.out.println("Comando mkdir para '" + caminho + "' executado com sucesso.");
    }

    /**
     * Remove um arquivo ou diretório.
     * @param caminho O caminho do arquivo/diretório a ser removido.
     * @param recursivo Se verdadeiro, remove recursivamente diretórios.
     * @param usuario O usuário que está tentando remover.
     * @throws CaminhoNaoEncontradoException Se o caminho não for encontrado.
     * @throws PermissaoException Se o usuário não tiver permissão para remover.
     */
    @Override
    public void rm(String caminho, String usuario, boolean recursivo)
            throws CaminhoNaoEncontradoException, PermissaoException {
        if (caminho == null || caminho.isEmpty() || "/".equals(caminho)) {
            throw new PermissaoException("Não é possível remover a raiz ou caminho inválido para rm.");
        }

        String[] parentAndName = resolveParentAndName(caminho);
        String nodeName = parentAndName[1];
        Directory parentDir = getParentDirectory(caminho, usuario); // Este já verifica permissão de navegação no pai

        // O diretório pai deve existir.
        if (parentDir == null) {
            throw new CaminhoNaoEncontradoException("Caminho pai de '" + caminho + "' não existe.");
        }

        // Permissão para remover: precisa de permissão de escrita no diretório pai.
        if (!parentDir.getMetaData().canWrite(usuario)) {
            throw new PermissaoException("Permissão negada: não pode remover em '" + parentAndName[0] + "' para o usuário '" + usuario + "'.");
        }

        Object nodeToRemove = getNodeAtPath(caminho);

        if (nodeToRemove == null) {
            throw new CaminhoNaoEncontradoException("rm: '" + caminho + "': Nenhum arquivo ou diretório encontrado para remoção.");
        }

        if (nodeToRemove instanceof File) {
            // Para remover um arquivo, o usuário precisa ter permissão de escrita no diretório PAI.
            // A permissão no próprio arquivo é irrelevante para a remoção em si, apenas para o seu conteúdo.
            parentDir.removeFile(nodeName);
            System.out.println("Arquivo '" + caminho + "' removido com sucesso.");
        } else if (nodeToRemove instanceof Directory) {
            Directory dirToRemove = (Directory) nodeToRemove;

            // Se o diretório não estiver vazio e 'recursivo' for falso, lança exceção.
            if (!dirToRemove.isEmpty() && !recursivo) {
                throw new PermissaoException("Diretório '" + caminho + "' não está vazio. Use rm -r para remover recursivamente.");
            }
            // Não é necessário verificar permissão de escrita no próprio diretório para remover, apenas no pai.

            // Remove o subdiretório do seu diretório pai.
            parentDir.removeSubdirectory(nodeName);
            System.out.println("Diretório '" + caminho + "' e seu conteúdo (se houver) removidos com sucesso.");
        } else {
            throw new CaminhoNaoEncontradoException("rm: '" + caminho + "': Tipo de nó desconhecido para remoção.");
        }
    }

    /**
     * Cria um novo arquivo vazio.
     * Comportamento do touch: se o arquivo já existir, apenas atualiza o timestamp de modificação.
     * @param caminho O caminho completo para o novo arquivo.
     * @param usuario O usuário que está tentando criar o arquivo.
     * @throws CaminhoJaExistenteException Se já existe um diretório com o mesmo nome no caminho final.
     * @throws PermissaoException Se o usuário não tiver permissão para criar o arquivo.
     * @throws CaminhoNaoEncontradoException Se o diretório pai não existir.
     */
    @Override
    public void touch(String caminho, String usuario) throws CaminhoJaExistenteException, PermissaoException, CaminhoNaoEncontradoException {
        if (caminho == null || caminho.isEmpty() || "/".equals(caminho)) {
            throw new IllegalArgumentException("Caminho inválido para touch.");
        }

        String[] parentAndName = resolveParentAndName(caminho);
        String parentPath = parentAndName[0];
        String fileName = parentAndName[1];

        // Obtém o diretório pai. Lança exceções se não existir ou sem permissão de navegação.
        Directory parentDir = getParentDirectory(caminho, usuario);
        if (parentDir == null) { // Caso específico se o caminho for diretamente um arquivo na raiz (ex: "/novoArquivo")
             parentDir = root; // Define a raiz como diretório pai
             // Se o usuário não tem permissão de escrita na raiz, lança exceção.
             if (!parentDir.getMetaData().canWrite(usuario)) {
                throw new PermissaoException("Permissão negada: não pode criar arquivo na raiz para o usuário '" + usuario + "'.");
            }
        }

        // Verifica permissão de escrita no diretório pai para criar o arquivo.
        if (!parentDir.getMetaData().canWrite(usuario)) {
            throw new PermissaoException("Permissão negada: não pode criar arquivo em '" + parentPath + "' para o usuário '" + usuario + "'.");
        }

        // Se o arquivo já existe, apenas atualiza o tempo de modificação (comportamento padrão do touch).
        if (parentDir.containsFile(fileName)) {
            File existingFile = parentDir.getFile(fileName);
            existingFile.getMetaData().updateModificationTime();
            System.out.println("Arquivo '" + fileName + "' já existe, tempo de modificação atualizado.");
            return; // Touch não lança exceção se o arquivo existe e é apenas atualizado.
        }
        // Se já existe um diretório com o mesmo nome, lança exceção, pois não pode criar um arquivo.
        else if (parentDir.containsSubdirectory(fileName)) {
            throw new CaminhoJaExistenteException("Não é possível criar arquivo '" + fileName + "': já existe um diretório com este nome.");
        }

        // Cria e adiciona o novo arquivo.
        File newFile = new File(fileName, usuario);
        parentDir.addFile(newFile);
        System.out.println("Arquivo '" + fileName + "' criado em '" + parentPath + "'.");
    }

    /**
     * Escreve dados em um arquivo.
     * Caso `anexar` (append) seja true, os dados serão adicionados ao final do arquivo.
     * Escrita sequencial. A interface IFileSystem não define offset, apenas 'anexar'.
     * @param caminho O caminho do arquivo onde escrever.
     * @param usuario O usuário que está tentando escrever no arquivo.
     * @param anexar Se verdadeiro, anexa os dados ao final do arquivo. Se falso, sobrescreve a partir do início.
     * @param buffer O array de bytes a ser escrito.
     * @throws CaminhoNaoEncontradoException Se o arquivo não existir.
     * @throws PermissaoException Se o usuário não tiver permissão de escrita no arquivo.
     */
    @Override
    public void write(String caminho, String usuario, boolean anexar, byte[] buffer)
            throws CaminhoNaoEncontradoException, PermissaoException {
        if (caminho == null || caminho.isEmpty()) {
            throw new IllegalArgumentException("Caminho inválido para write.");
        }

        Object node = getNodeAtPath(caminho);
        if (!(node instanceof File)) {
            throw new CaminhoNaoEncontradoException("'" + caminho + "' não é um arquivo ou não existe para escrita.");
        }

        File file = (File) node;
        // Verifica permissão de escrita no arquivo.
        if (!file.getMetaData().canWrite(usuario)) {
            throw new PermissaoException("Permissão negada: não pode escrever em '" + caminho + "' para o usuário '" + usuario + "'.");
        }

        // Se 'anexar' for falso, limpa o conteúdo do arquivo antes de escrever (sobrescreve a partir do início).
        if (!anexar) {
            file.clearContent();
        }
        
        long offset = file.getSize(); // O offset para escrita é sempre o final do arquivo no modo 'anexar'.
                                     // Se não for 'anexar', clearContent() já zerou o arquivo, então o offset 0 funciona.
        long bytesWritten = file.write(buffer, offset, anexar); // Chamar o write do File com o offset e flag anexar
        System.out.println(bytesWritten + " bytes escritos em '" + caminho + "'.");
    }

    /**
     * Lê dados de um arquivo. Leitura sequencial - todo o conteúdo do arquivo será lido e armazenado no buffer.
     * O buffer passado deve ter tamanho suficiente para o conteúdo lido.
     * @param caminho O caminho do arquivo para ler.
     * @param usuario O usuário que está tentando ler o arquivo.
     * @param buffer O array de bytes onde os dados lidos serão armazenados.
     * @throws CaminhoNaoEncontradoException Se o arquivo não existir.
     * @throws PermissaoException Se o usuário não tiver permissão de leitura no arquivo.
     */
    @Override
    public void read(String caminho, String usuario, byte[] buffer)
            throws CaminhoNaoEncontradoException, PermissaoException {
        if (caminho == null || caminho.isEmpty()) {
            throw new IllegalArgumentException("Caminho inválido para read.");
        }

        Object node = getNodeAtPath(caminho);
        if (!(node instanceof File)) {
            throw new CaminhoNaoEncontradoException("'" + caminho + "' não é um arquivo ou não existe para leitura.");
        }

        File file = (File) node;
        // Verifica permissão de leitura no arquivo.
        if (!file.getMetaData().canRead(usuario)) {
            throw new PermissaoException("Permissão negada: não pode ler '" + caminho + "' para o usuário '" + usuario + "'.");
        }

        // Calcula o tamanho real a ser lido, limitado pelo tamanho do arquivo e pelo tamanho do buffer fornecido.
        int actualLengthToRead = (int) Math.min(buffer.length, file.getSize());
        // Se o buffer for 0 ou o arquivo for vazio, não há nada para ler.
        if (actualLengthToRead <= 0) {
            System.out.println("0 bytes lidos de '" + caminho + "'. (Buffer ou arquivo vazio)");
            // O buffer não é preenchido, mas não é um erro se não há nada para ler.
            return;
        }

        // Realiza a leitura a partir do início do arquivo (offset 0).
        long bytesRead = file.read(buffer, 0, actualLengthToRead);

        // Se menos bytes foram lidos do que o tamanho do buffer, preenche o restante com zeros ou não altera.
        // O Main.java vai precisar usar apenas a parte válida do buffer.
        System.out.println(bytesRead + " bytes lidos de '" + caminho + "'.");
    }

    /**
     * Move ou renomeia um arquivo ou diretório.
     * @param caminhoAntigo O caminho do arquivo/diretório de origem.
     * @param caminhoNovo O caminho do arquivo/diretório de destino.
     * @param usuario O usuário que está tentando realizar a movimentação.
     * @throws CaminhoNaoEncontradoException Se o caminho de origem ou destino não for encontrado.
     * @throws PermissaoException Se o usuário não tiver permissão para mover/renomear.
     * @throws CaminhoJaExistenteException Se já existir um item no destino que impeça a movimentação.
     */
    @Override
    public void mv(String caminhoAntigo, String caminhoNovo, String usuario)
            throws CaminhoNaoEncontradoException, PermissaoException { // Removido CaminhoJaExistenteException do throws aqui
        if (caminhoAntigo == null || caminhoAntigo.isEmpty() || caminhoNovo == null || caminhoNovo.isEmpty()) {
            throw new IllegalArgumentException("Caminhos de origem ou destino inválidos para mv.");
        }
        if (caminhoAntigo.equals(caminhoNovo)) {
            throw new IllegalArgumentException("Caminhos de origem e destino são os mesmos.");
        }
        if ("/".equals(caminhoAntigo)) {
            throw new PermissaoException("Não é possível mover o diretório raiz.");
        }

        Object sourceNode = getNodeAtPath(caminhoAntigo);
        if (sourceNode == null) {
            throw new CaminhoNaoEncontradoException("mv: '" + caminhoAntigo + "': Nenhum arquivo ou diretório encontrado.");
        }

        String[] sourceParentAndName = resolveParentAndName(caminhoAntigo);
        String sourceNodeName = sourceParentAndName[1];
        Directory sourceParentDir = getParentDirectory(caminhoAntigo, usuario);

        // Se o diretório pai da origem não existe ou não tem permissão de navegação.
        if (sourceParentDir == null) {
            throw new CaminhoNaoEncontradoException("Caminho pai de origem não existe para '" + caminhoAntigo + "'.");
        }
        // Verificar permissão de escrita no diretório pai da origem para remover o item.
        if (!sourceParentDir.getMetaData().canWrite(usuario)) {
            throw new PermissaoException("Permissão negada: não pode remover item em '" + sourceParentAndName[0] + "' para o usuário '" + usuario + "'.");
        }
        // Verificar permissão de execução no diretório pai da origem para navegar.
        if (!sourceParentDir.getMetaData().canExecute(usuario)) {
            throw new PermissaoException("Permissão negada: não pode navegar no diretório de origem '" + sourceParentAndName[0] + "' para o usuário '" + usuario + "'.");
        }

        String[] destParentAndName = resolveParentAndName(caminhoNovo);
        String destName = destParentAndName[1];
        Directory destParentDir = null;
        Object existingDest = getNodeAtPath(caminhoNovo);

        // Caso especial: mover diretamente para a raiz (ex: mv /a /)
        if (caminhoNovo.equals("/")) {
            destParentDir = root;
            destName = sourceNodeName; // Mantém o nome original da fonte se o destino é só a raiz
        } else {
            try {
                destParentDir = getParentDirectory(caminhoNovo, usuario);
            } catch (CaminhoNaoEncontradoException e) {
                // Se o caminho pai não existe, é um erro, a menos que o destino seja um novo nome no pai atual.
                // Isso cobre casos como `mv /a /b` onde `/b` não existe e `/` é o pai, e `mv /a /dir/b` onde `/dir` não existe.
                if (existingDest == null) { // Só lança se o nó não existe e o pai não existe
                    throw new CaminhoNaoEncontradoException("Caminho de destino pai não existe para '" + caminhoNovo + "'.");
                }
            }
        }
        
        // Verifica se o diretório pai de destino é válido (ou se estamos movendo para a raiz).
        if (destParentDir == null) { // Isso significa que o getParentDirectory não encontrou um pai válido, e não era a raiz.
             throw new CaminhoNaoEncontradoException("Diretório de destino inválido ou permissão negada: '" + caminhoNovo + "'");
        }
        
        // Permissão de escrita no diretório pai de destino para adicionar o item.
        if (!destParentDir.getMetaData().canWrite(usuario)) {
            throw new PermissaoException("Permissão negada: não pode escrever no diretório de destino '" + destParentAndName[0] + "' para o usuário '" + usuario + "'.");
        }

        // Lógica de movimentação
        // Caso 1: Mover para um diretório existente (ex: mv /file /dir -> /dir/file)
        // Caso 2: Renomear/Substituir um item existente (ex: mv /file1 /file2 -> /file2 é substituído por /file1)
        // Caso 3: Mover para um novo caminho/nome (ex: mv /file1 /newfile)
        
        try {
            if (sourceNode instanceof File) {
                File sourceFile = (File) sourceNode;

                if (existingDest instanceof Directory) {
                    // Caso 1: Mover arquivo para dentro de um diretório existente
                    Directory targetDir = (Directory) existingDest;
                    if (targetDir.containsFile(sourceFile.getMetaData().getName()) || targetDir.containsSubdirectory(sourceFile.getMetaData().getName())) {
                        throw new CaminhoJaExistenteException("Já existe um item com o nome '" + sourceFile.getMetaData().getName() + "' no diretório de destino '" + caminhoNovo + "'.");
                    }
                    sourceParentDir.removeFile(sourceNodeName);
                    targetDir.addFile(sourceFile);
                    sourceFile.getMetaData().updateModificationTime();
                    System.out.println("Arquivo '" + caminhoAntigo + "' movido para '" + caminhoNovo + "/" + sourceFile.getMetaData().getName() + "'.");
                } else if (existingDest instanceof File) {
                    // Caso 2: Renomear/Sobrescrever um arquivo existente
                    File destFile = (File) existingDest;
                    if (!destFile.getMetaData().canWrite(usuario)) {
                        throw new PermissaoException("Permissão negada: não pode sobrescrever o arquivo de destino '" + caminhoNovo + "' para o usuário '" + usuario + "'.");
                    }
                    sourceParentDir.removeFile(sourceNodeName); // Remove o arquivo de origem
                    destParentDir.removeFile(destFile.getMetaData().getName()); // Remove o arquivo a ser sobrescrito
                    sourceFile.getMetaData().setName(destName); // Renomeia o arquivo original
                    destParentDir.addFile(sourceFile); // Adiciona o arquivo renomeado no novo local
                    sourceFile.getMetaData().updateModificationTime();
                    System.out.println("Arquivo '" + caminhoAntigo + "' movido e renomeado para '" + caminhoNovo + "'.");
                } else {
                    // Caso 3: Mover para um novo nome/local (destino não existe)
                    sourceParentDir.removeFile(sourceNodeName);
                    sourceFile.getMetaData().setName(destName);
                    destParentDir.addFile(sourceFile);
                    sourceFile.getMetaData().updateModificationTime();
                    System.out.println("Arquivo '" + caminhoAntigo + "' movido e/ou renomeado para '" + caminhoNovo + "'.");
                }

            } else if (sourceNode instanceof Directory) {
                Directory sourceDir = (Directory) sourceNode;

                // Não permite mover um diretório para dentro de si mesmo ou um subdiretório.
                if (caminhoNovo.startsWith(caminhoAntigo + "/")) {
                    throw new IllegalArgumentException("Não é possível mover um diretório para um de seus subdiretórios.");
                }

                if (existingDest instanceof Directory) {
                    // Caso 1: Mover diretório para dentro de um diretório existente
                    Directory targetDir = (Directory) existingDest;
                    if (targetDir.containsSubdirectory(sourceDir.getMetaData().getName()) || targetDir.containsFile(sourceDir.getMetaData().getName())) {
                        throw new CaminhoJaExistenteException("Já existe um item com o nome '" + sourceDir.getMetaData().getName() + "' no diretório de destino '" + caminhoNovo + "'.");
                    }
                    sourceParentDir.removeSubdirectory(sourceNodeName);
                    targetDir.addSubdirectory(sourceDir);
                    sourceDir.getMetaData().updateModificationTime();
                    System.out.println("Diretório '" + caminhoAntigo + "' movido para '" + caminhoNovo + "/" + sourceDir.getMetaData().getName() + "'.");
                } else if (existingDest instanceof File) {
                    throw new CaminhoJaExistenteException("Não é possível mover diretório para um arquivo existente: '" + caminhoNovo + "'.");
                } else {
                    // Caso 3: Mover para um novo nome/local (destino não existe)
                    sourceParentDir.removeSubdirectory(sourceNodeName);
                    sourceDir.getMetaData().setName(destName);
                    destParentDir.addSubdirectory(sourceDir);
                    sourceDir.getMetaData().updateModificationTime();
                    System.out.println("Diretório '" + caminhoAntigo + "' movido e/ou renomeado para '" + caminhoNovo + "'.");
                }
            }
        } catch (CaminhoJaExistenteException e) {
            // Captura a exceção interna e a relança para que seja tratada pelo Main
            throw new PermissaoException(e.getMessage()); // Re-lança como PermissaoException ou nova exceção específica de mv.
                                                          // A sua IFileSystem só permite PermissaoException e CaminhoNaoEncontradoException para mv.
        }
    }

    /**
     * Lista o conteúdo de um diretório.
     * @param caminho O caminho do diretório a ser listado.
     * @param usuario O usuário que está tentando listar.
     * @param recursivo Se verdadeiro, lista recursivamente.
     * @throws CaminhoNaoEncontradoException Se o caminho não for encontrado.
     * @throws PermissaoException Se o usuário não tiver permissão para listar.
     */
    @Override
    public void ls(String caminho, String usuario, boolean recursivo) throws CaminhoNaoEncontradoException, PermissaoException {
        Object node = getNodeAtPath(caminho);
        if (node == null) {
            throw new CaminhoNaoEncontradoException("ls: '" + caminho + "': Nenhum arquivo ou diretório encontrado.");
        }

        // Se o nó é um arquivo, apenas lista suas informações.
        if (node instanceof File) {
            File file = (File) node;
            if (!file.getMetaData().canRead(usuario)) {
                throw new PermissaoException("Permissão negada: não pode ler metadados de '" + caminho + "' para o usuário '" + usuario + "'.");
            }
            System.out.println(file.getMetaData().getName() + " (arquivo, " + file.getSize() + " bytes) - "
                    + file.getMetaData().getPermissions().getOrDefault(file.getMetaData().getOwner(), "---") + " " + file.getMetaData().getOwner());
            return;
        }

        // Se o nó é um diretório.
        Directory dirToList = (Directory) node;
        // Verifica permissão de leitura no diretório.
        if (!dirToList.getMetaData().canRead(usuario)) {
            throw new PermissaoException("Permissão negada: não pode ler diretório '" + caminho + "' para o usuário '" + usuario + "'.");
        }
        // Verifica permissão de execução (navegação) no diretório.
        if (!dirToList.getMetaData().canExecute(usuario)) {
            throw new PermissaoException("Permissão negada: não pode navegar (executar) no diretório '" + caminho + "' para o usuário '" + usuario + "'.");
        }

        StringBuilder result = new StringBuilder();
        String currentDisplayPath = caminho.equals("/") ? "/" : caminho; // Caminho para exibição

        // Chama o método auxiliar recursivo para construir a string de saída.
        lsRecursiveHelper(dirToList, recursivo, usuario, result, 0, currentDisplayPath);
        System.out.print(result.toString()); // Imprime o resultado final.
    }

    /**
     * Método auxiliar recursivo para o comando ls.
     * Constrói uma string com a listagem formatada de arquivos e subdiretórios.
     * @param currentDir O diretório atual a ser listado.
     * @param recursive Indica se a listagem deve ser recursiva.
     * @param currentUser O usuário atual.
     * @param result O StringBuilder onde a saída será acumulada.
     * @param depth A profundidade atual na árvore (para indentação).
     * @param currentPath O caminho completo do diretório atual para exibição.
     */
    private void lsRecursiveHelper(Directory currentDir, boolean recursive, String currentUser, StringBuilder result, int depth, String currentPath) {
        String indent = "  ".repeat(depth); // Indentação para a saída recursiva

        // Adiciona informações do próprio diretório na listagem.
        result.append(indent).append("Diretório: ").append(currentPath)
              .append(" (").append(currentDir.getMetaData().getPermissions().getOrDefault(currentDir.getMetaData().getOwner(), "---"))
              .append(" ").append(currentDir.getMetaData().getOwner()).append(")")
              .append("\n");

        // Lista subdiretórios.
        currentDir.getSubdirectories().values().stream()
                .sorted((d1, d2) -> d1.getMetaData().getName().compareTo(d2.getMetaData().getName()))
                .forEach(subDir -> {
                    String subDirPath = currentPath.equals("/") ? "/" + subDir.getMetaData().getName() : currentPath + "/" + subDir.getMetaData().getName();
                    result.append(indent).append("  ├── ").append(subDir.getMetaData().getName())
                          .append(" (DIR) - ")
                          .append(subDir.getMetaData().getPermissions().getOrDefault(subDir.getMetaData().getOwner(), "---"))
                          .append(" ").append(subDir.getMetaData().getOwner())
                          .append("\n");
                    // Se for recursivo e houver permissão, chama recursivamente para o subdiretório.
                    if (recursive) {
                        try {
                            if (subDir.getMetaData().canRead(currentUser) && subDir.getMetaData().canExecute(currentUser)) {
                                lsRecursiveHelper(subDir, true, currentUser, result, depth + 1, subDirPath);
                            } else {
                                // Se não tem permissão para listar/navegar no subdiretório, avisa.
                                result.append(indent).append("    (Permissão negada para listar ou navegar em '").append(subDirPath).append("')\n");
                            }
                        } catch (Exception e) {
                            // Captura qualquer exceção interna para não interromper a listagem.
                            result.append(indent).append("    (Erro ao listar '").append(subDirPath).append("': ").append(e.getMessage()).append(")\n");
                        }
                    }
                });

        // Lista arquivos.
        currentDir.getFiles().values().stream()
                .sorted((f1, f2) -> f1.getMetaData().getName().compareTo(f2.getMetaData().getName()))
                .forEach(file -> {
                    result.append(indent).append("  └── ").append(file.getMetaData().getName())
                          .append(" (FILE, ").append(file.getSize()).append(" bytes) - ")
                          .append(file.getMetaData().getPermissions().getOrDefault(file.getMetaData().getOwner(), "---"))
                          .append(" ").append(file.getMetaData().getOwner())
                          .append("\n");
                });
    }

    /**
     * Copia um arquivo ou diretório.
     * @param caminhoOrigem O caminho do arquivo/diretório de origem.
     * @param caminhoDestino O caminho do arquivo/diretório de destino.
     * @param usuario O usuário que está tentando realizar a cópia.
     * @param recursivo Não usado explicitamente no método para `cp` (cp é recursivo por natureza para diretórios).
     * @throws CaminhoNaoEncontradoException Se o caminho de origem ou destino não for encontrado.
     * @throws PermissaoException Se o usuário não tiver permissão para copiar.
     * @throws CaminhoJaExistenteException Se o item de destino já existir e for um tipo diferente ou não puder ser sobrescrito.
     */
    @Override
    public void cp(String caminhoOrigem, String caminhoDestino, String usuario, boolean recursivo)
            throws CaminhoNaoEncontradoException, PermissaoException, CaminhoJaExistenteException {
        if (caminhoOrigem == null || caminhoOrigem.isEmpty() || caminhoDestino == null || caminhoDestino.isEmpty()) {
            throw new IllegalArgumentException("Caminhos de origem ou destino inválidos para cp.");
        }

        Object sourceNode = getNodeAtPath(caminhoOrigem);
        if (sourceNode == null) {
            throw new CaminhoNaoEncontradoException("cp: '" + caminhoOrigem + "': Nenhum arquivo ou diretório encontrado.");
        }

        String[] destParentAndName = resolveParentAndName(caminhoDestino);
        String destParentPath = destParentAndName[0];
        String destName = destParentAndName[1];

        Directory destParentDir = null;
        if (!caminhoDestino.equals("/")) { // Se o destino não é a raiz, precisa de pai válido
            destParentDir = getParentDirectory(caminhoDestino, usuario);
        } else { // Se o destino é a raiz, o pai é a própria raiz (logicamente)
            destParentDir = root;
        }

        if (destParentDir == null) { // Acontece se getParentDirectory lançou CaminhoNaoEncontradoException ou se a raiz não pode ser pai
            throw new CaminhoNaoEncontradoException("cp: Destino '" + caminhoDestino + "': Caminho pai não existe.");
        }

        // Verifica permissão de escrita no diretório pai de destino.
        if (!destParentDir.getMetaData().canWrite(usuario)) {
            throw new PermissaoException("Permissão negada: não pode escrever no diretório de destino '" + destParentPath + "' para o usuário '" + usuario + "'.");
        }

        Object existingDest = getNodeAtPath(caminhoDestino);

        if (sourceNode instanceof File) {
            File sourceFile = (File) sourceNode;
            // Verifica permissão de leitura no arquivo de origem.
            if (!sourceFile.getMetaData().canRead(usuario)) {
                throw new PermissaoException("Permissão negada: não pode ler arquivo de origem '" + caminhoOrigem + "' para o usuário '" + usuario + "'.");
            }

            if (existingDest instanceof Directory) {
                // Copia arquivo para dentro do diretório de destino com o mesmo nome.
                Directory targetDir = (Directory) existingDest;
                // Se já existe um arquivo ou diretório com o mesmo nome dentro do diretório de destino.
                if (targetDir.containsFile(sourceFile.getMetaData().getName()) || targetDir.containsSubdirectory(sourceFile.getMetaData().getName())) {
                    throw new CaminhoJaExistenteException("Já existe um item com o nome '" + sourceFile.getMetaData().getName() + "' no diretório de destino '" + caminhoDestino + "'.");
                }
                File newFile = sourceFile.deepCopy(sourceFile.getMetaData().getName(), usuario);
                targetDir.addFile(newFile);
                System.out.println("Arquivo '" + sourceFile.getMetaData().getName() + "' copiado para '" + caminhoDestino + "/'.");
            } else if (existingDest instanceof File) {
                // Sobrescreve o arquivo existente.
                File destFile = (File) existingDest;
                if (!destFile.getMetaData().canWrite(usuario)) {
                    throw new PermissaoException("Permissão negada: não pode sobrescrever o arquivo de destino '" + caminhoDestino + "' para o usuário '" + usuario + "'.");
                }
                destFile.clearContent(); // Limpa o conteúdo existente
                // Lê o conteúdo da origem e escreve no destino.
                // Criar um buffer com o tamanho real do arquivo de origem para evitar alocação desnecessária.
                byte[] content = new byte[(int) sourceFile.getSize()];
                sourceFile.read(content, 0, (int) sourceFile.getSize()); // Lê todo o conteúdo da origem.
                destFile.write(content, 0, false); // Escreve o conteúdo no arquivo de destino, sobrescrevendo.
                System.out.println("Arquivo '" + sourceFile.getMetaData().getName() + "' copiado para '" + caminhoDestino + "' (sobrescrito).");
            } else {
                // Destino não existe, cria um novo arquivo no diretório pai do destino.
                File newFile = sourceFile.deepCopy(destName, usuario);
                destParentDir.addFile(newFile);
                System.out.println("Arquivo '" + sourceFile.getMetaData().getName() + "' copiado para '" + caminhoDestino + "'.");
            }
        } else if (sourceNode instanceof Directory) {
            Directory sourceDir = (Directory) sourceNode;
            // Verifica permissão de leitura e execução no diretório de origem.
            if (!sourceDir.getMetaData().canRead(usuario) || !sourceDir.getMetaData().canExecute(usuario)) {
                throw new PermissaoException("Permissão negada: não pode ler/navegar no diretório de origem '" + caminhoOrigem + "' para o usuário '" + usuario + "'.");
            }

            // Impede cópia de um diretório para dentro de si mesmo ou um subdiretório.
            if (caminhoDestino.startsWith(caminhoOrigem + "/")) {
                throw new IllegalArgumentException("Não é possível copiar um diretório para um de seus subdiretórios.");
            }

            // Onde o novo diretório (copiado) será criado.
            Directory targetParentForNewDir = destParentDir;
            String actualDestName = destName;

            if (existingDest instanceof File) {
                throw new CaminhoJaExistenteException("Não é possível copiar diretório para um arquivo existente: '" + caminhoDestino + "'.");
            } else if (existingDest instanceof Directory) {
                // Se o destino é um diretório existente (ex: cp /a /b -> copia 'a' para dentro de '/b', resultando em '/b/a')
                targetParentForNewDir = (Directory) existingDest;
                actualDestName = sourceDir.getMetaData().getName(); // Mantém o nome original do diretório.
                // Verifica se já existe um item com o mesmo nome dentro do diretório de destino.
                if (targetParentForNewDir.containsSubdirectory(actualDestName) || targetParentForNewDir.containsFile(actualDestName)) {
                     throw new CaminhoJaExistenteException("Já existe um item com o nome '" + actualDestName + "' no diretório de destino '" + caminhoDestino + "'.");
                }
            } else {
                // Se o destino não existe (ex: cp /a /b/c -> cria 'c' como cópia de 'a' dentro de '/b')
                // `targetParentForNewDir` e `actualDestName` já estão corretos.
                // Verifica se já existe um item com o mesmo nome no diretório pai de destino.
                 if (destParentDir.containsSubdirectory(actualDestName) || destParentDir.containsFile(actualDestName)) {
                    throw new CaminhoJaExistenteException("Já existe um item com o nome '" + actualDestName + "' no destino.");
                }
            }

            // Realiza a cópia recursiva do diretório e seu conteúdo.
            Directory newDir = sourceDir.deepCopyStructure(actualDestName, usuario);
            targetParentForNewDir.addSubdirectory(newDir);
            // Chama o método auxiliar recursivo para copiar os filhos.
            cpRecursiveHelper(sourceDir, newDir, usuario);
            System.out.println("Diretório '" + caminhoOrigem + "' e seu conteúdo copiados para '" + caminhoDestino + "'.");
        }
    }

    /**
     * Método auxiliar recursivo para o comando cp (para diretórios).
     * Copia arquivos e subdiretórios de forma recursiva.
     * @param source O diretório de origem.
     * @param destination O diretório de destino (cópia).
     * @param currentUser O usuário atual.
     * @throws PermissaoException Se houver erro de permissão durante a cópia de um item.
     * @throws CaminhoNaoEncontradoException Se um caminho não for encontrado durante a cópia.
     * @throws CaminhoJaExistenteException Se um item já existir e impedir a cópia.
     */
    private void cpRecursiveHelper(Directory source, Directory destination, String currentUser)
            throws PermissaoException, CaminhoNaoEncontradoException, CaminhoJaExistenteException {
        // Copia arquivos do diretório de origem para o diretório de destino.
        for (File file : source.getFiles().values()) {
            File newFile = file.deepCopy(file.getMetaData().getName(), currentUser);
            destination.addFile(newFile);
        }

        // Copia subdiretórios recursivamente.
        for (Directory subDir : source.getSubdirectories().values()) {
            Directory newSubDir = subDir.deepCopyStructure(subDir.getMetaData().getName(), currentUser);
            destination.addSubdirectory(newSubDir);
            // Chamada recursiva para copiar o conteúdo do subdiretório.
            cpRecursiveHelper(subDir, newSubDir, currentUser);
        }
    }

    /**
     * Método para alterar o usuário atual (simulação de login).
     * Este método não está na interface IFileSystem, mas é útil para a simulação no Main.java.
     * @param newUser O nome do novo usuário.
     */
    public void changeUser(String newUser) {
        if (newUser == null || newUser.trim().isEmpty()) {
            System.out.println("Erro: Nome de usuário inválido.");
            return;
        }
        this.currentUser = newUser;
        System.out.println("Usuário atual alterado para: " + this.currentUser);
    }

    /**
     * Retorna o usuário atualmente "logado".
     * Este método não está na interface IFileSystem, mas é útil para a simulação no Main.java.
     * @return O nome do usuário atual.
     */
    public String getCurrentUser() {
        return currentUser;
    }
}
