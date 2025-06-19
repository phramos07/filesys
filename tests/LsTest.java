package tests;

import filesys.FileSystemImpl;
import filesys.Usuario;
import exception.CaminhoNaoEncontradoException;
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
                new Usuario("maria", "/**", "rwx")));
        fs.mkdir("/docs", "maria");
        fs.touch("/docs/arquivo.txt", "maria");
    }

    @Test
    public void testListarDiretorioComSucesso() {
        assertDoesNotThrow(() -> fs.ls("/docs", "maria", false));
    }

    @Test
    public void testListarDiretorioInexistente() {
        assertThrows(CaminhoNaoEncontradoException.class, () -> fs.ls("/naoexiste", "maria", false));
    }
}
