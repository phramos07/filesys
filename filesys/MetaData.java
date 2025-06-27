package filesys;

import java.util.HashMap;
import java.util.Map;

public class MetaData {
    private String name;
    private String owner;
    private boolean isDirectory;
    private long lastModified;
    private Map<String, String> permissions; // Ex: "user" -> "rwx", "other" -> "r-x"

    public MetaData(String name, String owner, boolean isDirectory) {
        this.name = name;
        this.owner = owner;
        this.isDirectory = isDirectory;
        this.lastModified = System.currentTimeMillis();
        this.permissions = new HashMap<>();
        // Permissões padrão: dono rwx, outros r-x para diretórios, r-- para arquivos
        if (isDirectory) {
            permissions.put(owner, "rwx");
            permissions.put("other", "r-x");
        } else {
            permissions.put(owner, "rw-");
            permissions.put("other", "r--");
        }
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getOwner() { return owner; }
    public boolean isDirectory() { return isDirectory; }
    public long getLastModified() { return lastModified; }
    public Map<String, String> getPermissions() { return permissions; }

    public void setPermission(String user, String perm) {
        permissions.put(user, perm);
    }

    public boolean canRead(String user) {
        return checkPermission(user, 0, 'r');
    }

    public boolean canWrite(String user) {
        return checkPermission(user, 1, 'w');
    }

    public boolean canExecute(String user) {
        return checkPermission(user, 2, 'x');
    }

    private boolean checkPermission(String user, int pos, char permChar) {
        String perm = permissions.get(user);
        if (perm == null) perm = permissions.get("other");
        if (perm == null || perm.length() < 3) return false;
        return perm.charAt(pos) == permChar;
    }

    public void updateModificationTime() {
        this.lastModified = System.currentTimeMillis();
    }
}