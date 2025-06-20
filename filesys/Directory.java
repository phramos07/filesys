package filesys;

import java.util.LinkedHashMap;
import java.util.Map;

public class Directory {
    private MetaData metaData;
    private Map<String, Directory> subdirectories;
    private Map<String, File> files;

    public Directory(String name, String owner) {
        this.metaData = new MetaData(name, owner, true);
        this.subdirectories = new LinkedHashMap<>();
        this.files = new LinkedHashMap<>();
    }

    public MetaData getMetaData() { return metaData; }

    public Map<String, Directory> getSubdirectories() { return subdirectories; }
    public Map<String, File> getFiles() { return files; }

    public boolean containsSubdirectory(String name) {
        return subdirectories.containsKey(name);
    }

    public boolean containsFile(String name) {
        return files.containsKey(name);
    }

    public Directory getSubdirectory(String name) {
        return subdirectories.get(name);
    }

    public File getFile(String name) {
        return files.get(name);
    }

    public void addSubdirectory(Directory dir) {
        subdirectories.put(dir.getMetaData().getName(), dir);
    }

    public void addFile(File file) {
        files.put(file.getMetaData().getName(), file);
    }

    public void removeSubdirectory(String name) {
        subdirectories.remove(name);
    }

    public void removeFile(String name) {
        files.remove(name);
    }

    public boolean isEmpty() {
        return subdirectories.isEmpty() && files.isEmpty();
    }

    // Cópia profunda da estrutura (sem arquivos e subdirs)
    public Directory deepCopyStructure(String newName, String newOwner) {
        Directory newDir = new Directory(newName, newOwner);
        newDir.getMetaData().setPermission(newOwner, this.metaData.getPermissions().get(this.metaData.getOwner()));
        if (this.metaData.getPermissions().containsKey("other")) {
            newDir.getMetaData().setPermission("other", this.metaData.getPermissions().get("other"));
        }
        return newDir;
    }
}