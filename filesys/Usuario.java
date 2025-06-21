package filesys;

/**
 * Representa um usuário do sistema de arquivos virtual.
 * Cada usuário possui um nome, diretório inicial e permissões globais.
 *
 * <p>
 * Permissões são usadas para controle de acesso em diretórios e arquivos.
 * </p>
 *
 * @author SeuNome
 */
public class Usuario {
    /** Diretório inicial do usuário. */
    private String diretorio;
    /** Nome do usuário. */
    private String nome;
    /** Permissões globais do usuário (ex: "rwx"). */
    private String permissoes;

    /**
     * Cria um novo usuário.
     * 
     * @param nome       Nome do usuário
     * @param diretorio  Diretório inicial
     * @param permissoes Permissões globais (ex: "rwx")
     */
    public Usuario(String nome, String diretorio, String permissoes) {
        this.nome = nome;
        this.diretorio = diretorio;
        this.permissoes = permissoes;
    }

    /**
     * Retorna o diretório inicial do usuário.
     * 
     * @return Diretório inicial
     */
    public String getDiretorio() {
        return diretorio;
    }

    /**
     * Retorna o nome do usuário.
     * 
     * @return Nome
     */
    public String getNome() {
        return nome;
    }

    /**
     * Retorna as permissões globais do usuário.
     * 
     * @return Permissões (ex: "rwx")
     */
    public String getPermissoes() {
        return permissoes;
    }

    /**
     * Define o diretório inicial do usuário.
     * 
     * @param diretorio Novo diretório
     */
    public void setDiretorio(String diretorio) {
        this.diretorio = diretorio;
    }

    /**
     * Define o nome do usuário.
     * 
     * @param nome Novo nome
     */
    public void setNome(String nome) {
        this.nome = nome;
    }

    /**
     * Define as permissões globais do usuário.
     * 
     * @param permissoes Novas permissões (ex: "rwx")
     */
    public void setPermissoes(String permissoes) {
        this.permissoes = permissoes;
    }

    @Override
    public String toString() {
        return "Usuario{" +
                "diretorio='" + diretorio + '\'' +
                ", nome='" + nome + '\'' +
                ", permissoes='" + permissoes + '\'' +
                '}';
    }
}
