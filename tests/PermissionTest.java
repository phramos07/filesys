package tests;

import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.io.File;

import org.junit.jupiter.api.Test;

import exception.PermissaoException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeAll;

import filesys.FileSystem;
import filesys.FileSystemImpl;
import filesys.IFileSystem;
import filesys.Usuario;

// Essa classe testa cenários de permissão
public class PermissionTest {
    private static IFileSystem fileSystem;
    private static final String ROOT_USER = "root";
    private static final String ROOT_DIR = "/";
    private static final int READ_BUFFER_SIZE = 256;

    @BeforeAll
    public static void setUp() throws Exception {
        Map<String, Usuario> usuariosMap = new HashMap<>();
        try (Scanner userScanner = new Scanner(new File("users/users"))) {
            while (userScanner.hasNextLine()) {
                String line = userScanner.nextLine().trim();
                if (!line.isEmpty()) {
                    String[] parts = line.split(" ");
                    if (parts.length == 3) {
                        String userListed = parts[0];
                        String dir = parts[1];
                        String dirPermission = parts[2];
                        Usuario usuario = usuariosMap.getOrDefault(userListed, new Usuario(userListed));
                        usuario.adicionarPermissao(dir, dirPermission);
                        usuariosMap.put(userListed, usuario);
                    }
                }
            }
        }
        fileSystem = new FileSystem(usuariosMap);
        fileSystem.mkdir(ROOT_DIR, ROOT_USER);
        fileSystem.mkdir("/users", ROOT_USER);
        fileSystem.mkdir("/users/athos", ROOT_USER);
        fileSystem.mkdir("/users/joao", ROOT_USER);
    }

    @Test
    public void testMkdirUsersPermissions() throws Exception {
        // root: rwx em /** (pode tudo)
        assertDoesNotThrow(() -> fileSystem.mkdir("/rootTest", "root"));

        // maria: rw- em /** (pode criar, mas não pode executar)
        assertThrows(PermissaoException.class, () -> fileSystem.mkdir("/mariaTest", "maria"));

        // luzia: rwx em /** (possui permissão root)
        assertDoesNotThrow(() -> fileSystem.mkdir("/luziaTest", "luzia"));

        // carla: r-- em /** (não pode criar em lugar nenhum)
        assertThrows(PermissaoException.class, () -> fileSystem.mkdir("/carlaTest", "carla"));

        // athos: rwx apenas em /users/athos/**
        assertThrows(PermissaoException.class, () -> fileSystem.mkdir("/athosTest", "athos"));
        assertDoesNotThrow(() -> fileSystem.mkdir("/users/athos/teste", "athos"));

        // joao: rwx apenas em em /users/joao/**
        assertDoesNotThrow(() -> fileSystem.mkdir("/users/joao/teste", "joao"));
        assertThrows(PermissaoException.class, () -> fileSystem.mkdir("/users/athos/joaoTest", "joao"));
    }

    @Test
    public void testPermission() throws Exception {
        // Usuário com permissão de escrita
        assertDoesNotThrow(() -> fileSystem.mkdir("/testMaria", "maria"));

        // Usuário athos não tem permissão para criar no diretório raiz
        assertThrows(PermissaoException.class, () -> fileSystem.mkdir("/testAthos", "athos"));

        // Usuário athos pode criar no /home
        assertDoesNotThrow(() -> fileSystem.mkdir("/home/testAthos", "athos"));

        // Usuário root sempre pode
        assertDoesNotThrow(() -> fileSystem.mkdir("/testRoot", ROOT_USER));
    }
}
