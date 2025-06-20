package tests;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import exception.CaminhoNaoEncontradoException;
import exception.PermissaoException;
import filesys.FileSystemImpl;
import filesys.IFileSystem;
import filesys.Usuario;
import filesys.Offset;

public class chmodTest {
    private IFileSystem fs;

    @BeforeEach
    public void setUp() {
        fs = new FileSystemImpl(List.of(
                new Usuario("root", "/**", "rwx"),
                new Usuario("maria", "/**", "rwx"),
                new Usuario("joao", "/**", "rwx")
        ));
    }

    @Test
    public void testChmodPorRoot() throws Exception {
        fs.mkdir("/docs", "root");
        fs.touch("/docs/a.txt", "root");
        fs.chmod("/docs/a.txt", "root", "root", "rw");
        fs.write("/docs/a.txt", "root", false, new Offset(0), "abc".getBytes());
    }

    @Test
    public void testChmodPorDono() throws Exception {
        fs.mkdir("/mariadir", "maria");
        fs.touch("/mariadir/file.txt", "maria");
        fs.chmod("/mariadir/file.txt", "maria", "maria", "r--");
        assertThrows(PermissaoException.class, () -> {
            fs.write("/mariadir/file.txt", "maria", false, new Offset(0), "x".getBytes());
        });
    }

    @Test
    public void testChmodPorNaoDono() throws Exception {
        fs.mkdir("/mariadir", "maria");
        fs.touch("/mariadir/file.txt", "maria");
        assertThrows(PermissaoException.class, () -> {
            fs.chmod("/mariadir/file.txt", "joao", "maria", "rwx");
        });
    }

    @Test
    public void testChmodDiretorio() throws Exception {
        fs.mkdir("/mariadir", "maria");
        fs.chmod("/mariadir", "maria", "maria", "r--");
        assertThrows(PermissaoException.class, () -> {
            fs.touch("/mariadir/novo.txt", "maria");
        });
    }

    @Test
    public void testChmodCaminhoInvalido() {
        assertThrows(CaminhoNaoEncontradoException.class, () -> {
            fs.chmod("/inexistente.txt", "root", "root", "rwx");
        });
    }
}
