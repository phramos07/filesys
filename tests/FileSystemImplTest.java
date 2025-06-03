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

    @Test
    void testMkdirAndTouch() throws Exception {
        fs.mkdir("/home/root/docs", "root");
        fs.touch("/home/root/docs/file.txt", "root");
        assertDoesNotThrow(() -> fs.mkdir("/home/root/docs", "root"));
    }

    @Test
    void testTouchNoWritePermission() throws Exception {
        fs.mkdir("/docs", "root");
        fs.chmod("/docs", "root", "root", "---");
        assertThrows(PermissaoException.class, () -> fs.touch("/docs/file.txt", "root"));
    }

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

    @Test
    void testWriteNoPermission() throws Exception {
        fs.mkdir("/docs", "root");
        fs.touch("/docs/file.txt", "root");
        fs.chmod("/docs/file.txt", "root", "root", "r--");
        assertThrows(PermissaoException.class, () -> fs.write("/docs/file.txt", "root", false, "fail".getBytes()));
    }

    @Test
    void testReadNoPermission() throws Exception {
        fs.mkdir("/docs", "root");
        fs.touch("/docs/file.txt", "root");
        fs.write("/docs/file.txt", "root", false, "abc".getBytes());
        fs.chmod("/docs/file.txt", "root", "root", "---");
        assertThrows(PermissaoException.class, () -> fs.read("/docs/file.txt", "root", new byte[10], new Offset()));
    }

    @Test
    void testRmFileAndDirectory() throws Exception {
        fs.mkdir("/tmp", "root");
        fs.touch("/tmp/file.txt", "root");
        fs.rm("/tmp/file.txt", "root", false);
        fs.rm("/tmp", "root", false);
        assertThrows(CaminhoNaoEncontradoException.class, () -> fs.rm("/tmp", "root", false));
    }

    @Test
    void testRmNonEmptyDirWithoutRecursive() throws Exception {
        fs.mkdir("/dir", "root");
        fs.touch("/dir/file.txt", "root");
        assertThrows(PermissaoException.class, () -> fs.rm("/dir", "root", false));
    }

    @Test
    void testRmNonEmptyDirWithRecursive() throws Exception {
        fs.mkdir("/dir", "root");
        fs.touch("/dir/file.txt", "root");
        fs.rm("/dir", "root", true);
        assertThrows(CaminhoNaoEncontradoException.class, () -> fs.rm("/dir", "root", false));
    }

    @Test
    void testChmodAndPermissions() throws Exception {
        fs.mkdir("/docs", "root");
        fs.touch("/docs/file.txt", "root");
        fs.chmod("/docs/file.txt", "root", "root", "rw-");
        fs.write("/docs/file.txt", "root", false, "ok".getBytes());
    }

    @Test
    void testLsListsContents() throws Exception {
        fs.mkdir("/dir", "root");
        fs.touch("/dir/file1.txt", "root");
        fs.touch("/dir/file2.txt", "root");
        assertDoesNotThrow(() -> fs.ls("/dir", "root", false));
    }
}