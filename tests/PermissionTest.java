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
        assertDoesNotThrow(() -> fileSystem.mkdir("/testdir_root", "root"));
        assertDoesNotThrow(() -> fileSystem.touch("/testfile_root.txt", "root"));
        assertDoesNotThrow(() -> fileSystem.mkdir("/home_user1", "root"));
        assertDoesNotThrow(() -> fileSystem.chmod("/home_user1", "root", "user1", "rwx"));
        String user1 = "user1";
        assertDoesNotThrow(() -> fileSystem.touch("/home_user1/my_own_file.txt", user1));
        assertDoesNotThrow(() -> fileSystem.chmod("/home_user1/my_own_file.txt", "root", user1, "r--"));
        assertDoesNotThrow(() -> fileSystem.chmod("/home_user1/my_own_file.txt", "root", "other", "rwx"));
        String content = "Hello World";
        byte[] buffer = content.getBytes(StandardCharsets.UTF_8);
        assertDoesNotThrow(() -> fileSystem.write("/testfile_root.txt", "root", false, buffer));
        byte[] readBuffer = new byte[buffer.length];
        assertDoesNotThrow(() -> fileSystem.read("/testfile_root.txt", "root", readBuffer));
        assertEquals(content, new String(readBuffer, StandardCharsets.UTF_8));
    }

    @Test
    public void testUserCannotWriteWithoutPermission() throws CaminhoJaExistenteException, PermissaoException, CaminhoNaoEncontradoException {
        assertDoesNotThrow(() -> fileSystem.mkdir("/public_dir", "root"));
        assertDoesNotThrow(() -> fileSystem.chmod("/public_dir", "root", "other", "r-x"));
        String normalUser = "joao";
        assertThrows(PermissaoException.class, () -> fileSystem.touch("/public_dir/joaofile.txt", normalUser));
        assertDoesNotThrow(() -> fileSystem.mkdir("/home_joao", "root"));
        assertDoesNotThrow(() -> fileSystem.chmod("/home_joao", "root", "joao", "rwx"));
        assertThrows(PermissaoException.class, () -> fileSystem.mkdir("/home_joao/joao_subdir", normalUser));
        assertThrows(PermissaoException.class, () -> fileSystem.chmod("/home_joao", normalUser, "other", "r--"));
        String outroUser = "maria";
        assertThrows(PermissaoException.class, () -> fileSystem.touch("/home_joao/maria_file.txt", outroUser));
    }

    @Test
    public void testChmodPermissions() throws CaminhoNaoEncontradoException, PermissaoException, CaminhoJaExistenteException {
        assertDoesNotThrow(() -> fileSystem.touch("/file_perms.txt", "root"));
        String userA = "userA";
        assertDoesNotThrow(() -> fileSystem.chmod("/file_perms.txt", "root", userA, "rw-"));
        byte[] data = "Hello from userA".getBytes(StandardCharsets.UTF_8);
        assertDoesNotThrow(() -> fileSystem.write("/file_perms.txt", userA, false, data));
        assertDoesNotThrow(() -> fileSystem.chmod("/file_perms.txt", "root", userA, "r--"));
        assertThrows(PermissaoException.class, () -> fileSystem.write("/file_perms.txt", userA, false, "New data".getBytes(StandardCharsets.UTF_8)));
        assertDoesNotThrow(() -> fileSystem.mkdir("/home_userB", "root"));
        assertDoesNotThrow(() -> fileSystem.chmod("/home_userB", "root", "userB", "rwx"));
        String userB = "userB";
        assertDoesNotThrow(() -> fileSystem.touch("/home_userB/another_file.txt", userB));
        String userC = "userC";
        assertThrows(PermissaoException.class, () -> fileSystem.chmod("/home_userB/another_file.txt", userC, "userB", "rwx"));
    }

    @Test
    public void testRmPermissions() throws CaminhoJaExistenteException, PermissaoException, CaminhoNaoEncontradoException {
        assertDoesNotThrow(() -> fileSystem.mkdir("/parent_dir", "root"));
        assertDoesNotThrow(() -> fileSystem.touch("/parent_dir/file_to_remove.txt", "root"));
        String userB = "userB";
        assertThrows(PermissaoException.class, () -> fileSystem.rm("/parent_dir/file_to_remove.txt", userB, false));
        assertDoesNotThrow(() -> fileSystem.chmod("/parent_dir", "root", "other", "r-x"));
        assertDoesNotThrow(() -> fileSystem.mkdir("/home_userB_rm", "root"));
        assertDoesNotThrow(() -> fileSystem.chmod("/home_userB_rm", "root", userB, "rwx"));
        assertDoesNotThrow(() -> fileSystem.touch("/home_userB_rm/my_file.txt", userB));
        assertDoesNotThrow(() -> fileSystem.rm("/home_userB_rm/my_file.txt", userB, false));
        assertDoesNotThrow(() -> fileSystem.mkdir("/non_empty_dir", "root"));
        assertDoesNotThrow(() -> fileSystem.touch("/non_empty_dir/a.txt", "root"));
        assertThrows(PermissaoException.class, () -> fileSystem.rm("/non_empty_dir", "root", false));
        assertDoesNotThrow(() -> fileSystem.rm("/non_empty_dir", "root", true));
    }

    @Test
    public void testMvPermissions() throws CaminhoJaExistenteException, PermissaoException, CaminhoNaoEncontradoException {
        assertDoesNotThrow(() -> fileSystem.mkdir("/dir1", "root"));
        assertDoesNotThrow(() -> fileSystem.mkdir("/dir2", "root"));
        assertDoesNotThrow(() -> fileSystem.touch("/dir1/file1.txt", "root"));
        assertDoesNotThrow(() -> fileSystem.chmod("/dir1", "root", "userC", "rwx"));
        String userC = "userC";
        assertThrows(PermissaoException.class, () -> fileSystem.mv("/dir1/file1.txt", "/dir2/file1.txt", userC));
        assertDoesNotThrow(() -> fileSystem.touch("/dir1/file_userC.txt", userC));
        assertDoesNotThrow(() -> fileSystem.chmod("/dir2", "root", userC, "rwx"));
        assertDoesNotThrow(() -> fileSystem.mv("/dir1/file_userC.txt", "/dir2/moved_file.txt", userC));
        assertThrows(PermissaoException.class, () -> fileSystem.mv("/", "/new_root", "root"));
    }

    @Test
    public void testCpPermissions() throws CaminhoJaExistenteException, PermissaoException, CaminhoNaoEncontradoException {
        assertDoesNotThrow(() -> fileSystem.mkdir("/source_dir", "root"));
        assertDoesNotThrow(() -> fileSystem.mkdir("/dest_dir", "root"));
        assertDoesNotThrow(() -> fileSystem.touch("/source_dir/file_orig.txt", "root"));
        String userD = "userD";
        assertThrows(PermissaoException.class, () -> fileSystem.cp("/source_dir/file_orig.txt", "/dest_dir/copy.txt", userD, false));
        assertDoesNotThrow(() -> fileSystem.chmod("/source_dir/file_orig.txt", "root", "other", "r--"));
        assertDoesNotThrow(() -> fileSystem.chmod("/source_dir", "root", "other", "r-x"));
        assertDoesNotThrow(() -> fileSystem.chmod("/dest_dir", "root", "other", "r-x"));
        assertThrows(PermissaoException.class, () -> fileSystem.cp("/source_dir/file_orig.txt", "/dest_dir/copy.txt", userD, false));
        assertDoesNotThrow(() -> fileSystem.chmod("/dest_dir", "root", userD, "rwx"));
        assertDoesNotThrow(() -> fileSystem.cp("/source_dir/file_orig.txt", "/dest_dir/copy_by_userD.txt", userD, false));
    }

    @Test
    public void testReadWritePermissions() throws CaminhoJaExistenteException, PermissaoException, CaminhoNaoEncontradoException {
        assertDoesNotThrow(() -> fileSystem.touch("/private_file.txt", "root"));
        String content = "Secret data";
        byte[] originalContentBytes = content.getBytes(StandardCharsets.UTF_8);
        assertDoesNotThrow(() -> fileSystem.write("/private_file.txt", "root", false, originalContentBytes));
        String hacker = "hacker";
        byte[] readBuffer = new byte[originalContentBytes.length];
        assertDoesNotThrow(() -> fileSystem.chmod("/private_file.txt", "root", "other", "---"));
        assertThrows(PermissaoException.class, () -> fileSystem.read("/private_file.txt", hacker, readBuffer));
        assertThrows(PermissaoException.class, () -> fileSystem.write("/private_file.txt", hacker, false, "Malicious code".getBytes(StandardCharsets.UTF_8)));
        assertDoesNotThrow(() -> fileSystem.chmod("/private_file.txt", "root", "other", "r--"));
        assertDoesNotThrow(() -> fileSystem.read("/private_file.txt", hacker, readBuffer));
        assertEquals(content, new String(readBuffer, StandardCharsets.UTF_8));
    }
}