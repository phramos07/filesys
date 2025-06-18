package filesys;

import java.util.HashMap;
import java.util.Map;

/**
 * Representa um diretório no sistema de arquivos virtual.
 * Contém metadados (nome, dono, permissões) e mapas para gerenciar
 * seus arquivos e subdiretórios filhos.
 */
public class Directory {
    private MetaData metaData; // Metadados do diretório.
    // Mapa de arquivos: Chave = nome do arquivo, Valor = objeto File.
    private Map<String, File> files;
    // Mapa de subdiretórios: Chave = nome do subdiretório, Valor = objeto Directory.
    private Map<String, Directory> subdirectories;

    /**
     * Construtor para criar um novo diretório.
     * @param name O nome do diretório.
     * @param owner O dono do diretório.
     */
    public Directory(String name, String owner) {
        // Inicializa metadados, indicando que é um diretório (true).
        this.metaData = new MetaData(name, owner, true);
        this.files = new HashMap<>(); // Inicializa o mapa de arquivos vazio.
        this.subdirectories = new HashMap<>(); // Inicializa o mapa de subdiretórios vazio.
    }

    /**
     * Retorna os metadados do diretório.
     * @return O objeto MetaData associado a este diretório.
     */
    public MetaData getMetaData() {
        return metaData;
    }

    /**
     * Adiciona um arquivo a este diretório.
     * @param file O objeto File a ser adicionado.
     */
    public void addFile(File file) {
        this.files.put(file.getMetaData().getName(), file);
        this.metaData.updateModificationTime(); // Atualiza o timestamp de modificação do diretório.
    }

    /**
     * Adiciona um subdiretório a este diretório.
     * @param directory O objeto Directory a ser adicionado.
     */
    public void addSubdirectory(Directory directory) {
        this.subdirectories.put(directory.getMetaData().getName(), directory);
        this.metaData.updateModificationTime(); // Atualiza o timestamp de modificação do diretório.
    }

    /**
     * Remove um arquivo deste diretório pelo seu nome.
     * @param name O nome do arquivo a ser removido.
     */
    public void removeFile(String name) {
        this.files.remove(name);
        this.metaData.updateModificationTime(); // Atualiza o timestamp de modificação do diretório.
    }

    /**
     * Remove um subdiretório deste diretório pelo seu nome.
     * @param name O nome do subdiretório a ser removido.
     */
    public void removeSubdirectory(String name) {
        this.subdirectories.remove(name);
        this.metaData.updateModificationTime(); // Atualiza o timestamp de modificação do diretório.
    }

    /**
     * Retorna um arquivo pelo seu nome.
     * @param name O nome do arquivo.
     * @return O objeto File se encontrado, null caso contrário.
     */
    public File getFile(String name) {
        return files.get(name);
    }

    /**
     * Retorna um subdiretório pelo seu nome.
     * @param name O nome do subdiretório.
     * @return O objeto Directory se encontrado, null caso contrário.
     */
    public Directory getSubdirectory(String name) {
        return subdirectories.get(name);
    }

    /**
     * Verifica se o diretório contém um arquivo com o nome especificado.
     * @param name O nome do arquivo.
     * @return Verdadeiro se o arquivo existe, falso caso contrário.
     */
    public boolean containsFile(String name) {
        return files.containsKey(name);
    }

    /**
     * Verifica se o diretório contém um subdiretório com o nome especificado.
     * @param name O nome do subdiretório.
     * @return Verdadeiro se o subdiretório existe, falso caso contrário.
     */
    public boolean containsSubdirectory(String name) {
        return subdirectories.containsKey(name);
    }

    /**
     * Retorna um mapa dos arquivos neste diretório.
     * @return Um mapa (nome do arquivo -> objeto File).
     */
    public Map<String, File> getFiles() {
        return files;
    }

    /**
     * Retorna um mapa dos subdiretórios neste diretório.
     * @return Um mapa (nome do diretório -> objeto Directory).
     */
    public Map<String, Directory> getSubdirectories() {
        return subdirectories;
    }

    /**
     * Verifica se o diretório está vazio (não contém arquivos nem subdiretórios).
     * @return Verdadeiro se o diretório está vazio, falso caso contrário.
     */
    public boolean isEmpty() {
        return files.isEmpty() && subdirectories.isEmpty();
    }

    /**
     * Cria uma cópia profunda da estrutura deste diretório.
     * Isso cria um novo objeto Directory com os mesmos metadados,
     * mas seus mapas de filhos (arquivos e subdiretórios) são inicialmente vazios.
     * É usado como ponto de partida para cópias recursivas de diretórios.
     * @param newName O nome do novo diretório copiado.
     * @param newOwner O dono do novo diretório copiado.
     * @return Uma nova instância de Directory com a estrutura copiada.
     */
    public Directory deepCopyStructure(String newName, String newOwner) {
        Directory newDir = new Directory(newName, newOwner); // Cria um novo diretório.
        // Copia as permissões do diretório original para o novo diretório.
        newDir.getMetaData().setPermission(newOwner, this.metaData.getPermissions().get(this.metaData.getOwner()));
        if(this.metaData.getPermissions().containsKey("other")) {
            newDir.getMetaData().setPermission("other", this.metaData.getPermissions().get("other"));
        }
        newDir.metaData.updateModificationTime(); // Atualiza o timestamp de modificação.
        return newDir;
    }
}
