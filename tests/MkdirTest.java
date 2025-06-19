package tests;

import filesys.FileSystemImpl;
import filesys.Usuario;
import exception.CaminhoJaExistenteException;
import exception.CaminhoNaoEncontradoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class MkdirTest {
    private FileSystemImpl fs;

    @BeforeEach
    public void setup() {
        fs = new FileSystemImpl(List.of(
                new Usuario("root", "/**", "rwx"),
                new Usuario("maria", "/**", "rwx"),
                new Usuario("joao", "/**", "rw-")));
    }

    @Test
    public void testCriarDiretorioComSucesso() {
        assertDoesNotThrow(() -> fs.mkdir("/docs", "maria"));
    }

    @Test
    public void testCriarDiretorioPaiNaoExiste() {
        assertThrows(CaminhoNaoEncontradoException.class, () -> fs.mkdir("/naoexiste/docs", "maria"));
    }

    @Test
    public void testCriarDiretorioQueJaExiste() throws Exception {
        fs.mkdir("/docs", "maria");
        assertThrows(CaminhoJaExistenteException.class, () -> fs.mkdir("/docs", "maria"));
    }
}