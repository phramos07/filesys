package filesys;

import java.util.HashMap;
import java.util.Map;

/**
 * Classe que representa os metadados de arquivos e diretórios no sistema de
 * arquivos virtual.
 * Armazena nome, tamanho, dono e permissões de acesso por usuário.
 *
 * <p>
 * Permissões são representadas como strings do tipo "rwx" para cada usuário.
 * </p>
 *
 * @author SeuNome
 */
class MetaDados {
    /** Nome do arquivo ou diretório. */
    private String nome;
    /** Tamanho do arquivo (em bytes). */
    private int tamanho;
    /** Nome do usuário dono do arquivo/diretório. */
    private String dono;
    /** Mapa de permissões: usuário → string de permissões (ex: "rwx"). */
    private Map<String, String> permissoes; // user → "rwx"

    /**
     * Cria metadados para um arquivo ou diretório.
     * 
     * @param nome               Nome do arquivo/diretório
     * @param dono               Usuário dono
     * @param permissoesIniciais Permissões iniciais do dono (ex: "rwx")
     */
    public MetaDados(String nome, String dono, String permissoesIniciais) {
        this.nome = nome;
        this.dono = dono;
        this.permissoes = new HashMap<>();
        this.permissoes.put(dono, permissaoPadrao(permissoesIniciais));
        this.tamanho = 0;
    }

    /**
     * Retorna a permissão padrão se a string for nula ou vazia.
     * 
     * @param p Permissão informada
     * @return Permissão válida (ex: "rwx" ou "---")
     */
    private String permissaoPadrao(String p) {
        return (p == null || p.isEmpty()) ? "---" : p;
    }

    /**
     * Verifica se o usuário tem permissão de escrita.
     * 
     * @param usuario Usuário
     * @return true se pode escrever
     */
    public boolean podeEscrever(String usuario) {
        return permissoes.containsKey(usuario) && permissoes.get(usuario).contains("w");
    }

    /**
     * Verifica se o usuário tem permissão de leitura.
     * 
     * @param usuario Usuário
     * @return true se pode ler
     */
    public boolean podeLer(String usuario) {
        return permissoes.containsKey(usuario) && permissoes.get(usuario).contains("r");
    }

    /**
     * Verifica se o usuário tem permissão de execução.
     * 
     * @param usuario Usuário
     * @return true se pode executar
     */
    public boolean podeExecutar(String usuario) {
        return permissoes.containsKey(usuario) && permissoes.get(usuario).contains("x");
    }

    /**
     * Define a permissão de um usuário.
     * 
     * @param usuario   Usuário
     * @param permissao Permissão (ex: "rwx")
     */
    public void setPermissao(String usuario, String permissao) {
        permissoes.put(usuario, permissao);
    }

    /**
     * Retorna a permissão de um usuário.
     * 
     * @param usuario Usuário
     * @return Permissão (ex: "rwx")
     */
    public String getPermissao(String usuario) {
        return permissoes.get(usuario);
    }

    /**
     * Retorna o nome do dono do arquivo/diretório.
     * 
     * @return Nome do dono
     */
    public String getDono() {
        return dono;
    }

    /**
     * Retorna o tamanho do arquivo.
     * 
     * @return Tamanho em bytes
     */
    public int getTamanho() {
        return tamanho;
    }

    /**
     * Define o tamanho do arquivo.
     * 
     * @param tamanho Novo tamanho
     */
    public void setTamanho(int tamanho) {
        this.tamanho = tamanho;
    }

    /**
     * Define o nome do arquivo/diretório.
     * 
     * @param nome Novo nome
     */
    public void setNome(String nome) {
        this.nome = nome;
    }

    @Override
    public String toString() {
        return "MetaDados{" +
                "nome='" + nome + '\'' +
                ", tamanho=" + tamanho +
                ", dono='" + dono + '\'' +
                ", permissoes=" + permissoes +
                '}';
    }

}
