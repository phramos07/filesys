package tests;

import org.junit.jupiter.api.Test;
import exception.PermissaoException;
import static org.junit.jupiter.api.Assertions.*;

// Essa classe testa cenários de permissão
public class PermissionTest extends FileSystemTestBase {

    @Test
    public void testMkdirPermissions() throws Exception {
        // root pode tudo
        assertDoesNotThrow(() -> fileSystem.mkdir("/rootTest", "root"));

        // maria rw- em /** (não pode criar diretório, falta x)
        assertThrows(PermissaoException.class, () -> fileSystem.mkdir("/mariaTest", "maria"));

        // luzia rwx em /**
        assertDoesNotThrow(() -> fileSystem.mkdir("/luziaTest", "luzia"));

        // carla r-- em /**
        assertThrows(PermissaoException.class, () -> fileSystem.mkdir("/carlaTest", "carla"));

        // athos rwx apenas em /users/athos/**
        assertThrows(PermissaoException.class, () -> fileSystem.mkdir("/athosTest", "athos"));
        assertDoesNotThrow(() -> fileSystem.mkdir("/users/athos/teste", "athos"));

        // joao rwx apenas em /users/joao/**
        assertDoesNotThrow(() -> fileSystem.mkdir("/users/joao/teste", "joao"));
        assertThrows(PermissaoException.class, () -> fileSystem.mkdir("/users/athos/joaoTest", "joao"));
    }

    @Test
    public void testTouchPermissions() throws Exception {
        // root pode tudo
        assertDoesNotThrow(() -> fileSystem.touch("/users/athos/teste.txt", "root"));

        // maria rw- em /** (não pode criar arquivo, falta x)
        assertThrows(PermissaoException.class, () -> fileSystem.touch("/users/athos/arquivoMaria.txt", "maria"));

        // luzia rwx em /**
        assertDoesNotThrow(() -> fileSystem.touch("/users/athos/arquivoLuzia.txt", "luzia"));

        // carla r-- em /**
        // não pode criar arquivo, falta x
        assertThrows(PermissaoException.class, () -> fileSystem.touch("/users/athos/arquivoCarla.txt", "carla"));

        // athos rwx apenas em /users/athos/**
        assertDoesNotThrow(() -> fileSystem.touch("/users/athos/arquivoAthos.txt", "athos"));
        assertThrows(PermissaoException.class, () -> fileSystem.touch("/users/joao/arquivoAthos.txt", "athos"));

        // joao rwx apenas em /users/joao/**
        assertDoesNotThrow(() -> fileSystem.touch("/users/joao/arquivoJoao.txt", "joao"));
    }

    @Test
    public void testLsPermissions() throws Exception {
        // root pode listar tudo
        assertDoesNotThrow(() -> fileSystem.ls("/users/athos", "root", false));

        // maria rw- em /** (não pode listar, falta x)
        assertThrows(PermissaoException.class, () -> fileSystem.ls("/users/athos", "maria", false));

        // luzia rwx em /**
        assertDoesNotThrow(() -> fileSystem.ls("/users/athos", "luzia", false));

        // carla r-- em /**
        assertThrows(PermissaoException.class, () -> fileSystem.ls("/users/athos", "carla", false));

        // athos rwx apenas em /users/athos/**
        assertDoesNotThrow(() -> fileSystem.ls("/users/athos", "athos", false));
        assertThrows(PermissaoException.class, () -> fileSystem.ls("/users/joao", "athos", false));

        // joao rwx apenas em /users/joao/**
        assertDoesNotThrow(() -> fileSystem.ls("/users/joao", "joao", false));
    }

    @Test
    public void testCpPermissions() throws Exception {
        fileSystem.mkdir("/testeCopiaRoot", ROOT_USER);
        fileSystem.touch("/users/athos/arquivo.txt", ROOT_USER);
        fileSystem.chmod("/users/athos/arquivo.txt", ROOT_USER, "athos", "rwx");
        // root pode tudo
        assertDoesNotThrow(
                () -> fileSystem.cp("/users/athos/arquivo.txt", "/testeCopiaRoot/arquivo.txt", "root", false));
        // athos tem permissão de escrita/leitura
        assertDoesNotThrow(
                () -> fileSystem.cp("/users/athos/arquivo.txt", "/users/athos/arquivo2.txt", "athos", false));
        // maria não tem permissão
        assertThrows(PermissaoException.class,
                () -> fileSystem.cp("/users/athos/arquivo.txt", "/testeCopiaRoot/arquivo2.txt", "maria", false));
    }

    @Test
    public void testMvPermissions() throws Exception {
        try {
            fileSystem.mkdir("/testeMovimentoRoot", ROOT_USER);
            fileSystem.touch("/users/athos/arquivo.txt", ROOT_USER);
            fileSystem.chmod("/users/athos/arquivo.txt", ROOT_USER, "athos", "rwx");
        } catch (Exception e) {
        }

        // root pode tudo
        assertDoesNotThrow(() -> fileSystem.mv("/users/athos/arquivo.txt", "/testeMovimentoRoot/arquivo.txt", "root"));

        // maria não pode mover
        try {
            fileSystem.touch("/users/athos/arquivo.txt", ROOT_USER);
        } catch (Exception e) {
        }
        assertThrows(PermissaoException.class,
                () -> fileSystem.mv("/users/athos/arquivo.txt", "/testeMovimentoRoot/arquivo2.txt", "maria"));
    }

