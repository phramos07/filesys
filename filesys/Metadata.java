package filesys;

import java.util.HashMap;
import java.util.Map;

public class Metadata {
    private String name;
    private String owner;
    private int size;
    private Map<String, String> permissions = new HashMap<>();
    
    public Metadata(String name, String owner, int size) {
        this.name = name;
        this.owner = owner;
        this.size = size;
        permissions.put(owner, "rwx");
    }
    public Metadata(String name, String owner, int size, Map<String, String> permissions) {
        this.name = name;
        this.owner = owner;
        this.size = size;
        this.permissions.putAll(permissions);
        this.permissions.put(owner, "rwx");
    }

    public Metadata(String name, String owner) {
        this.name = name;
        this.owner = owner;
        this.size = 0;
        permissions.put(owner, "rwx");
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public Map<String, String> getPermissions() {
        return permissions;
    }

    public void setPermissions(Map<String, String> permissions) {
        this.permissions = permissions;
    }
}
