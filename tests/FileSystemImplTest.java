package tests;

import static org.junit.Assert.assertTrue;

import org.junit.jupiter.api.Test;

import exception.CaminhoJaExistenteException;
import exception.CaminhoNaoEncontradoException;
import exception.PermissaoException;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeAll;

import filesys.FileSystemImpl;
import filesys.IFileSystem;
import filesys.Usuario;

// Essa classe testa cenários de permissão
public class FileSystemImplTest {
    private static IFileSystem fileSystem;
    byte[] buffer = new byte[1024];

    @BeforeAll
    public static void setUp() throws CaminhoJaExistenteException, PermissaoException, CaminhoNaoEncontradoException {
        fileSystem = new FileSystemImpl();

        fileSystem.addUser(new Usuario("maria", "/", "r-x"));
        fileSystem.addUser(new Usuario("joao", "/", "rwx"));
        fileSystem.addUser(new Usuario("tiago", "/", "rw-"));
        fileSystem.addUser(new Usuario("cega", "/", "-wx"));

        fileSystem.mkdir("/area1", "joao");
        fileSystem.mkdir("/area1/area2", "tiago");
        fileSystem.mkdir("/area1/area2/area_meh", "root");

        fileSystem.touch("/area1/area2/arquivo.txt", "root");
        fileSystem.touch("/area1/area2/arquivo_meh.txt", "joao");
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


    // =============== TOUCH ===============

    @Test
    public void testTouchSuccess() throws CaminhoJaExistenteException, PermissaoException, CaminhoNaoEncontradoException {
        // Tenta criar um arquivo com permissão de rwx
        assertDoesNotThrow(() -> fileSystem.touch("/area1/arquivo2.txt", "joao"));
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

    @Test
    public void testTouchAlreadyExists() {
        // Tenta criar um arquivo que já existe
        assertThrows(CaminhoJaExistenteException.class, () -> fileSystem.touch("/area1/area2/arquivo.txt", "joao"));
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

    @Test
    public void testChmodInvalidPermission() {
        // Tenta alterar as permissões de um diretório com permissões inválidas
        assertThrows(IllegalArgumentException.class, () -> fileSystem.chmod("/area1/area2", "root", "tiago", "rw"));
    }


    // =============== LS ===============

    @Test
    public void testLsSuccess() throws CaminhoNaoEncontradoException, PermissaoException {
        // Tenta listar o conteúdo de um diretório com permissão
        assertDoesNotThrow(() -> fileSystem.ls("/area1", "joao", true));
    }

    @Test
    public void testlsNaoRecursiveSuccess() throws CaminhoNaoEncontradoException, PermissaoException {
        // Tenta listar o conteúdo de um diretório sem permissão de leitura recursiva
        assertDoesNotThrow(() -> fileSystem.ls("/area1", "joao", false));
    }

    @Test
    public void testLsPermissionFail() {
        // Tenta listar o conteúdo de um diretório sem permissão
        assertThrows(PermissaoException.class, () -> fileSystem.ls("/area1/area2", "cega", true));
    }

    @Test
    public void testLsPathFail() {
        // Tenta listar o conteúdo de um diretório inexistente
        assertThrows(CaminhoNaoEncontradoException.class, () -> fileSystem.ls("/area1/area2/inexistente", "joao", true));
    }


    // =============== CP ===============

    @Test
    public void testCpSuccess() throws CaminhoNaoEncontradoException, PermissaoException {
        // Tenta copiar um arquivo com permissão
        assertDoesNotThrow(() -> fileSystem.cp("/area1/area2/arquivo.txt", "/area1", "root", true));
    }

    @Test
    public void testCpPermissionFail() {
        // Tenta copiar um arquivo sem permissão
        assertThrows(PermissaoException.class, () -> fileSystem.cp("/area1/area2/arquivo.txt", "/area1/area2", "maria", true));
    }

    @Test
    public void testCpPathFail() {
        // Tenta copiar um arquivo de um caminho inexistente
        assertThrows(CaminhoNaoEncontradoException.class, () -> fileSystem.cp("/area1/area2/inexistente/arquivo.txt", "/area1/area2", "root", true));
    }

    @Test
    public void testSobrescreverArquivoSucesso() throws CaminhoNaoEncontradoException, PermissaoException {
        // Tenta copiar um arquivo sobrescrevendo outro existente
        assertDoesNotThrow(() -> fileSystem.cp("/area1/area2/arquivo.txt", "/area1/area2/arquivo.txt", "joao", true));
    }


    // =============== RM ===============

    @Test
    public void testRmSuccess() throws CaminhoNaoEncontradoException, PermissaoException {
        // Tenta remover um arquivo com permissão
        assertDoesNotThrow(() -> fileSystem.rm("/area1/area2/arquivo.txt", "joao", true));
    }

    @Test
    public void testRmPermissionFail() {
        // Tenta remover um arquivo sem permissão
        assertThrows(PermissaoException.class, () -> fileSystem.rm("/area1/area2/arquivo.txt", "maria", true));
    }

    @Test
    public void testRmPathFail() {
        // Tenta remover um arquivo de um caminho inexistente
        assertThrows(CaminhoNaoEncontradoException.class, () -> fileSystem.rm("/area1/area2/inexistente/arquivo.txt", "joao", true));
    }


    // =============== MV ===============

    @Test
    public void testMvInSamePlaceRenamingSuccess() throws CaminhoNaoEncontradoException, PermissaoException {
        // Tenta mover um arquivo com permissão
        assertDoesNotThrow(() -> fileSystem.mv("/area1/area2/arquivo_meh.txt", "/area1/area2/arquivo_moved.txt", "root"));
    }

    @Test
    public void testMvInSamePlaceFail() {
        // Tenta mover um arquivo para o mesmo lugar sem permissão
        assertThrows(IllegalArgumentException.class, () -> fileSystem.mv("/area1/area2/arquivo_moved.txt", "/area1/area2/arquivo_moved.txt", "root"));
    }

    @Test
    public void testMvPermissionFail() {
        // Tenta mover um arquivo sem permissão
        assertThrows(PermissaoException.class, () -> fileSystem.mv("/area1/area2/area_meh", "/area1/area3", "maria"));
    }

    @Test
    public void testMvPathFail() {
        // Tenta mover um arquivo de um caminho inexistente
        assertThrows(CaminhoNaoEncontradoException.class, () -> fileSystem.mv("/area1/area2/inexistente/arquivo.txt", "/area1/area2/arquivo_moved.txt", "root"));
    }


    // =============== READ ===============

    @Test
    public void testReadSuccess() throws CaminhoNaoEncontradoException, PermissaoException {
        // Tenta ler um arquivo com permissão
        assertDoesNotThrow(() -> fileSystem.read("/area1/area2/arquivo.txt", "joao", buffer, 0));
    }

    @Test
    public void testReadPermissionFail() {
        // Tenta ler um arquivo sem permissão
        assertThrows(PermissaoException.class, () -> fileSystem.read("/area1/area2/arquivo.txt", "cega", buffer, 0));
    }

    @Test
    public void testReadPathFail() {
        // Tenta ler um arquivo de um caminho inexistente
        assertThrows(CaminhoNaoEncontradoException.class, () -> fileSystem.read("/area1/area2/inexistente/arquivo.txt", "joao", buffer, 0));
    }


    // =============== WRITE ===============

    @Test
    public void testWriteSuccess() throws CaminhoNaoEncontradoException, PermissaoException {
        // Tenta escrever em um arquivo com permissão
        assertDoesNotThrow(() -> fileSystem.write("/area1/arquivo.txt", "joao", true, buffer));
    }

    @Test
    public void testWritePermissionFail() { 
        // Tenta escrever em um arquivo sem permissão
        assertThrows(PermissaoException.class, () -> fileSystem.write("/area1/area2/arquivo.txt", "maria", true, buffer));
    }

    @Test
    public void testWritePathFail() {
        // Tenta escrever em um arquivo de um caminho inexistente
        assertThrows(CaminhoNaoEncontradoException.class, () -> fileSystem.write("/area1/area2/inexistente/arquivo.txt", "joao", true, buffer));
    }


    //=============== User ===============

    @Test
    public void testAddUser() {
        // Tenta adicionar um usuário com permissão
        assertDoesNotThrow(() -> fileSystem.addUser(new Usuario("carlos", "/", "rwx")));
    }

    @Test
    public void testAddUserWithExistingName() {
        // Tenta adicionar um usuário com nome já existente
        assertThrows(IllegalArgumentException.class, () -> fileSystem.addUser(new Usuario("maria", "/", "rwx")));
    }

    @Test
    public void testAddUserWithInvalidPermission() {
        // Tenta adicionar um usuário com permissões inválidas
        assertThrows(IllegalArgumentException.class, () -> fileSystem.addUser(new Usuario("carlos", "/", "rw")));
    }

    @Test
    public void testAddUserWithEmptyName() {
        // Tenta adicionar um usuário com nome vazio
        assertThrows(IllegalArgumentException.class, () -> fileSystem.addUser(new Usuario("", "/", "rwx")));
    }

    @Test
    public void testAddUserWithoutDirectory() {
        // Tenta adicionar um usuário sem diretório
        assertThrows(IllegalArgumentException.class, () -> fileSystem.addUser(new Usuario("carlos", null, "rwx")));
    }

}