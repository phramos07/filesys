package filesys;

import java.util.HashMap;
import java.util.Map;

/**
 * Representa um diretório no sistema de arquivos virtual.
 * Cada diretório possui metadados, subdiretórios e arquivos.
 *
 * <p>
 * Permite a criação de hierarquias de diretórios e armazenamento de arquivos.
 * </p>
 *
 * @author SeuNome
 */
public class Diretorio {
    /** Metadados do diretório (nome, dono, permissões, etc). */
    MetaDados metaDados;
    /** Subdiretórios deste diretório. */
    Map<String, Diretorio> subdirs = new HashMap<>();
    /** Arquivos contidos neste diretório. */
    Map<String, Arquivo> arquivos = new HashMap<>();

    /**
     * Cria um novo diretório com nome e dono especificados.
     * Permissão padrão: "rwx" para o dono.
     *
     * @param nome Nome do diretório
     * @param dono Usuário dono do diretório
     */
    public Diretorio(String nome, String dono) {
        this.metaDados = new MetaDados(nome, dono, "rwx");
    }
}
