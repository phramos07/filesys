package tests;

import filesys.FileSystemImpl;
import filesys.Usuario;
import exception.CaminhoNaoEncontradoException;
import exception.PermissaoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class LsTest {
    private FileSystemImpl fs;

    @BeforeEach
    public void setup() throws Exception {
        fs = new FileSystemImpl(List.of(
                new Usuario("root", "/**", "rwx"),
                new Usuario("maria", "/**", "rwx"),
                new Usuario("carla", "/**", "r--"),
                new Usuario("lucas", "/**", "-wx")));
        fs.mkdir("/docs", "maria");
        fs.touch("/docs/arquivo.txt", "maria");
        fs.mkdir("/docs/subdir", "maria");
        fs.touch("/docs/subdir/outro.txt", "maria");
        fs.mkdir("/vazio", "maria");
    }

    @Test
    public void testListarDiretorioComSucesso() {
        assertDoesNotThrow(() -> fs.ls("/docs", "maria", false));
    }

    @Test
    public void testListarDiretorioInexistente() {
        assertThrows(CaminhoNaoEncontradoException.class, () -> fs.ls("/naoexiste", "maria", false));
    }

    @Test
    public void testListarDiretorioSemPermissao() {
        // Tira permissão de leitura da pasta docs para lucas
        assertThrows(PermissaoException.class, () -> fs.ls("/docs", "lucas", false));
    }

    @Test
    public void testListarDiretorioComPermissaoGlobal() {
        // carla tem r-- no /** (permite ls em /docs)
        assertDoesNotThrow(() -> fs.ls("/docs", "carla", false));
    }

    @Test
    public void testListarComoRoot() {
        assertDoesNotThrow(() -> fs.ls("/docs", "root", false));
    }

    @Test
    public void testListarDiretorioRecursivo() {
        assertDoesNotThrow(() -> fs.ls("/docs", "maria", true));
    }

    @Test
    public void testListarDiretorioVazio() {
        assertDoesNotThrow(() -> fs.ls("/vazio", "maria", false));
    }
}