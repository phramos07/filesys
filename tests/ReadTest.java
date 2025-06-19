package tests;

import filesys.FileSystemImpl;
import filesys.Offset;
import filesys.Usuario;
import exception.CaminhoNaoEncontradoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class ReadTest {
    private FileSystemImpl fs;

    @BeforeEach
    public void setup() throws Exception {
        fs = new FileSystemImpl(List.of(
                new Usuario("root", "/**", "rwx"),
                new Usuario("maria", "/**", "rwx")));
        fs.mkdir("/docs", "maria");
        fs.touch("/docs/arquivo.txt", "maria");
        Offset offset = new Offset(0);
        fs.write("/docs/arquivo.txt", "maria", false, offset, "Hello World".getBytes());
    }

    @Test
    public void testLerArquivoComSucesso() {
        byte[] buffer = new byte[20];
        assertDoesNotThrow(() -> fs.read("/docs/arquivo.txt", "maria", buffer));
    }

    @Test
    public void testLerArquivoNaoExiste() {
        byte[] buffer = new byte[20];
        assertThrows(CaminhoNaoEncontradoException.class, () -> fs.read("/docs/inexistente.txt", "maria", buffer));
    }

}
