package filesys;

import exception.CaminhoJaExistenteException;
import exception.CaminhoNaoEncontradoException;
import exception.PermissaoException;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class FileSystemImpl implements IFileSystem {
    private Directory root;
    private String currentUser;
    private static final String ROOT_USER = "root";

    public FileSystemImpl() {
        this.root = new Directory("/", ROOT_USER);
        this.currentUser = ROOT_USER;
        System.out.println("Sistema de arquivos inicializado. Usuário atual: " + currentUser);
    }

    private List<String> getPathComponents(String path) {
        return Arrays.stream(path.split("/"))
                     .filter(s -> !s.isEmpty())
                     .collect(Collectors.toList());
    }

    private String[] resolveParentAndName(String path) {
        if ("/".equals(path)) {
            return new String[]{"", ""};
        }
        List<String> components = getPathComponents(path);
        if (components.isEmpty()) {
            return new String[]{"", ""};
        }
        String name = components.get(components.size() - 1);
        List<String> parentComponents = components.subList(0, components.size() - 1);
        String parentPath = "/" + String.join("/", parentComponents);
        if (parentPath.isEmpty()) {
            parentPath = "/";
        }
        return new String[]{parentPath, name};
    }

    public Object getNodeAtPath(String path) {
        if ("/".equals(path) || path.isEmpty()) {
            return root;
        }
        List<String> components = getPathComponents(path);
        Directory currentDir = root;
        for (int i = 0; i < components.size(); i++) {
            String component = components.get(i);
            if (i == components.size() - 1) {
                if (currentDir.containsSubdirectory(component)) {
                    return currentDir.getSubdirectory(component);
                } else if (currentDir.containsFile(component)) {
                    return currentDir.getFile(component);
                } else {
                    return null;
                }
            } else {
                if (currentDir.containsSubdirectory(component)) {
                    currentDir = currentDir.getSubdirectory(component);
                } else {
                    return null;
                }
            }
        }
        return null;
    }

    private Directory getParentDirectory(String path, String currentUser) throws CaminhoNaoEncontradoException, PermissaoException {
        if ("/".equals(path) || path.isEmpty()) {
            return null;
        }
        String[] parentAndName = resolveParentAndName(path);
        String parentPath = parentAndName[0];
        Object node = getNodeAtPath(parentPath);
        if (node instanceof Directory) {
            Directory parentDir = (Directory) node;
            if (!parentDir.getMetaData().canExecute(currentUser)) {
                throw new PermissaoException("Permissão negada: não pode navegar em '" + parentPath + "' para o usuário '" + currentUser + "'.");
            }
            return parentDir;
        }
        throw new CaminhoNaoEncontradoException("Caminho pai '" + parentPath + "' não existe ou não é um diretório.");
    }

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
            throw new CaminhoNaoEncontradoException("chmod: '" + caminho + "': Tipo de nó desconhecido.");
        }
        if (ROOT_USER.equals(usuario)) {
        } else if (!metaDataToChange.getOwner().equals(usuario)) {
            throw new PermissaoException("Permissão negada: Somente o dono ou '" + ROOT_USER + "' pode alterar permissões de '" + caminho + "'.");
        } else if (!metaDataToChange.canWrite(usuario)) {
             throw new PermissaoException("Permissão negada: O usuário '" + usuario + "' é o dono, mas não tem permissão de escrita no item para alterar suas permissões de '" + caminho + "'.");
        }
        if (permissao == null || !permissao.matches("[r-][w-][x-]")) {
            throw new IllegalArgumentException("Formato de permissão inválido. Use 'rwx', 'rw-', 'r-x', '---', etc.");
        }
        metaDataToChange.setPermission(usuarioAlvo, permissao);
        System.out.println("Permissões de '" + caminho + "' para o usuário '" + usuarioAlvo + "' alteradas para '" + permissao + "'.");
    }

    @Override
    public void mkdir(String caminho, String usuario) throws CaminhoJaExistenteException, PermissaoException {
        if (caminho == null || caminho.isEmpty() || "/".equals(caminho)) {
            throw new CaminhoJaExistenteException("Caminho inválido ou raiz já existente para mkdir.");
        }
        List<String> components = getPathComponents(caminho);
        Directory currentDir = root;
        for (int i = 0; i < components.size(); i++) {
            String component = components.get(i);
            if (!currentDir.getMetaData().canWrite(usuario)) {
                throw new PermissaoException("Permissão negada: não pode criar diretório em '" + currentDir.getMetaData().getName() + "' para o usuário '" + usuario + "'.");
            }
            if (currentDir.containsFile(component)) {
                throw new CaminhoJaExistenteException("Não é possível criar diretório '" + component + "': já existe um arquivo com este nome.");
            }
            if (currentDir.containsSubdirectory(component)) {
                currentDir = currentDir.getSubdirectory(component);
            } else {
                Directory newDir = new Directory(component, usuario);
                newDir.getMetaData().setPermission(usuario, "rwx");
                newDir.getMetaData().setPermission("other", "r-x");
                currentDir.addSubdirectory(newDir);
                currentDir = newDir;
                System.out.println("Diretório '" + component + "' criado em '" + (i == 0 ? "/" : "/" + String.join("/", components.subList(0, i))) + "'.");
            }
        }
        System.out.println("Comando mkdir para '" + caminho + "' executado com sucesso.");
    }

    @Override
    public void rm(String caminho, String usuario, boolean recursivo)
            throws CaminhoNaoEncontradoException, PermissaoException {
        if (caminho == null || caminho.isEmpty() || "/".equals(caminho)) {
            throw new PermissaoException("Não é possível remover a raiz ou caminho inválido para rm.");
        }
        String[] parentAndName = resolveParentAndName(caminho);
        String nodeName = parentAndName[1];
        Directory parentDir = getParentDirectory(caminho, usuario);
        if (parentDir == null) {
            throw new CaminhoNaoEncontradoException("Caminho pai de '" + caminho + "' não existe.");
        }
        if (!parentDir.getMetaData().canWrite(usuario)) {
            throw new PermissaoException("Permissão negada: não pode remover em '" + parentAndName[0] + "' para o usuário '" + usuario + "'.");
        }
        Object nodeToRemove = getNodeAtPath(caminho);
        if (nodeToRemove == null) {
            throw new CaminhoNaoEncontradoException("rm: '" + caminho + "': Nenhum arquivo ou diretório encontrado para remoção.");
        }
        if (nodeToRemove instanceof File) {
            parentDir.removeFile(nodeName);
            System.out.println("Arquivo '" + caminho + "' removido com sucesso.");
        } else if (nodeToRemove instanceof Directory) {
            Directory dirToRemove = (Directory) nodeToRemove;
            if (!dirToRemove.isEmpty() && !recursivo) {
                throw new PermissaoException("Diretório '" + caminho + "' não está vazio. Use rm -r para remover recursivamente.");
            }
            parentDir.removeSubdirectory(nodeName);
            System.out.println("Diretório '" + caminho + "' e seu conteúdo (se houver) removidos com sucesso.");
        } else {
            throw new CaminhoNaoEncontradoException("rm: '" + caminho + "': Tipo de nó desconhecido para remoção.");
        }
    }

    @Override
    public void touch(String caminho, String usuario) throws CaminhoJaExistenteException, PermissaoException, CaminhoNaoEncontradoException {
        if (caminho == null || caminho.isEmpty() || "/".equals(caminho)) {
            throw new IllegalArgumentException("Caminho inválido para touch.");
        }
        String[] parentAndName = resolveParentAndName(caminho);
        String parentPath = parentAndName[0];
        String fileName = parentAndName[1];
        Directory parentDir = getParentDirectory(caminho, usuario);
        if (parentDir == null) {
             parentDir = root;
        }
        if (!parentDir.getMetaData().canWrite(usuario)) {
            throw new PermissaoException("Permissão negada: não pode criar arquivo em '" + parentPath + "' para o usuário '" + usuario + "'.");
        }
        if (parentDir.containsFile(fileName)) {
            File existingFile = parentDir.getFile(fileName);
            existingFile.getMetaData().updateModificationTime();
            System.out.println("Arquivo '" + fileName + "' já existe, tempo de modificação atualizado.");
            return;
        } else if (parentDir.containsSubdirectory(fileName)) {
            throw new CaminhoJaExistenteException("Não é possível criar arquivo '" + fileName + "': já existe um diretório com este nome.");
        }
        File newFile = new File(fileName, usuario);
        parentDir.addFile(newFile);
        System.out.println("Arquivo '" + fileName + "' criado em '" + parentPath + "'.");
    }

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
        if (!file.getMetaData().canWrite(usuario)) {
            throw new PermissaoException("Permissão negada: não pode escrever em '" + caminho + "' para o usuário '" + usuario + "'.");
        }
        if (!anexar) {
            file.clearContent();
        }
        long offset = file.getSize();
        long bytesWritten = file.write(buffer, offset, anexar);
        System.out.println(bytesWritten + " bytes escritos em '" + caminho + "'.");
    }

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
        if (!file.getMetaData().canRead(usuario)) {
            throw new PermissaoException("Permissão negada: não pode ler '" + caminho + "' para o usuário '" + usuario + "'.");
        }
        int actualLengthToRead = (int) Math.min(buffer.length, file.getSize());
        if (actualLengthToRead <= 0) {
            System.out.println("0 bytes lidos de '" + caminho + "'. (Buffer ou arquivo vazio)");
            return;
        }
        long bytesRead = file.read(buffer, 0, actualLengthToRead);
        System.out.println(bytesRead + " bytes lidos de '" + caminho + "'.");
    }

    @Override
    public void mv(String caminhoAntigo, String caminhoNovo, String usuario)
            throws CaminhoNaoEncontradoException, PermissaoException {
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
        if (sourceParentDir == null) {
            throw new CaminhoNaoEncontradoException("Caminho pai de origem não existe para '" + caminhoAntigo + "'.");
        }
        if (!sourceParentDir.getMetaData().canWrite(usuario)) {
            throw new PermissaoException("Permissão negada: não pode remover item em '" + sourceParentAndName[0] + "' para o usuário '" + usuario + "'.");
        }
        if (!sourceParentDir.getMetaData().canExecute(usuario)) {
            throw new PermissaoException("Permissão negada: não pode navegar no diretório de origem '" + sourceParentAndName[0] + "' para o usuário '" + usuario + "'.");
        }
        String[] destParentAndName = resolveParentAndName(caminhoNovo);
        String destName = destParentAndName[1];
        Directory destParentDir = null;
        Object existingDest = getNodeAtPath(caminhoNovo);
        if (caminhoNovo.equals("/")) {
            destParentDir = root;
            destName = sourceNodeName;
        } else {
            try {
                destParentDir = getParentDirectory(caminhoNovo, usuario);
            } catch (CaminhoNaoEncontradoException e) {
                if (existingDest == null) {
                    throw new CaminhoNaoEncontradoException("Caminho de destino pai não existe para '" + caminhoNovo + "'.");
                }
            }
        }
        if (destParentDir == null) {
             throw new CaminhoNaoEncontradoException("Diretório de destino inválido ou permissão negada: '" + caminhoNovo + "'");
        }
        if (!destParentDir.getMetaData().canWrite(usuario)) {
            throw new PermissaoException("Permissão negada: não pode escrever no diretório de destino '" + destParentAndName[0] + "' para o usuário '" + usuario + "'.");
        }
        try {
            if (sourceNode instanceof File) {
                File sourceFile = (File) sourceNode;
                if (existingDest instanceof Directory) {
                    Directory targetDir = (Directory) existingDest;
                    if (targetDir.containsFile(sourceFile.getMetaData().getName()) || targetDir.containsSubdirectory(sourceFile.getMetaData().getName())) {
                        throw new CaminhoJaExistenteException("Já existe um item com o nome '" + sourceFile.getMetaData().getName() + "' no diretório de destino '" + caminhoNovo + "'.");
                    }
                    sourceParentDir.removeFile(sourceNodeName);
                    targetDir.addFile(sourceFile);
                    sourceFile.getMetaData().updateModificationTime();
                    System.out.println("Arquivo '" + caminhoAntigo + "' movido para '" + caminhoNovo + "/" + sourceFile.getMetaData().getName() + "'.");
                } else if (existingDest instanceof File) {
                    File destFile = (File) existingDest;
                    if (!destFile.getMetaData().canWrite(usuario)) {
                        throw new PermissaoException("Permissão negada: não pode sobrescrever o arquivo de destino '" + caminhoNovo + "' para o usuário '" + usuario + "'.");
                    }
                    sourceParentDir.removeFile(sourceNodeName);
                    destParentDir.removeFile(destFile.getMetaData().getName());
                    sourceFile.getMetaData().setName(destName);
                    destParentDir.addFile(sourceFile);
                    sourceFile.getMetaData().updateModificationTime();
                    System.out.println("Arquivo '" + caminhoAntigo + "' movido e renomeado para '" + caminhoNovo + "'.");
                } else {
                    sourceParentDir.removeFile(sourceNodeName);
                    sourceFile.getMetaData().setName(destName);
                    destParentDir.addFile(sourceFile);
                    sourceFile.getMetaData().updateModificationTime();
                    System.out.println("Arquivo '" + caminhoAntigo + "' movido e/ou renomeado para '" + caminhoNovo + "'.");
                }
            } else if (sourceNode instanceof Directory) {
                Directory sourceDir = (Directory) sourceNode;
                if (caminhoNovo.startsWith(caminhoAntigo + "/")) {
                    throw new IllegalArgumentException("Não é possível mover um diretório para um de seus subdiretórios.");
                }
                if (existingDest instanceof Directory) {
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
                    sourceParentDir.removeSubdirectory(sourceNodeName);
                    sourceDir.getMetaData().setName(destName);
                    destParentDir.addSubdirectory(sourceDir);
                    sourceDir.getMetaData().updateModificationTime();
                    System.out.println("Diretório '" + caminhoAntigo + "' movido e/ou renomeado para '" + caminhoNovo + "'.");
                }
            }
        } catch (CaminhoJaExistenteException e) {
            throw new PermissaoException(e.getMessage());
        }
    }
    
    @Override
    public void ls(String caminho, String usuario, boolean recursivo) throws CaminhoNaoEncontradoException, PermissaoException {
        Object node = getNodeAtPath(caminho);
        if (node == null) {
            throw new CaminhoNaoEncontradoException("ls: '" + caminho + "': Nenhum arquivo ou diretório encontrado.");
        }
        if (node instanceof File) {
            File file = (File) node;
            if (!file.getMetaData().canRead(usuario)) {
                throw new PermissaoException("Permissão negada: não pode ler metadados de '" + caminho + "' para o usuário '" + usuario + "'.");
            }
            System.out.println(file.getMetaData().getName() + " (arquivo, " + file.getSize() + " bytes) - "
                    + file.getMetaData().getPermissions().getOrDefault(file.getMetaData().getOwner(), "---") + " " + file.getMetaData().getOwner());
            return;
        }
        Directory dirToList = (Directory) node;
        if (!dirToList.getMetaData().canRead(usuario)) {
            throw new PermissaoException("Permissão negada: não pode ler diretório '" + caminho + "' para o usuário '" + usuario + "'.");
        }
        if (!dirToList.getMetaData().canExecute(usuario)) {
            throw new PermissaoException("Permissão negada: não pode navegar (executar) no diretório '" + caminho + "' para o usuário '" + usuario + "'.");
        }
        StringBuilder result = new StringBuilder();
        String currentDisplayPath = caminho.equals("/") ? "/" : caminho;
        lsRecursiveHelper(dirToList, recursivo, usuario, result, 0, currentDisplayPath);
        System.out.print(result.toString());
    }

    private void lsRecursiveHelper(Directory currentDir, boolean recursive, String currentUser, StringBuilder result, int depth, String currentPath) {
        String indent = "  ".repeat(depth);
        result.append(indent).append("Diretório: ").append(currentPath)
              .append(" (").append(currentDir.getMetaData().getPermissions().getOrDefault(currentDir.getMetaData().getOwner(), "---"))
              .append(" ").append(currentDir.getMetaData().getOwner()).append(")")
              .append("\n");
        currentDir.getSubdirectories().values().stream()
                .sorted((d1, d2) -> d1.getMetaData().getName().compareTo(d2.getMetaData().getName()))
                .forEach(subDir -> {
                    String subDirPath = currentPath.equals("/") ? "/" + subDir.getMetaData().getName() : currentPath + "/" + subDir.getMetaData().getName();
                    result.append(indent).append("  ├── ").append(subDir.getMetaData().getName())
                          .append(" (DIR) - ")
                          .append(subDir.getMetaData().getPermissions().getOrDefault(subDir.getMetaData().getOwner(), "---"))
                          .append(" ").append(subDir.getMetaData().getOwner())
                          .append("\n");
                    if (recursive) {
                        try {
                            if (subDir.getMetaData().canRead(currentUser) && subDir.getMetaData().canExecute(currentUser)) {
                                lsRecursiveHelper(subDir, true, currentUser, result, depth + 1, subDirPath);
                            } else {
                                result.append(indent).append("    (Permissão negada para listar ou navegar em '").append(subDirPath).append("')\n");
                            }
                        } catch (Exception e) {
                            result.append(indent).append("    (Erro ao listar '").append(subDirPath).append("': ").append(e.getMessage()).append(")\n");
                        }
                    }
                });
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
        if (!caminhoDestino.equals("/")) {
            destParentDir = getParentDirectory(caminhoDestino, usuario);
        } else {
            destParentDir = root;
        }
        if (destParentDir == null) {
            throw new CaminhoNaoEncontradoException("cp: Destino '" + caminhoDestino + "': Caminho pai não existe.");
        }
        if (!destParentDir.getMetaData().canWrite(usuario)) {
            throw new PermissaoException("Permissão negada: não pode escrever no diretório de destino '" + destParentPath + "' para o usuário '" + usuario + "'.");
        }
        Object existingDest = getNodeAtPath(caminhoDestino);
        if (sourceNode instanceof File) {
            File sourceFile = (File) sourceNode;
            if (!sourceFile.getMetaData().canRead(usuario)) {
                throw new PermissaoException("Permissão negada: não pode ler arquivo de origem '" + caminhoOrigem + "' para o usuário '" + usuario + "'.");
            }
            if (existingDest instanceof Directory) {
                Directory targetDir = (Directory) existingDest;
                if (targetDir.containsFile(sourceFile.getMetaData().getName()) || targetDir.containsSubdirectory(sourceFile.getMetaData().getName())) {
                    throw new CaminhoJaExistenteException("Já existe um item com o nome '" + sourceFile.getMetaData().getName() + "' no diretório de destino '" + caminhoDestino + "'.");
                }
                File newFile = sourceFile.deepCopy(sourceFile.getMetaData().getName(), usuario);
                targetDir.addFile(newFile);
                System.out.println("Arquivo '" + sourceFile.getMetaData().getName() + "' copiado para '" + caminhoDestino + "/'.");
            } else if (existingDest instanceof File) {
                File destFile = (File) existingDest;
                if (!destFile.getMetaData().canWrite(usuario)) {
                    throw new PermissaoException("Permissão negada: não pode sobrescrever o arquivo de destino '" + caminhoDestino + "' para o usuário '" + usuario + "'.");
                }
                destFile.clearContent();
                byte[] content = new byte[(int) sourceFile.getSize()];
                sourceFile.read(content, 0, (int) sourceFile.getSize());
                destFile.write(content, 0, false);
                System.out.println("Arquivo '" + sourceFile.getMetaData().getName() + "' copiado para '" + caminhoDestino + "' (sobrescrito).");
            } else {
                File newFile = sourceFile.deepCopy(destName, usuario);
                destParentDir.addFile(newFile);
                System.out.println("Arquivo '" + sourceFile.getMetaData().getName() + "' copiado para '" + caminhoDestino + "'.");
            }
        } else if (sourceNode instanceof Directory) {
            Directory sourceDir = (Directory) sourceNode;
            if (!sourceDir.getMetaData().canRead(usuario) || !sourceDir.getMetaData().canExecute(usuario)) {
                throw new PermissaoException("Permissão negada: não pode ler/navegar no diretório de origem '" + caminhoOrigem + "' para o usuário '" + usuario + "'.");
            }
            if (caminhoDestino.startsWith(caminhoOrigem + "/")) {
                throw new IllegalArgumentException("Não é possível copiar um diretório para um de seus subdiretórios.");
            }
            Directory targetParentForNewDir = destParentDir;
            String actualDestName = destName;
            if (existingDest instanceof File) {
                throw new CaminhoJaExistenteException("Não é possível copiar diretório para um arquivo existente: '" + caminhoDestino + "'.");
            } else if (existingDest instanceof Directory) {
                targetParentForNewDir = (Directory) existingDest;
                actualDestName = sourceDir.getMetaData().getName();
                if (targetParentForNewDir.containsSubdirectory(actualDestName) || targetParentForNewDir.containsFile(actualDestName)) {
                     throw new CaminhoJaExistenteException("Já existe um item com o nome '" + actualDestName + "' no diretório de destino '" + caminhoDestino + "'.");
                }
            } else {
                 if (destParentDir.containsSubdirectory(actualDestName) || destParentDir.containsFile(actualDestName)) {
                    throw new CaminhoJaExistenteException("Já existe um item com o nome '" + actualDestName + "' no destino.");
                }
            }
            Directory newDir = sourceDir.deepCopyStructure(actualDestName, usuario);
            targetParentForNewDir.addSubdirectory(newDir);
            cpRecursiveHelper(sourceDir, newDir, usuario);
            System.out.println("Diretório '" + caminhoOrigem + "' e seu conteúdo copiados para '" + caminhoDestino + "'.");
        }
    }

    private void cpRecursiveHelper(Directory source, Directory destination, String currentUser)
            throws PermissaoException, CaminhoNaoEncontradoException, CaminhoJaExistenteException {
        for (File file : source.getFiles().values()) {
            File newFile = file.deepCopy(file.getMetaData().getName(), currentUser);
            destination.addFile(newFile);
        }
        for (Directory subDir : source.getSubdirectories().values()) {
            Directory newSubDir = subDir.deepCopyStructure(subDir.getMetaData().getName(), currentUser);
            destination.addSubdirectory(newSubDir);
            cpRecursiveHelper(subDir, newSubDir, currentUser);
        }
    }

    public void changeUser(String newUser) {
        if (newUser == null || newUser.trim().isEmpty()) {
            System.out.println("Erro: Nome de usuário inválido.");
            return;
        }
        this.currentUser = newUser;
        System.out.println("Usuário atual alterado para: " + this.currentUser);
    }

    public String getCurrentUser() {
        return currentUser;
    }
}