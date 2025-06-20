package filesys;

/**
 * Representa um bloco de dados no sistema de arquivos virtual.
 * Pode ser utilizado para armazenar partes do conteúdo de arquivos grandes.
 *
 * <p>
 * Blocos podem ser usados para implementar escrita/leitura em chunks.
 * </p>
 *
 * @author SeuNome
 */
public class Bloco {
    /** Dados armazenados neste bloco. */
    private Byte[] dados;

    /**
     * Construtor para criar um bloco vazio.
     */
    public Bloco() {
        this.dados = new Byte[1024]; // Tamanho padrão de 1KB
    }

    /**
     * Construtor para criar um bloco com dados específicos.
     *
     * @param dados Dados a serem armazenados no bloco.
     */

    public Bloco(Byte[] dados) {
        this.dados = dados;
    }   

    /**
     * Obtém os dados armazenados neste bloco.
     *
     * @return Dados do bloco.
     */
    public Byte[] getDados() {
        return dados;
    }
    
}
