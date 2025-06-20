package filesys;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa um arquivo no sistema de arquivos virtual.
 * Armazena metadados, conteúdo e pode futuramente utilizar blocos para escrita.
 *
 * <p>
 * O conteúdo do arquivo é armazenado como uma lista de bytes.
 * </p>
 *
 * @author SeuNome
 */
class Arquivo {
    /** Metadados do arquivo (nome, dono, permissões, etc). */
    private MetaDados metaDados;
    /** Blocos de dados do arquivo (não utilizado atualmente). */
    private Bloco[] bloco; // pode ser usado depois em write()
    /** Conteúdo do arquivo em bytes. */
    private List<Byte> conteudo = new ArrayList<>();

    /**
     * Cria um novo arquivo com nome e dono especificados.
     * Permissão padrão: "rwx" para o dono.
     *
     * @param nome Nome do arquivo
     * @param dono Usuário dono do arquivo
     */
    public Arquivo(String nome, String dono) {
        this.metaDados = new MetaDados(nome, dono, "rwx");
        this.conteudo = new ArrayList<>();
    }

    /**
     * Retorna os metadados do arquivo.
     * 
     * @return Metadados
     */
    public MetaDados getMetaDados() {
        return metaDados;
    }

    /**
     * Retorna o conteúdo do arquivo como lista de bytes.
     * 
     * @return Conteúdo do arquivo
     */
    public List<Byte> getConteudo() {
        return conteudo;
    }

    /**
     * Limpa todo o conteúdo do arquivo.
     */
    public void limparConteudo() {
        conteudo.clear();
    }

    /**
     * Adiciona um byte ao conteúdo do arquivo.
     * 
     * @param b Byte a ser adicionado
     */
    public void adicionarByte(byte b) {
        conteudo.add(b);
    }
}
