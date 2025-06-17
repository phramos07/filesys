package filesys;

import java.util.HashMap;
import java.util.Map;

public class MetaData {
    private String name;
    private String owner;
    private Map<String, String> permissions;
    private long creationTime; 
    private long modificationTime;

    /**
     * Construtor para MetaData.
     * @param name 
     * @param owner 
     * @param isDirectory 
     */
    public MetaData(String name, String owner, boolean isDirectory) {
        this.name = name;
        this.owner = owner;
        this.permissions = new HashMap<>();
        this.permissions.put(owner, "rwx"); // Dono tem permissão total por padrão
        if (isDirectory) {
            this.permissions.put("other", "r-x"); // Outros podem listar e navegar em diretórios
        } else {
            this.permissions.put("other", "r--"); // Outros podem ler arquivos
        }
        long now = System.currentTimeMillis();
        this.creationTime = now;
        this.modificationTime = now;
    }

    // --- Getters ---
    public String getName() {
        return name;
    }

    public String getOwner() {
        return owner;
    }

    public Map<String, String> getPermissions() {
        return permissions;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public long getModificationTime() {
        return modificationTime;
    }

    // --- Setters ---
    public void setName(String name) {
        this.name = name;
        updateModificationTime();
    }

    public void setOwner(String owner) {
        this.owner = owner;
        updateModificationTime();
    }

    /**
     * As permissões devem ser uma string de 3 caracteres (e.g., "rwx", "r-x", "---").
     * @param user O nome do usuário.
     * @param perms A string de permissões.
     */
    public void setPermission(String user, String perms) {
        if (perms != null && perms.matches("[r-][w-][x-]")) {
            this.permissions.put(user, perms);
            updateModificationTime();
        } else {
            System.err.println("Erro: Permissões inválidas. Use 'rwx', 'r-x', 'rw-', etc.");
        }
    }

    /**
     * Atualiza o tempo da última modificação para o tempo atual.
     */
    public void updateModificationTime() {
        this.modificationTime = System.currentTimeMillis();
    }

    // --- Métodos de Verificação de Permissão ---

    /**
     * Verifica se um usuário tem permissão de leitura.
     * @param user O usuário que está tentando acessar.
     * @return Verdadeiro se o usuário tem permissão de leitura, falso caso contrário.
     */
    public boolean canRead(String user) {
        // O usuário "root" (kernel) sempre tem permissão total.
        if ("root".equals(user)) {
            return true;
        }

        String userPerms = permissions.get(user);
        if (userPerms != null) {
            return userPerms.contains("r");
        }

        userPerms = permissions.get("other");
        if (userPerms != null) {
            return userPerms.contains("r");
        }
        return false;
    }
    /**
     * Verifica se um usuário tem permissão de escrita.
     * @param user O usuário que está tentando acessar.
     * @return Verdadeiro se o usuário tem permissão de escrita, falso caso contrário.
     */
    public boolean canWrite(String user) {
        // O usuário "root" (kernel) sempre tem permissão total.
        if ("root".equals(user)) {
            return true;
        }

        String userPerms = permissions.get(user);
        if (userPerms != null) {
            return userPerms.contains("w");
        }

        // Se o usuário não tem permissão explícita, verifica as permissões "other"
        userPerms = permissions.get("other");
        if (userPerms != null) {
            return userPerms.contains("w");
        }
        return false;
    }

    /**
     * Verifica se um usuário tem permissão de execução (para diretórios, significa navegação/listagem).
     * Usuário raiz ("root") e o dono sempre têm permissão total.
     * @param user O usuário que está tentando acessar.
     * @return Verdadeiro se o usuário tem permissão de execução, falso caso contrário.
     */
    public boolean canExecute(String user) {
        if ("root".equals(user)) {
            return true;
        }

        String userPerms = permissions.get(user);
        if (userPerms != null) {
            return userPerms.contains("x");
        }

        userPerms = permissions.get("other");
        if (userPerms != null) {
            return userPerms.contains("x");
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Nome: %s, Dono: %s, ", name, owner));
        sb.append("Permissões: ");
        permissions.forEach((user, perms) -> sb.append(String.format("[%s:%s]", user, perms)));
        sb.append(String.format(", Criação: %tD %tT, Modificação: %tD %tT", creationTime, creationTime, modificationTime, modificationTime));
        return sb.toString();
    }
}