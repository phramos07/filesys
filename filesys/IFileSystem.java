package filesys;

import exception.CaminhoJaExistenteException;
import exception.CaminhoNaoEncontradoException;
import exception.PermissaoException;

/**
 * Interface que define as operações de um sistema de arquivos virtual.
 * 
 * <p>
 * Inclui métodos para manipulação de arquivos, diretórios, permissões e
 * usuários.
 * </p>
 *
 * <p>
 * Implementações devem garantir a integridade, segurança e controle de
 * permissões conforme descrito.
 * </p>
 *
 * <b>Modifique esta interface apenas se for extremamente necessário e documente
 * todas as alterações.</b>
 *
 * @author SeuNome
 */
public interface IFileSystem {

        /**
         * Adiciona um novo usuário ao sistema de arquivos.
         * 
         * @param nome       Nome do usuário
         * @param diretorio  Diretório inicial do usuário
         * @param permissoes Permissões iniciais
         * @throws CaminhoNaoEncontradoException Se o diretório não existir
         */
        public void addUser(String nome, String diretorio, String permissoes) throws CaminhoNaoEncontradoException;

        /**
         * Remove um usuário do sistema de arquivos.
         * O usuário deve ser o dono do diretório que está tentando remover.
         * 
         * @param nome Nome do usuário
         * @throws CaminhoNaoEncontradoException Se o usuário não existir
         * @throws PermissaoException            Se não tiver permissão para remover
         */
        public void removeUser(String nome) throws CaminhoNaoEncontradoException, PermissaoException;

        /**
         * Altera as permissões de um arquivo ou diretório.
         * Apenas o usuário root ou o dono podem alterar as permissões.
         * 
         * @param caminho     Caminho do arquivo ou diretório
         * @param usuario     Usuário solicitante
         * @param usuarioAlvo Usuário alvo da permissão
         * @param permissao   Permissão a ser atribuída (ex: "rwx")
         * @throws CaminhoNaoEncontradoException Se o caminho não existir
         * @throws PermissaoException            Se não tiver permissão para alterar
         */
        void chmod(String caminho, String usuario, String usuarioAlvo, String permissao)
                        throws CaminhoNaoEncontradoException, PermissaoException;

        /**
         * Cria um novo diretório. Diretórios intermediários são criados
         * automaticamente.
         * 
         * @param caminho Caminho do diretório
         * @param usuario Usuário solicitante
         * @throws CaminhoJaExistenteException   Se já existir
         * @throws PermissaoException            Se não tiver permissão
         * @throws CaminhoNaoEncontradoException Se o diretório pai não existir
         */
        void mkdir(String caminho, String usuario)
                        throws CaminhoJaExistenteException, PermissaoException, CaminhoNaoEncontradoException;

        /**
         * Remove um arquivo ou diretório. Diretórios podem ser removidos
         * recursivamente.
         * 
         * @param caminho   Caminho do arquivo/diretório
         * @param usuario   Usuário solicitante
         * @param recursivo Se true, remove recursivamente
         * @throws CaminhoNaoEncontradoException Se não existir
         * @throws PermissaoException            Se não tiver permissão
         */
        void rm(String caminho, String usuario, boolean recursivo)
                        throws CaminhoNaoEncontradoException, PermissaoException;

        /**
         * Cria um novo arquivo.
         * 
         * @param caminho Caminho do arquivo
         * @param usuario Usuário solicitante
         * @throws CaminhoJaExistenteException   Se já existir
         * @throws PermissaoException            Se não tiver permissão
         * @throws CaminhoNaoEncontradoException Se o diretório pai não existir
         */
        void touch(String caminho, String usuario)
                        throws CaminhoJaExistenteException, PermissaoException, CaminhoNaoEncontradoException;

        /**
         * Escreve dados em um arquivo. Suporta escrita sequencial e append.
         * 
         * @param caminho Caminho do arquivo
         * @param usuario Usuário solicitante
         * @param anexar  Se true, adiciona ao final do arquivo
         * @param offset  Offset inicial para escrita
         * @param buffer  Dados a serem escritos
         * @throws CaminhoNaoEncontradoException Se o arquivo não existir
         * @throws PermissaoException            Se não tiver permissão
         */
        void write(String caminho, String usuario, boolean anexar, Offset offset, byte[] buffer)
                        throws CaminhoNaoEncontradoException, PermissaoException;

        /**
         * Lê dados de um arquivo para o buffer informado.
         * 
         * @param caminho Caminho do arquivo
         * @param usuario Usuário solicitante
         * @param buffer  Buffer de leitura
         * @throws CaminhoNaoEncontradoException Se o arquivo não existir
         * @throws PermissaoException            Se não tiver permissão
         */
        void read(String caminho, String usuario, byte[] buffer)
                        throws CaminhoNaoEncontradoException, PermissaoException;

        /**
         * Move ou renomeia um arquivo ou diretório.
         * 
         * @param caminhoAntigo Caminho de origem
         * @param caminhoNovo   Caminho de destino
         * @param usuario       Usuário solicitante
         * @throws CaminhoNaoEncontradoException Se o caminho de origem não existir
         * @throws PermissaoException            Se não tiver permissão
         */
        void mv(String caminhoAntigo, String caminhoNovo, String usuario)
                        throws CaminhoNaoEncontradoException, PermissaoException;

        /**
         * Lista o conteúdo de um diretório.
         * 
         * @param caminho   Caminho do diretório
         * @param usuario   Usuário solicitante
         * @param recursivo Se true, lista recursivamente
         * @throws CaminhoNaoEncontradoException Se o diretório não existir
         * @throws PermissaoException            Se não tiver permissão
         */
        void ls(String caminho, String usuario, boolean recursivo)
                        throws CaminhoNaoEncontradoException, PermissaoException;

        /**
         * Copia um arquivo ou diretório.
         * 
         * @param caminhoOrigem  Caminho de origem
         * @param caminhoDestino Caminho de destino
         * @param usuario        Usuário solicitante
         * @param recursivo      Se true, copia recursivamente
         * @throws CaminhoNaoEncontradoException Se o caminho de origem não existir
         * @throws PermissaoException            Se não tiver permissão
         */
        void cp(String caminhoOrigem, String caminhoDestino, String usuario, boolean recursivo)
                        throws CaminhoNaoEncontradoException, PermissaoException;
}