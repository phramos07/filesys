package tests;

import exception.CaminhoJaExistenteException;
import exception.CaminhoNaoEncontradoException;
import exception.PermissaoException;
import filesys.FileSystemImpl;
import filesys.IFileSystem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ReadWriteTest {

    private IFileSystem fs;
    private final String usuario = "root";
    private final String caminhoArquivo = "/teste.txt";

    @BeforeEach
    public void setup() throws CaminhoJaExistenteException, PermissaoException {
        fs = new FileSystemImpl();
        fs.touch(caminhoArquivo, usuario);
    }

    @Test
    public void testEscritaSimples() throws CaminhoNaoEncontradoException, PermissaoException {
        byte[] dados = "Hello World".getBytes();
        fs.write(caminhoArquivo, usuario, false, dados);

        byte[] buffer = new byte[50];
        fs.read(caminhoArquivo, usuario, buffer);

        String resultado = new String(buffer).trim();
        assertTrue(resultado.startsWith("Hello World"));
    }

    @Test
    public void testEscritaComAppend() throws CaminhoNaoEncontradoException, PermissaoException {
        fs.write(caminhoArquivo, usuario, false, "Parte1".getBytes());
        fs.write(caminhoArquivo, usuario, true, "Parte2".getBytes());

        byte[] buffer = new byte[50];
        fs.read(caminhoArquivo, usuario, buffer);

        String resultado = new String(buffer).trim();
        assertTrue(resultado.startsWith("Parte1Parte2"));
    }

    @Test
    public void testLeituraComBufferPequeno() throws CaminhoNaoEncontradoException, PermissaoException {
        String conteudo = "1234567890";
        fs.write(caminhoArquivo, usuario, false, conteudo.getBytes());

        byte[] buffer = new byte[5];
        fs.read(caminhoArquivo, usuario, buffer);

        String resultado = new String(buffer).trim();
        assertEquals("12345", resultado);
    }

    @Test
    public void testLeituraArquivoInexistente() {
        byte[] buffer = new byte[10];
        assertThrows(CaminhoNaoEncontradoException.class, () -> {
            fs.read("/inexistente.txt", usuario, buffer);
        });
    }

    @Test
    public void testEscritaSemPermissao() throws CaminhoNaoExistenteException, PermissaoException {
        String outroUsuario = "user1";
        fs.chmod(caminhoArquivo, usuario, outroUsuario, "---");

        assertThrows(PermissaoException.class, () -> {
            fs.write(caminhoArquivo, outroUsuario, false, "teste".getBytes());
        });
    }
}
