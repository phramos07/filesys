package filesys;

import exception.CaminhoJaExistenteException;
import exception.CaminhoNaoEncontradoException;
import exception.PermissaoException;

// Apenas modifique essa interface caso seja EXTREMAMENTE necessário.
// Documente & Justifique TODAS asalterações feitas.
public interface IFileSystem {

    // Adiciona um novo usuário ao sistema de arquivos.
    // O usuário deve ter um nome, um diretório inicial e permissões.
    public void addUser(String nome, String diretorio, String permissoes)throws CaminhoNaoEncontradoException;

    // Remove um usuário do sistema de arquivos.
    // O usuário deve ser o dono do diretório que está tentando remover.
    public void removeUser(String nome) throws CaminhoNaoEncontradoException, PermissaoException;

    // Altera as permissões de um arquivo ou diretório.
    // Configura a permissao do caminho para o usuarioAlvo.
    // Apenas o usuario root ou que tenha permissão de rw do caminho podem alterar
    // as permissões.
    void chmod(String caminho, String usuario, String usuarioAlvo, String permissao)
            throws CaminhoNaoEncontradoException, PermissaoException;

    // Cria um novo diretório. Se o diretório já existir, será lançada uma exceção.
    // Se o usuario não tiver permissão para criar o diretório, será lançada uma
    // exceção.
    void mkdir(String caminho, String usuario)
            throws CaminhoJaExistenteException, PermissaoException, CaminhoNaoEncontradoException;

    // Remove um arquivo ou diretório. Se o diretório não existir, será lançada uma
    // exceção.
    // Caso recursivo seja true, o diretório será removido recursivamente.
    void rm(String caminho, String usuario, boolean recursivo) throws CaminhoNaoEncontradoException, PermissaoException;

    // Cria um novo arquivo. Atenção: Se o arquivo já existir, será lançada uma
    // exceção.
    void touch(String caminho, String usuario)
            throws CaminhoJaExistenteException, PermissaoException, CaminhoNaoEncontradoException;

    // Escreve dados em um arquivo. Se o diretório não existir, será lançada uma
    // exceção.
    // Caso o diretório exista, o arquivo será criado ou sobrescrito.
    // Caso anexar (append) seja true, os dados serão adicionados ao final do
    // arquivo.
    // Escrita sequencial.
    void write(String caminho, String usuario, boolean anexar, Offset offset, byte[] buffer)
            throws CaminhoNaoEncontradoException, PermissaoException;

    // Lê dados de um arquivo. Se o arquivo não existir, será lançada uma exceção.
    // Leitura sequencial - todo o conteudo do arquivo sera lido e armazenado no
    // buffer.
    void read(String caminho, String usuario, byte[] buffer) throws CaminhoNaoEncontradoException, PermissaoException;

    // Move ou renomeia um arquivo ou diretório. Se o diretório não existir, será
    // lançada uma exceção.
    // Se o diretório já existir, será sobrescrito.
    // mv é naturalmente recursivo.
    void mv(String caminhoAntigo, String caminhoNovo, String usuario)
            throws CaminhoNaoEncontradoException, PermissaoException;

    // Lista o conteúdo de um diretório. Se o diretório não existir, será lançada
    // uma exceção.
    // Caso recursivo seja true, todo o conteúdo do diretório será listado
    // recursivamente.
    void ls(String caminho, String usuario, boolean recursivo) throws CaminhoNaoEncontradoException, PermissaoException;

    // Copia um arquivo ou diretório. Se o diretório não existir, será lançada uma
    // exceção.
    void cp(String caminhoOrigem, String caminhoDestino, String usuario, boolean recursivo)
            throws CaminhoNaoEncontradoException, PermissaoException;
}