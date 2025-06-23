package filesys;

import exception.CaminhoJaExistenteException;
import exception.CaminhoNaoEncontradoException;
import exception.OperacaoInvalidaException;
import exception.PermissaoException;

/**
 * Interface para as operações de um sistema de arquivos virtual.
 * 
 *  IFyleSystem é implementada por FileSystemImpl, que contém a lógica de manipulação de arquivos e diretórios e FyleSystem atua como um proxy que delega as chamadas para a implementação real.
 *  Essa interface define os métodos necessários para gerenciar usuários, permissões e operações de arquivos.
 *  O sistema de arquivos é inicializado com um diretório raiz padrão ("/") e um usuário padrão ("root")
 *  Não é necessário passar parâmetros para o construtor e nem adicionar o diretório e o usuário root >
 *  O usuário root não pode ser removido.
 */
public interface IFileSystem {

    /**
     * Adiciona um novo usuário ao sistema.
     * 
     * @param user Usuário a ser adicionado.
     */
    void addUser(Usuario user);

    /**
     * Remove um usuário do sistema.
     * 
     * O usuário root não pode ser removido.
     * 
     * @param username Nome do usuário a ser removido.
     */
    void removeUser(String username);

    /**
     * Altera as permissões de um caminho para um usuário específico.
     * 
     *
     * Apenas o usuário root ou aquele com permissão 
     * rw no caminho pode alterar permissões.
     *
     * @param caminho Caminho do arquivo ou diretório.
     * @param usuario Usuário que está realizando a operação.
     * @param usuarioAlvo Usuário cujas permissões serão alteradas.
     * @param permissao Novas permissões (ex: "rwx").
     * @throws CaminhoNaoEncontradoException Se o caminho não existir.
     * @throws PermissaoException Se o usuário não tiver permissão para modificar.
     */
    void chmod(String caminho, String usuario, String usuarioAlvo, String permissao)
            throws CaminhoNaoEncontradoException, PermissaoException;

    /**
     * Cria um novo diretório.
     * 
     * @param caminho Caminho completo do diretório a ser criado.
     * @param usuario Usuário que está criando o diretório.
     * @throws CaminhoJaExistenteException Se o diretório já existir.
     * @throws PermissaoException Se o usuário não tiver permissão para criar o diretório.
     * @throws CaminhoNaoEncontradoException Se o caminho pai não existir.
     * @throws OperacaoInvalidaException 
     */
    void mkdir(String caminho, String usuario)
            throws CaminhoJaExistenteException, PermissaoException, OperacaoInvalidaException;

    /**
     * Remove um arquivo ou diretório.
     * 
     * @param caminho Caminho do arquivo ou diretório a ser removido.
     * @param usuario Usuário que realiza a remoção.
     * @param recursivo Se <code>true</code>, remove diretórios recursivamente.
     * @throws CaminhoNaoEncontradoException Se o caminho não existir.
     * @throws PermissaoException Se o usuário não tiver permissão de remoção.
     */
    void rm(String caminho, String usuario, boolean recursivo)
            throws CaminhoNaoEncontradoException, PermissaoException;

    /**
     * Cria um novo arquivo vazio.
     * 
     * Se o arquivo já existir, será lançada uma exceção.
     *
     * @param caminho Caminho do arquivo.
     * @param usuario Usuário que está criando o arquivo.
     * @throws CaminhoJaExistenteException Se o arquivo já existir.
     * @throws PermissaoException Se o usuário não tiver permissão de criação.
     * @throws CaminhoNaoEncontradoException Se o caminho pai não existir.
     * @throws OperacaoInvalidaException 
     */
    void touch(String caminho, String usuario)
            throws CaminhoJaExistenteException, PermissaoException, CaminhoNaoEncontradoException, OperacaoInvalidaException;

    /**
     * Escreve dados em um arquivo.
     * 
     * 
     * Caso o arquivo não exista, será lançado um erro.
     * Se anexar for true, os dados serão adicionados ao final do arquivo.
     * 
     *
     * @param caminho Caminho do arquivo.
     * @param usuario Usuário que está escrevendo.
     * @param anexar Se <code>true</code>, realiza operação de append.
     * @param buffer Dados a serem escritos.
     * @throws CaminhoNaoEncontradoException Se o arquivo não existir.
     * @throws PermissaoException Se o usuário não tiver permissão de escrita.
     * @throws OperacaoInvalidaException 
     */
    void write(String caminho, String usuario, boolean anexar, byte[] buffer)
            throws CaminhoNaoEncontradoException, PermissaoException, OperacaoInvalidaException;

    /**
     * Lê dados de um arquivo sequencialmente.
     * 
     *Todo o conteúdo do arquivo será lido a partir de um Offset e armazenado no buffer.
     * 
     *
     * @param caminho Caminho do arquivo.
     * @param usuario Usuário que está lendo.
     * @param buffer Buffer onde os dados serão armazenados.
     * @param offset Objeto que representa o deslocamento atual da leitura.
     * @throws CaminhoNaoEncontradoException Se o arquivo não existir.
     * @throws PermissaoException Se o usuário não tiver permissão de leitura.
     * @throws OperacaoInvalidaException 
     */
    void read(String caminho, String usuario, byte[] buffer, Offset offset)
            throws CaminhoNaoEncontradoException, PermissaoException, OperacaoInvalidaException;

    /**
     * Move ou renomeia um arquivo ou diretório.
     * 
     * 
     * A operação é recursiva. Se o destino já existir, será sobrescrito.
     * 
     *
     * @param caminhoAntigo Caminho original.
     * @param caminhoNovo Novo caminho.
     * @param usuario Usuário que está realizando a operação.
     * @throws CaminhoNaoEncontradoException Se o caminho antigo não existir.
     * @throws PermissaoException Se o usuário não tiver permissão.
     */
    void mv(String caminhoAntigo, String caminhoNovo, String usuario)
            throws CaminhoNaoEncontradoException, PermissaoException;

    /**
     * Lista o conteúdo de um diretório.
     * 
     * @param caminho Caminho do diretório.
     * @param usuario Usuário que está acessando.
     * @param recursivo Se true, lista recursivamente os subdiretórios.
     * @throws CaminhoNaoEncontradoException Se o diretório não existir.
     * @throws PermissaoException Se o usuário não tiver permissão de leitura.
     */
    void ls(String caminho, String usuario, boolean recursivo)
            throws CaminhoNaoEncontradoException, PermissaoException;

    /**
     * Copia um arquivo ou diretório.
     * 
     * @param caminhoOrigem Caminho de origem.
     * @param caminhoDestino Caminho de destino.
     * @param usuario Usuário que está realizando a operação.
     * @param recursivo Se true, copia diretórios recursivamente.
     * @throws CaminhoNaoEncontradoException Se a origem não existir.
     * @throws PermissaoException Se o usuário não tiver permissão.
     */
    void cp(String caminhoOrigem, String caminhoDestino, String usuario, boolean recursivo)
            throws CaminhoNaoEncontradoException, PermissaoException;
}