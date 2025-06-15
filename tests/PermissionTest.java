package tests;

import static org.junit.Assert.assertTrue;

import org.junit.jupiter.api.Test;

import exception.CaminhoJaExistenteException;
import exception.CaminhoNaoEncontradoException;
import exception.PermissaoException;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeAll;

import filesys.FileSystemImpl;
import filesys.IFileSystem;
import filesys.Usuario;

// Essa classe testa cenários de permissão
public class PermissionTest {
    private static IFileSystem fileSystem;

    @BeforeAll
    public static void setUp() throws CaminhoJaExistenteException, PermissaoException, CaminhoNaoEncontradoException {
        fileSystem = new FileSystemImpl();
        fileSystem.addUser(new Usuario("maria", "/", "r-x"));
        fileSystem.addUser(new Usuario("joao", "/", "rwx"));
        fileSystem.addUser(new Usuario("tiago", "/", "rw-"));

        fileSystem.mkdir("/area1", "joao");
        fileSystem.mkdir("/area1/area2", "tiago");
    }


    // =============== MKDIR ===============

    @Test
    public void testMkdirSuccess() throws CaminhoJaExistenteException, PermissaoException {
        // Tenta criar um diretório com permissão de rwx
        assertDoesNotThrow(() -> fileSystem.mkdir("/area1/area2/area3", "root"));
    }

    @Test
    public void testMkdirPermissionFail() {
        // Tenta criar um diretório sem permissão
        assertThrows(PermissaoException.class, () -> fileSystem.mkdir("/area1/area2/area3/area4", "maria"));
    }

    @Test
    public void testMkdirPathFail() {
        // Tenta criar um diretório em um caminho inexistente
        assertThrows(CaminhoNaoEncontradoException.class, () -> fileSystem.mkdir("/area1/area2/area3/inexistente/area5", "joao"));
    }


    // =============== TOUCH ===============

    @Test
    public void testTouchSuccess() throws CaminhoJaExistenteException, PermissaoException, CaminhoNaoEncontradoException {
        // Tenta criar um arquivo com permissão de rwx
        assertDoesNotThrow(() -> fileSystem.touch("/area1/area2/arquivo.txt", "joao"));
    }

    @Test
    public void testTouchPermissionFail() {
        // Tenta criar um arquivo sem permissão
        assertThrows(PermissaoException.class, () -> fileSystem.touch("/area1/area2/arquivo2.txt", "maria"));
    }

    @Test
    public void testTouchPathFail() {
        // Tenta criar um arquivo em um caminho inexistente
        assertThrows(CaminhoNaoEncontradoException.class, () -> fileSystem.touch("/area1/area2/inexistente/arquivo3.txt", "root"));
    }


    // =============== CHMOD ===============

    @Test
    public void testChmodSuccess() throws CaminhoNaoEncontradoException, PermissaoException {
        // Tenta alterar as permissões de um diretório existente
        assertDoesNotThrow(() -> fileSystem.chmod("/area1/area2", "root", "tiago", "rwx"));
    }

    @Test
    public void testChmodPermissionFail() {
        // Tenta alterar as permissões de um diretório sem permissão
        assertThrows(PermissaoException.class, () -> fileSystem.chmod("/area1/area2", "maria", "tiago", "rwx"));
    }

    @Test
    public void testChmodPathFail() {
        // Tenta alterar as permissões de um diretório inexistente
        assertThrows(CaminhoNaoEncontradoException.class, () -> fileSystem.chmod("/area1/area2/inexistente", "root", "tiago", "rwx"));
    }

}
