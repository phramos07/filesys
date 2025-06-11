package tests;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;
import exception.*;

public class WriteTest extends FileSystemTestBase {
    @BeforeEach
    public void prepararAmbienteTeste() throws Exception {
        fileSystem.mkdir("/testes", ROOT_USER);
        fileSystem.touch("/testes/arquivo1.txt", ROOT_USER);
    }

    @AfterEach
    public void limparAmbienteTeste() throws Exception {
        try { fileSystem.rm("/testes", ROOT_USER, true); } catch (Exception e) {}
    }

    @Test
    public void escreverArquivoSimples() {
        byte[] dados = "conteudo hahahahahah".getBytes();
        assertDoesNotThrow(() -> fileSystem.write("/testes/arquivo1.txt", ROOT_USER, false, dados));
    }

    @Test
    public void escreverArquivoSemPermissao() {
        byte[] dados = "conteudo hahahahahah".getBytes();
        assertThrows(PermissaoException.class, () -> fileSystem.write("/testes/arquivo1.txt", "joao", false, dados));
    }

    @Test
    public void escreverArquivoInexistente() {
        byte[] dados = "conteudo hahahahahah".getBytes();
        assertThrows(CaminhoNaoEncontradoException.class, () -> fileSystem.write("/testes/pedrin.txt", ROOT_USER, false, dados));
    }

    @Test
    public void anexarArquivo() throws Exception {
        byte[] dados1 = "abc".getBytes();
        byte[] dados2 = "def".getBytes();
        fileSystem.write("/testes/arquivo1.txt", ROOT_USER, false, dados1);
        assertDoesNotThrow(() -> fileSystem.write("/testes/arquivo1.txt", ROOT_USER, true, dados2));
    }
}