    @Test
    public void testWritePermissions() throws Exception {
        // Garante que o arquivo existe antes dos testes
        try {
            fileSystem.touch("/users/athos/arquivo.txt", ROOT_USER);
        } catch (Exception e) {
        }

        // root pode escrever
        assertDoesNotThrow(() -> fileSystem.write("/users/athos/arquivo.txt", "root", false, "abc".getBytes()));

        // maria não pode escrever
        assertThrows(PermissaoException.class,
                () -> fileSystem.write("/users/athos/arquivo.txt", "maria", false, "abc".getBytes()));

        // luzia pode escrever
        assertDoesNotThrow(
                () -> fileSystem.write("/users/athos/arquivo.txt", "luzia", false, "abc".getBytes()));

        // carla não pode escrever
        assertThrows(PermissaoException.class,
                () -> fileSystem.write("/users/athos/arquivo.txt", "carla", false, "abc".getBytes()));

        // athos pode escrever apenas em sua pasta
        assertDoesNotThrow(() -> fileSystem.write("/users/athos/arquivo.txt", "athos", false, "abc".getBytes()));
        try {
            fileSystem.touch("/users/joao/arquivo.txt", "joao");
        } catch (Exception e) {
        }
        assertThrows(PermissaoException.class,
                () -> fileSystem.write("/users/joao/arquivo.txt", "athos", false, "abc".getBytes()));
    }

    @Test
    public void testReadPermissions() throws Exception {
        fileSystem.touch("/users/athos/texto.txt", ROOT_USER);
        fileSystem.write("/users/athos/texto.txt", ROOT_USER, false, "abc".getBytes());
        fileSystem.chmod("/users/athos/texto.txt", ROOT_USER, "athos", "rwx");
        fileSystem.chmod("/users/athos/texto.txt", ROOT_USER, "luzia", "rwx");
        fileSystem.chmod("/users/athos/texto.txt", ROOT_USER, "maria", "rw-");
        fileSystem.chmod("/users/athos/texto.txt", ROOT_USER, "carla", "r--");

        // root pode ler
        assertDoesNotThrow(() -> fileSystem.read("/users/athos/texto.txt", "root", buffer, offset));

        // maria não pode ler (falta x)
        assertThrows(PermissaoException.class,
                () -> fileSystem.read("/users/athos/texto.txt", "maria", buffer, offset));

        // luzia pode ler
        assertDoesNotThrow(() -> fileSystem.read("/users/athos/texto.txt", "luzia", buffer, offset));

        // athos pode ler
        assertDoesNotThrow(() -> fileSystem.read("/users/athos/texto.txt", "athos", buffer, offset));

        // athos não pode ler arquivo de joao
        assertThrows(PermissaoException.class, () -> fileSystem.read("/users/joao/texto.txt", "athos", buffer, offset));

        // joao pode ler seu próprio arquivo
        fileSystem.touch("/users/joao/texto.txt", "joao");
        fileSystem.write("/users/joao/texto.txt", "joao", false, "abc".getBytes());
        assertDoesNotThrow(() -> fileSystem.read("/users/joao/texto.txt", "joao", buffer, offset));
    }

    @Test
    public void testRmPermissions() throws Exception {
        String path = "/users/athos/rmtest.txt";
        // Garante que o arquivo existe antes dos testes
        Runnable setup = () -> {
            try {
                fileSystem.touch(path, ROOT_USER);
            } catch (Exception ignored) {
            }
        };

        // root pode remover
        setup.run();
        fileSystem.chmod(path, ROOT_USER, "athos", "rwx");
        fileSystem.chmod(path, ROOT_USER, "luzia", "rwx");
        fileSystem.chmod(path, ROOT_USER, "maria", "rw-");
        fileSystem.chmod(path, ROOT_USER, "carla", "r--");
        assertDoesNotThrow(() -> fileSystem.rm(path, "root", false));

        // athos pode remover
        setup.run();
        fileSystem.chmod(path, ROOT_USER, "athos", "rwx");
        assertDoesNotThrow(() -> fileSystem.rm(path, "athos", false));

        // luzia pode remover
        setup.run();
        fileSystem.chmod(path, ROOT_USER, "luzia", "rwx");
        assertDoesNotThrow(() -> fileSystem.rm(path, "luzia", false));

        // maria não pode remover
        setup.run();
        fileSystem.chmod(path, ROOT_USER, "maria", "rw-");
        assertThrows(PermissaoException.class, () -> fileSystem.rm(path, "maria", false));

        // carla não pode remover
        setup.run();
        fileSystem.chmod(path, ROOT_USER, "carla", "r--");
        assertThrows(PermissaoException.class, () -> fileSystem.rm(path, "carla", false));
    }

    @Test
    public void testChmodPermissions() throws Exception {
        // root pode mudar permissões de qualquer arquivo   
        assertDoesNotThrow(() -> fileSystem.chmod("/users/athos", "root", "maria", "rwx"));
       
        // maria não pode mudar permissões de /users/athos
        assertDoesNotThrow(() -> fileSystem.mkdir("/users/athos/testeLuzia", "luzia"));
    }
}
