package tests;

import filesys.FileSystemImpl;
import filesys.Usuario;
import exception.CaminhoJaExistenteException;
import exception.CaminhoNaoEncontradoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class TouchTest {
    private FileSystemImpl fs;

    @BeforeEach
    public void setup() throws Exception {
        fs = new FileSystemImpl(List.of(
                new Usuario("root", "/**", "rwx"),
                new Usuario("maria", "/**", "rwx")));
        fs.mkdir("/docs", "maria");
    }

    @Test
    public void testCriarArquivoComSucesso() {
        assertDoesNotThrow(() -> fs.touch("/docs/arquivo.txt", "maria"));
    }

    @Test
    public void testCriarArquivoDiretorioInexistente() {
        assertThrows(CaminhoNaoEncontradoException.class, () -> fs.touch("/naoexiste/arquivo.txt", "maria"));
    }

    @Test
    public void testCriarArquivoJaExistente() throws Exception {
        fs.touch("/docs/arquivo.txt", "maria");
        assertThrows(CaminhoJaExistenteException.class, () -> fs.touch("/docs/arquivo.txt", "maria"));
    }
}
