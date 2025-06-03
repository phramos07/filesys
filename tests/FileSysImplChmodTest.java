package tests;
import filesys.Arquivo;
import filesys.Diretorio;
import filesys.FileSystemImpl;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import exception.CaminhoNaoEncontradoException;
import exception.PermissaoException;

class FileSystemImplChmodTest {

    private FileSystemImpl fsImpl;

    @BeforeEach
    void setUp() {
        fsImpl = new FileSystemImpl();
        // Opcional: podemos já criar uma estrutura de diretórios com subpastas e arquivos
        // para os testes. Por enquanto, deixamos vazio e cada teste monta o que precisa.
    }

    /**
     * 1) Sucesso no chmod quando quem executa é root (criaremos /d1 e um arquivo lá):
     *    - mkdir("/", "d1")
     *    - touch("/d1/arquivo.log", "root")
     *    - chmod("/d1/arquivo.log", "root", "alice", "r--")
     *    → Verifica que em /d1/arquivo.log existe permissão "r--" para "alice"
     */
    @Test
    void testChmodByRootSuccess() throws Exception {
        // Cria diretório e arquivo
        fsImpl.mkdir("/", "d1");
        fsImpl.touch("/d1/arquivo.log", "root");

        // Executa chmod como root, conferindo permissão de 'r--' para usuário 'alice'
        fsImpl.chmod("/d1/arquivo.log", "root", "alice", "r--");

        // Localiza o Arquivo para verificar MetaDados
        Diretorio d1 = (Diretorio) fsImpl.getFileSys().getRaiz()
                          .getSubDirs().stream()
                          .filter(d -> d.getMetaDados().getNome().equals("d1"))
                          .findFirst().orElse(null);
        assertNotNull(d1, "Diretório /d1 deveria existir.");

        Arquivo arqLog = d1.getArquivos().stream()
                           .filter(a -> a.getMetaDados().getNome().equals("arquivo.log"))
                           .findFirst().orElse(null);
        assertNotNull(arqLog, "Arquivo /d1/arquivo.log deveria existir.");

        // Verifica permissão para "alice"
        assertTrue(arqLog.getMetaDados().hasPermissao("alice", "r"),
                   "Alice deve ter permissão de leitura ('r').");
        assertFalse(arqLog.getMetaDados().hasPermissao("alice", "w"),
                    "Alice não deve ter permissão de escrita ('w').");
        assertFalse(arqLog.getMetaDados().hasPermissao("alice", "x"),
                    "Alice não deve ter permissão de execução ('x').");
    }

    /**
     * 2) Sucesso no chmod quando quem executa é o dono do objeto:
     *    - mkdir("/", "d2")
     *    - touch("/d2/file.dat", "bob")
     *    - chmod("/d2/file.dat", "bob", "charlie", "rw-")
     *    → Verifica que 'charlie' tem agora permissão "rw-"
     */
    @Test
    void testChmodByOwnerSuccess() throws Exception {
        // Cria d2 e, como usuário 'bob', cria o arquivo dentro
        fsImpl.mkdir("/", "d2");
        fsImpl.touch("/d2/file.dat", "bob");

        // Agora 'bob' altera permissão de 'charlie' para "rw-"
        fsImpl.chmod("/d2/file.dat", "bob", "charlie", "rw-");

        // Localiza file.dat
        Diretorio d2 = (Diretorio) fsImpl.getFileSys().getRaiz()
                          .getSubDirs().stream()
                          .filter(d -> d.getMetaDados().getNome().equals("d2"))
                          .findFirst().orElse(null);
        assertNotNull(d2, "Diretório /d2 deveria existir.");

        Arquivo fileDat = d2.getArquivos().stream()
                            .filter(a -> a.getMetaDados().getNome().equals("file.dat"))
                            .findFirst().orElse(null);
        assertNotNull(fileDat, "Arquivo /d2/file.dat deveria existir.");

        // Verifica permissão de charlie
        assertTrue(fileDat.getMetaDados().hasPermissao("charlie", "r"),
                   "Charlie deve ter permissão de leitura ('r').");
        assertTrue(fileDat.getMetaDados().hasPermissao("charlie", "w"),
                   "Charlie deve ter permissão de escrita ('w').");
        assertFalse(fileDat.getMetaDados().hasPermissao("charlie", "x"),
                    "Charlie não deve ter permissão de execução ('x').");
    }

    /**
     * 3) Falha se o caminho não existir → CaminhoNaoEncontradoException
     *    - chmod("/inexistente/arquivo", "root", "any", "rwx")
     */
    @Test
    void testChmodPathNotFound() {
        CaminhoNaoEncontradoException ex = assertThrows(
            CaminhoNaoEncontradoException.class,
            () -> fsImpl.chmod("/inexistente/arquivo", "root", "any", "rwx")
        );
        assertTrue(ex.getMessage().contains("Componente não encontrado") ||
                   ex.getMessage().contains("Caminho inválido"),
                   "Ao tentar chmod em caminho inexistente, deve lançar CaminhoNaoEncontradoException.");
    }

    /**
     * 4) Falha se usuário não for root e nem dono → PermissaoException
     *    - mkdir("/", "d3")
     *    - touch("/d3/foo", "alice")
     *    - chmod("/d3/foo", "bob", "someUser", "r-x")  // 'bob' não é root nem dono
     */
    @Test
    void testChmodNoPermission() throws Exception {
        // Cria o diretório /d3 e o arquivo como 'alice'
        fsImpl.mkdir("/", "d3");
        fsImpl.touch("/d3/foo", "alice");

        // Agora 'bob' (nem root, nem dono) tenta alterar:
        PermissaoException pe = assertThrows(
            PermissaoException.class,
            () -> fsImpl.chmod("/d3/foo", "bob", "eve", "r-x")
        );
        assertEquals(
            "Usuário 'bob' não tem permissão para alterar direitos em: /d3/foo",
            pe.getMessage(),
            "Quando usuário não é root nem dono, deve lançar PermissaoException."
        );
    }

    /**
     * 5) Se a string de permissão for inválida (ex: "abx" ou tamanho != 3), deve lançar IllegalArgumentException
     *    - mkdir("/", "d4")
     *    - touch("/d4/bar", "alice")
     *    - chmod("/d4/bar", "alice", "mallory", "abx")
     */
    @Test
    void testChmodInvalidPermissionString() throws Exception {
        // Cria /d4/bar
        fsImpl.mkdir("/", "d4");
        fsImpl.touch("/d4/bar", "alice");

        IllegalArgumentException iae = assertThrows(
            IllegalArgumentException.class,
            () -> fsImpl.chmod("/d4/bar", "alice", "mallory", "abx")
        );
        assertTrue(iae.getMessage().contains("Permissão inválida"),
                   "Quando a string de permissão conter chars inválidos, deve lançar IllegalArgumentException.");

        // Também testa tamanho diferente de 3
        IllegalArgumentException iae2 = assertThrows(
            IllegalArgumentException.class,
            () -> fsImpl.chmod("/d4/bar", "alice", "mallory", "rw")
        );
        assertTrue(iae2.getMessage().contains("Permissão inválida"),
                   "Quando a string de permissão tiver tamanho != 3, deve lançar IllegalArgumentException.");
    }
}
