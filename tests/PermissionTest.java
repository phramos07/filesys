package tests;

import static org.junit.Assert.assertTrue;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;

import filesys.FileSystemImpl;
import filesys.IFileSystem;
import filesys.Usuario;

// Essa classe testa cenários de permissão
public class PermissionTest {
    private static IFileSystem fileSystem;

    @BeforeAll
    public static void setUp() {
        fileSystem = new FileSystemImpl(Collections.singletonList(new Usuario("root", "rwx", "/")));
    }

    @Test
    public void testPermission() {
        // Teste de permissão
        assertTrue(true);
    }
}
