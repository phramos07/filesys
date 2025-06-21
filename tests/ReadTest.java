package tests;

import filesys.FileSystemImpl;
import filesys.Offset;
import filesys.Usuario;
import exception.CaminhoNaoEncontradoException;
import exception.PermissaoException;
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
                new Usuario("maria", "/**", "rwx"),
                new Usuario("lucas", "/**", "-wx"),
                new Usuario("carla", "/**", "r--")));

        fs.mkdir("/docs", "maria");
        fs.touch("/docs/arquivo.txt", "maria");

        Offset offset = new Offset(0);
        fs.write("/docs/arquivo.txt", "maria", false, offset, "Hello World".getBytes());

        fs.touch("/docs/vazio.txt", "maria");
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

    @Test
    public void testLerArquivoSemPermissao() {
        byte[] buffer = new byte[20];
        assertThrows(PermissaoException.class, () -> fs.read("/docs/arquivo.txt", "lucas", buffer));
    }

    @Test
    public void testLerArquivoComoRoot() {
        byte[] buffer = new byte[20];
        assertDoesNotThrow(() -> fs.read("/docs/arquivo.txt", "root", buffer));
    }

    @Test
    public void testLeituraParcialComBufferPequeno() {
        byte[] buffer = new byte[5]; // conteúdo é "Hello World"
        assertDoesNotThrow(() -> fs.read("/docs/arquivo.txt", "maria", buffer));
        String lido = new String(buffer);
        assertEquals("Hello", lido); // só cabe "Hello" no buffer
    }

    @Test
    public void testLeituraDeArquivoVazio() {
        byte[] buffer = new byte[10];
        assertDoesNotThrow(() -> fs.read("/docs/vazio.txt", "maria", buffer));
    }

    @Test
    public void testLeituraPermitidaPorPermissaoGlobal() {
        byte[] buffer = new byte[20];
        assertDoesNotThrow(() -> fs.read("/docs/arquivo.txt", "carla", buffer));
    }
}
