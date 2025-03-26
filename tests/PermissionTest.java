package tests;

import static org.junit.Assert.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeAll;

import filesys.FileSystemImpl;
import filesys.IFileSystem;

// Essa classe testa cenários de permissão
public class PermissionTest {
    private static IFileSystem fileSystem;

    @BeforeAll
    public static void setUp() {
        fileSystem = new FileSystemImpl(/*args...*/);
    }

    @Test
    public void testPermission() {
        // Teste de permissão
        assertTrue(true);
    }
}
