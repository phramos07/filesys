package tests;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import exception.CaminhoJaExistenteException;
import exception.CaminhoNaoEncontradoException;
import exception.PermissaoException;
import filesys.FileSystemImpl;
import filesys.IFileSystem;
import filesys.Offset;
import filesys.Usuario;

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
            fileSystem.write("/teste/arquivo.txt", "root", false, new Offset(0), "Hello World".getBytes());
            fileSystem.chmod("/teste/arquivo.txt", "root", "carla", "r--");
            fileSystem.chmod("/teste/arquivo.txt", "root", "maria", "rw-");
            fileSystem.chmod("/teste/arquivo.txt", "root", "lucas", "--x");
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

    @Test
    public void testWritePermissionAllowed() {
        Offset offset = new Offset(0);
        assertDoesNotThrow(() -> fileSystem.write("/teste/arquivo.txt", "maria", true, offset, "Mais texto".getBytes()));
    }

    @Test
    public void testWritePermissionDenied() {
        Offset offset = new Offset(0);
        assertThrows(PermissaoException.class, () -> fileSystem.write("/teste/arquivo.txt", "carla", false, offset, "negado".getBytes()));
    }

    @Test
    public void testChmodPorRoot() {
        assertDoesNotThrow(() -> fileSystem.chmod("/teste/arquivo.txt", "root", "maria", "rwx"));
    }

    @Test
    public void testChmodPorDono() throws Exception {
        fileSystem.touch("/teste/meuarq.txt", "maria");
        assertDoesNotThrow(() -> fileSystem.chmod("/teste/meuarq.txt", "maria", "maria", "rwx"));
    }

    @Test
    public void testChmodNegado() throws Exception {
        fileSystem.touch("/teste/meuarq.txt", "maria");
        assertThrows(PermissaoException.class, () -> fileSystem.chmod("/teste/meuarq.txt", "lucas", "maria", "rwx"));
    }

    @Test
    public void testRmPermissionAllowed() {
        assertDoesNotThrow(() -> fileSystem.rm("/teste/arquivo.txt", "root", false));
    }

    @Test
    public void testRmPermissionDenied() {
        assertThrows(PermissaoException.class, () -> fileSystem.rm("/teste/arquivo.txt", "carla", false));
    }

    @Test
    public void testMvPermissionDenied() throws Exception {
        fileSystem.touch("/teste/b.txt", "maria");
        assertThrows(PermissaoException.class, () -> fileSystem.mv("/teste/b.txt", "/teste/b_renomeado.txt", "carla"));
    }

    @Test
    public void testCpPermissionDenied() throws Exception {
        fileSystem.touch("/teste/d.txt", "maria");
        assertThrows(PermissaoException.class, () -> fileSystem.cp("/teste/d.txt", "/teste/d_copia.txt", "carla", false));
    }

    @Test
    public void testRmCaminhoNaoExiste() {
        assertThrows(CaminhoNaoEncontradoException.class, () -> fileSystem.rm("/teste/inexistente.txt", "maria", false));
    }

    @Test
    public void testTouchDiretorioNaoExiste() {
        assertThrows(CaminhoNaoEncontradoException.class, () -> fileSystem.touch("/inexistente/novo.txt", "maria"));
    }

    @Test
    public void testMkdirNoCaminhoRaizNaoPode() {
        assertThrows(CaminhoJaExistenteException.class, () -> fileSystem.mkdir("/", "maria"));
    }
}
