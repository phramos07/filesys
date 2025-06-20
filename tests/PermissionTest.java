package tests;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import exception.PermissaoException;
import filesys.FileSystemImpl;
import filesys.IFileSystem;
import filesys.Usuario;

/**
 * Testes de permissões básicas no sistema de arquivos.
 */
public class PermissionTest {

    private IFileSystem fileSystem;

    @BeforeEach
    public void setUp() {
        List<Usuario> usuarios = new ArrayList<>();
        usuarios.add(new Usuario("root", "/**", "rwx"));
        usuarios.add(new Usuario("carla", "/**", "r--"));
        usuarios.add(new Usuario("maria", "/**", "rw-"));
        usuarios.add(new Usuario("lucas", "/**", "-wx"));
        fileSystem = new FileSystemImpl(usuarios);

        try {
            fileSystem.mkdir("/teste", "root");
            fileSystem.touch("/teste/arquivo.txt", "root");
        } catch (Exception e) {
            fail("Falha na configuração inicial: " + e.getMessage());
        }
    }

    @Test
    public void testReadPermissionAllowed() {
        byte[] buffer = new byte[100];
        assertDoesNotThrow(() -> fileSystem.read("/teste/arquivo.txt", "carla", buffer));
    }

    @Test
    public void testReadPermissionDenied() {
        byte[] buffer = new byte[100];
        assertThrows(PermissaoException.class, () -> fileSystem.read("/teste/arquivo.txt", "lucas", buffer));
    }

    @Test
    public void testLsPermissionAllowed() {
        assertDoesNotThrow(() -> fileSystem.ls("/teste", "carla", false));
    }

    @Test
    public void testLsPermissionDenied() {
        // chmod tira o r da pasta para "maria"
        assertDoesNotThrow(() -> fileSystem.chmod("/teste", "root", "maria", "--x"));
        assertThrows(PermissaoException.class, () -> fileSystem.ls("/teste", "maria", false));
    }

    @Test
    public void testTouchPermissionAllowed() {
        assertDoesNotThrow(() -> fileSystem.touch("/teste/novo.txt", "maria"));
    }

    @Test
    public void testTouchPermissionDenied() {
        assertThrows(PermissaoException.class, () -> fileSystem.touch("/teste/negado.txt", "carla"));
    }

    @Test
    public void testMkdirPermissionAllowed() {
        assertDoesNotThrow(() -> fileSystem.mkdir("/teste/novoDir", "maria"));
    }

    @Test
    public void testMkdirPermissionDenied() {
        assertThrows(PermissaoException.class, () -> fileSystem.mkdir("/teste/semPerm", "carla"));
    }
}