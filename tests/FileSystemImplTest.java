package tests;

import exception.CaminhoJaExistenteException;
import exception.CaminhoNaoEncontradoException;
import exception.PermissaoException;
import filesys.FileSystemImpl;
import filesys.Offset;
import filesys.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class FileSystemImplTest {

    private FileSystemImpl fs;
    private Usuario root;

    @BeforeEach
    void setUp() {
        root = new Usuario("root", "rwx", "/");
        fs = new FileSystemImpl(Arrays.asList(root));
    }

    /**
     * Testa a criação de diretórios e arquivos.
     * Verifica se mkdir e touch funcionam e se mkdir é idempotente.
     */
    @Test
    void testMkdirAndTouch() throws Exception {
        fs.mkdir("/home/root/docs", "root");
        fs.touch("/home/root/docs/file.txt", "root");
        assertDoesNotThrow(() -> fs.mkdir("/home/root/docs", "root"));
    }

    /**
     * Testa se o touch lança exceção ao tentar criar arquivo sem permissão de escrita.
     */
    @Test
    void testTouchNoWritePermission() throws Exception {
        fs.mkdir("/docs", "root");
        fs.chmod("/docs", "root", "root", "---");
        // Tenta criar arquivo como maria (não-root)
        assertThrows(PermissaoException.class, () -> fs.touch("/docs/file.txt", "maria"));
    }

    /**
     * Testa escrita e leitura de arquivos.
     * Verifica se o conteúdo lido é igual ao escrito.
     */
    @Test
    void testWriteAndReadFile() throws Exception {
        fs.mkdir("/docs", "root");
        fs.touch("/docs/file.txt", "root");
        byte[] data = "hello world".getBytes();
        fs.write("/docs/file.txt", "root", false, data);

        byte[] buffer = new byte[20];
        Offset offset = new Offset();
        fs.read("/docs/file.txt", "root", buffer, offset);
        String read = new String(buffer, 0, "hello world".length());
        assertEquals("hello world", read);
    }

    /**
     * Testa se a escrita falha quando não há permissão de escrita.
     */
    @Test
    void testWriteNoPermission() throws Exception {
        fs.mkdir("/docs", "root");
        fs.touch("/docs/file.txt", "root");
        fs.chmod("/docs/file.txt", "root", "root", "r--"); // remove permissão de escrita

        // Tenta escrever como maria (não-root, não-dono)
        assertThrows(PermissaoException.class, () -> {
            fs.write("/docs/file.txt", "maria", false, "fail".getBytes());
        });
    }

    /**
     * Testa se a leitura falha quando não há permissão de leitura.
     */
    @Test
        void testReadNoPermission() throws Exception {
            fs.mkdir("/docs", "root");
            fs.touch("/docs/file.txt", "root");
            fs.write("/docs/file.txt", "root", false, "abc".getBytes());

            // Remove todas as permissões do arquivo
            fs.chmod("/docs/file.txt", "root", "root", "---");

            // Tenta ler como um usuário não-root (deve lançar PermissaoException)
            assertThrows(PermissaoException.class, () -> {
                fs.read("/docs/file.txt", "maria", new byte[10], new Offset());
            });
}

    /**
     * Testa remoção de arquivos e diretórios.
     * Verifica se a remoção de um diretório inexistente lança exceção.
     */
    @Test
    void testRmFileAndDirectory() throws Exception {
        fs.mkdir("/tmp", "root");
        fs.touch("/tmp/file.txt", "root");
        fs.rm("/tmp/file.txt", "root", false);
        fs.rm("/tmp", "root", false);
        assertThrows(CaminhoNaoEncontradoException.class, () -> fs.rm("/tmp", "root", false));
    }

    /**
     * Testa se a remoção de diretório não vazio sem recursão lança exceção.
     */
    @Test
    void testRmNonEmptyDirWithoutRecursive() throws Exception {
        fs.mkdir("/dir", "root");
        fs.touch("/dir/file.txt", "root");
        assertThrows(PermissaoException.class, () -> fs.rm("/dir", "root", false));
    }

    /**
     * Testa remoção recursiva de diretório não vazio.
     */
    @Test
    void testRmNonEmptyDirWithRecursive() throws Exception {
        fs.mkdir("/dir", "root");
        fs.touch("/dir/file.txt", "root");
        fs.rm("/dir", "root", true);
        assertThrows(CaminhoNaoEncontradoException.class, () -> fs.rm("/dir", "root", false));
    }

    /**
     * Testa chmod e permissões de escrita.
     */
    @Test
    void testChmodAndPermissions() throws Exception {
        fs.mkdir("/docs", "root");
        fs.touch("/docs/file.txt", "root");
        fs.chmod("/docs/file.txt", "root", "root", "rw-");
        fs.write("/docs/file.txt", "root", false, "ok".getBytes());
    }

    /**
     * Testa se ls lista o conteúdo do diretório sem lançar exceção.
     */
    @Test
    void testLsListsContents() throws Exception {
        fs.mkdir("/dir", "root");
        fs.touch("/dir/file1.txt", "root");
        fs.touch("/dir/file2.txt", "root");
        assertDoesNotThrow(() -> fs.ls("/dir", "root", false));
    }

    /**
     * Testa o comando mv para mover e renomear arquivos.
     */
    @Test
    void testMvFile() throws Exception {
        // Cria diretório e arquivo
        fs.mkdir("/docs", "root");
        fs.touch("/docs/file.txt", "root");
        // Move arquivo para novo nome
        fs.mv("/docs/file.txt", "/docs/file2.txt", "root");
        // Verifica se o arquivo antigo não existe mais e o novo existe
        assertThrows(CaminhoNaoEncontradoException.class, () -> fs.ls("/docs/file.txt", "root", false));
        assertDoesNotThrow(() -> fs.ls("/docs/file2.txt", "root", false));
    }

    /**
     * Testa o comando cp para copiar arquivos.
     */
    @Test
    void testCpFile() throws Exception {
        fs.mkdir("/docs", "root");
        fs.touch("/docs/file.txt", "root");
        fs.write("/docs/file.txt", "root", false, "abc".getBytes());
        fs.cp("/docs/file.txt", "/docs/file2.txt", "root", false);

        byte[] buffer = new byte[10];
        Offset offset = new Offset();
        fs.read("/docs/file2.txt", "root", buffer, offset);
        String read = new String(buffer, 0, 3);
        assertEquals("abc", read);
    }

    /**
     * Testa o comando cp para copiar diretórios recursivamente.
     */
    @Test
    void testCpDirectoryRecursive() throws Exception {
        fs.mkdir("/dir", "root");
        fs.touch("/dir/file.txt", "root");
        fs.write("/dir/file.txt", "root", false, "xyz".getBytes());
        fs.cp("/dir", "/dir2", "root", true);

        byte[] buffer = new byte[10];
        Offset offset = new Offset();
        fs.read("/dir2/file.txt", "root", buffer, offset);
        String read = new String(buffer, 0, 3);
        assertEquals("xyz", read);
    }

    /**
     * Testa se a cópia de diretório sem recursão lança exceção.
     */
    @Test
    void testCpDirectoryWithoutRecursiveThrows() throws Exception {
        fs.mkdir("/dir", "root");
        fs.touch("/dir/file.txt", "root");
        assertThrows(PermissaoException.class, () -> fs.cp("/dir", "/dir2", "root", false));
    }
}