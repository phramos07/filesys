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
}
