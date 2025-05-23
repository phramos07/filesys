package tests;

import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

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
       
        Map<String, Map<String, String>> permissoes = new HashMap<>();

        Map<String, String> rootPermissoes = new HashMap<>();
        rootPermissoes.put("/", "rwx");
        permissoes.put("root", rootPermissoes);

        fileSystem = new FileSystemImpl(permissoes);
        // Adicionar mais usuários se necessário.

    }

    @Test
    public void testPermission() {
        // Teste de permissão
        assertTrue(true);
    }
    
}
