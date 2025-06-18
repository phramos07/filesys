package tests;

import filesys.FileSystem;
import filesys.FileSystemImpl;
import filesys.IFileSystem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import exception.CaminhoJaExistenteException;
import exception.CaminhoNaoEncontradoException;
import exception.PermissaoException;

import java.nio.charset.StandardCharsets;

// Testes principais de permissão do sistema de arquivos
public class PermissionTest {
    private IFileSystem fileSystem;
    private FileSystemImpl fileSystemImplRef;

    @BeforeEach
    public void setUp() {
        FileSystem proxy = new FileSystem();
        fileSystem = proxy;
        fileSystemImplRef = (FileSystemImpl) proxy.fileSystemImpl;
    }

    @Test
    public void testRootHasFullPermission() throws CaminhoJaExistenteException, PermissaoException, CaminhoNaoEncontradoException {
        // root pode criar, alterar permissões, ler e escrever em qualquer lugar
        assertTrue(true);

        fileSystem.mkdir("/testdir", "root");
        fileSystem.touch("/testfile.txt", "root");

        fileSystem.touch("/otherfile.txt", "user1");

        // user1 pode mudar permissão de 'other' no seu próprio arquivo
        assertDoesNotThrow(() -> {
            fileSystem.chmod("/otherfile.txt", "user1", "other", "---");
        });

        // root pode mudar permissões de qualquer arquivo
        assertDoesNotThrow(() -> {
            fileSystem.chmod("/otherfile.txt", "root", "user1", "rwx");
        });
        assertDoesNotThrow(() -> {
            fileSystem.chmod("/otherfile.txt", "root", "other", "r--");
        });

        String content = "Hello World";
        byte[] buffer = content.getBytes(StandardCharsets.UTF_8);
        fileSystem.write("/testfile.txt", "root", false, buffer);

        byte[] readBuffer = new byte[buffer.length];
        fileSystem.read("/testfile.txt", "root", readBuffer);

        assertEquals(content, new String(readBuffer, StandardCharsets.UTF_8));
    }

    @Test
    public void testUserCannotWriteWithoutPermission() throws CaminhoJaExistenteException, PermissaoException, CaminhoNaoEncontradoException {
        fileSystem.mkdir("/public", "root");
        fileSystem.chmod("/public", "root", "other", "r-x");

        String normalUser = "joao";

        // Usuário sem permissão de escrita não pode criar arquivo
        assertThrows(PermissaoException.class, () -> {
            fileSystem.touch("/public/joaofile.txt", normalUser);
        });

        fileSystem.mkdir("/joao_dir", normalUser);
        fileSystem.chmod("/joao_dir", normalUser, "other", "r--");

        String outroUser = "maria";
        assertThrows(PermissaoException.class, () -> {
            fileSystem.touch("/joao_dir/maria_file.txt", outroUser);
        });
    }

    @Test
    public void testChmodPermissions() throws CaminhoNaoEncontradoException, PermissaoException, CaminhoJaExistenteException {
        fileSystem.touch("/file_perms.txt", "root");

        // root pode mudar permissões para qualquer usuário
        assertDoesNotThrow(() -> {
            fileSystem.chmod("/file_perms.txt", "root", "userA", "rw-");
        });

        String userA = "userA";
        byte[] data = "Hello from userA".getBytes(StandardCharsets.UTF_8);
        assertDoesNotThrow(() -> {
            fileSystem.write("/file_perms.txt", userA, false, data);
        });

        assertDoesNotThrow(() -> {
            fileSystem.chmod("/file_perms.txt", "root", userA, "r--");
        });

        // userA não pode mais escrever após permissão removida
        assertThrows(PermissaoException.class, () -> {
            fileSystem.write("/file_perms.txt", userA, false, "New data".getBytes(StandardCharsets.UTF_8));
        });

        fileSystem.touch("/another_file.txt", "userB");
        String userC = "userC";
        // Apenas root ou dono pode mudar permissões
        assertThrows(PermissaoException.class, () -> {
            fileSystem.chmod("/another_file.txt", userC, "userB", "rwx");
        });
    }

