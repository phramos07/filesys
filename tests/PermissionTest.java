package tests;

import org.junit.jupiter.api.Test;

import exception.PermissaoException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

// Essa classe testa cenários de permissão
public class PermissionTest extends FileSystemTestBase {

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
    public void testChmodPermissions() throws Exception {
        // root: rwx em /** (pode tudo)
        assertDoesNotThrow(() -> fileSystem.chmod("/users/athos", "root", "maria", "rwx"));

        // luzia agora pode criar no /users/athos
        assertDoesNotThrow(() -> fileSystem.mkdir("/users/athos/testeLuzia", "luzia"));

        // athos não pode alterar permissão de maria
        assertThrows(PermissaoException.class, () -> fileSystem.chmod("/users/athos", "athos", "maria", "rwx"));
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