    @Test
    public void testRmPermissions() throws CaminhoJaExistenteException, PermissaoException, CaminhoNaoEncontradoException {
        fileSystem.mkdir("/parent_dir", "root");
        fileSystem.touch("/parent_dir/file_to_remove.txt", "root");

        String userB = "userB";
        // userB não pode remover arquivo sem permissão de escrita no diretório pai
        assertThrows(PermissaoException.class, () -> {
            fileSystem.rm("/parent_dir/file_to_remove.txt", userB, false);
        });

        fileSystem.chmod("/parent_dir", "root", "other", "r-x");

        fileSystem.mkdir("/userB_home", userB);
        fileSystem.touch("/userB_home/my_file.txt", userB);

        assertDoesNotThrow(() -> {
            fileSystem.rm("/userB_home/my_file.txt", userB, false);
        });

        fileSystem.mkdir("/non_empty_dir", "root");
        fileSystem.touch("/non_empty_dir/a.txt", "root");
        // Não pode remover diretório não vazio sem recursivo
        assertThrows(PermissaoException.class, () -> {
            fileSystem.rm("/non_empty_dir", "root", false);
        });

        // Pode remover diretório não vazio com recursivo
        assertDoesNotThrow(() -> {
            fileSystem.rm("/non_empty_dir", "root", true);
        });
    }

    @Test
    public void testMvPermissions() throws CaminhoJaExistenteException, PermissaoException, CaminhoNaoEncontradoException {
        fileSystem.mkdir("/dir1", "root");
        fileSystem.mkdir("/dir2", "root");
        fileSystem.touch("/dir1/file1.txt", "root");

        String userC = "userC";

        // userC não pode mover arquivo sem permissão de escrita no diretório de origem
        assertThrows(PermissaoException.class, () -> {
            fileSystem.mv("/dir1/file1.txt", "/dir2/file1.txt", userC);
        });

        fileSystem.touch("/dir1/file_userC.txt", userC);
        fileSystem.chmod("/dir1", "root", userC, "rwx");
        fileSystem.chmod("/dir2", "root", userC, "rwx");

        assertDoesNotThrow(() -> {
            fileSystem.mv("/dir1/file_userC.txt", "/dir2/moved_file.txt", userC);
        });

        // Não pode mover a raiz
        assertThrows(PermissaoException.class, () -> {
            fileSystem.mv("/", "/new_root", "root");
        });
    }

    @Test
    public void testCpPermissions() throws CaminhoJaExistenteException, PermissaoException, CaminhoNaoEncontradoException {
        fileSystem.mkdir("/source_dir", "root");
        fileSystem.mkdir("/dest_dir", "root");
        fileSystem.touch("/source_dir/file_orig.txt", "root");

        String userD = "userD";

        // userD não pode copiar arquivo sem permissão de leitura
        assertThrows(PermissaoException.class, () -> {
            fileSystem.cp("/source_dir/file_orig.txt", "/dest_dir/copy.txt", userD, false);
        });

        fileSystem.chmod("/source_dir/file_orig.txt", "root", "other", "r--");
        fileSystem.chmod("/source_dir", "root", "other", "r-x");
        fileSystem.chmod("/dest_dir", "root", "other", "r-x");
        assertThrows(PermissaoException.class, () -> {
            fileSystem.cp("/source_dir/file_orig.txt", "/dest_dir/copy.txt", userD, false);
        });

        fileSystem.chmod("/dest_dir", "root", userD, "rwx");

        assertDoesNotThrow(() -> {
            fileSystem.cp("/source_dir/file_orig.txt", "/dest_dir/copy_by_userD.txt", userD, false);
        });
    }

    @Test
    public void testReadWritePermissions() throws CaminhoJaExistenteException, PermissaoException, CaminhoNaoEncontradoException {
        fileSystem.touch("/private_file.txt", "root");
        String content = "Secret data";
        byte[] originalContentBytes = content.getBytes(StandardCharsets.UTF_8);
        fileSystem.write("/private_file.txt", "root", false, originalContentBytes);

        String hacker = "hacker";
        byte[] readBuffer = new byte[originalContentBytes.length];

        // Usuário sem permissão não pode ler nem escrever
        assertThrows(PermissaoException.class, () -> {
            fileSystem.read("/private_file.txt", hacker, readBuffer);
        });

        assertThrows(PermissaoException.class, () -> {
            fileSystem.write("/private_file.txt", hacker, false, "Malicious code".getBytes(StandardCharsets.UTF_8));
        });

        fileSystem.chmod("/private_file.txt", "root", "other", "r--");
        assertDoesNotThrow(() -> {
            fileSystem.read("/private_file.txt", hacker, readBuffer);
        });
        assertEquals(content, new String(readBuffer, StandardCharsets.UTF_8));
    }
}