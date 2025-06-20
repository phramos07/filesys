package tests;

import exception.CaminhoJaExistenteException;
import exception.CaminhoNaoEncontradoException;
import exception.PermissaoException;
import filesys.FileSystemImpl;
import model.Usuario;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileSystemImplTest {
    private FileSystemImpl fs;
    List<Usuario> usuarios = new ArrayList<>();

    @BeforeEach
    void setUp() {
        fs = new FileSystemImpl(usuarios);
    }

    @Test
    void mkdir_CriaDiretorioComSucesso() throws Exception {
        fs.mkdir("/docs", "root");
        assertDoesNotThrow(() -> fs.mkdir("/docs2", "root"));
    }

    @Test
    void mkdir_LancaExcecaoSeDiretorioJaExiste() throws Exception {
        fs.mkdir("/docs", "root");
        assertThrows(CaminhoJaExistenteException.class, () -> fs.mkdir("/docs", "root"));
    }

    @Test
    void mkdir_LancaExcecaoSeUsuarioSemPermissao() throws Exception {
        fs.mkdir("/docs", "root");
        assertThrows(PermissaoException.class, () -> fs.mkdir("/docs/privado", "usuario"));
    }

    @Test
    void touch_CriaArquivoComSucesso() throws Exception {
        fs.mkdir("/docs", "root");
        fs.touch("/docs/arquivo.txt", "root");
        assertThrows(CaminhoJaExistenteException.class, () -> fs.touch("/docs/arquivo.txt", "root"));
    }

    @Test
    void touch_LancaExcecaoSeUsuarioSemPermissao() throws Exception {
        fs.mkdir("/docs", "root");
        assertThrows(PermissaoException.class, () -> fs.touch("/docs/novo.txt", "usuario"));
    }

    @Test
    void chmod_AlteraPermissaoComSucesso() throws Exception {
        fs.mkdir("/docs", "root");
        assertDoesNotThrow(() -> fs.chmod("/docs", "root", "usuario", "rwx"));
    }

    @Test
    void chmod_LancaExcecaoSeUsuarioSemPermissao() throws Exception {
        fs.mkdir("/docs", "root");
        assertThrows(PermissaoException.class, () -> fs.chmod("/docs", "usuario", "usuario", "rwx"));
    }

    @Test
    void chmod_LancaExcecaoSeCaminhoNaoExiste() {
        assertThrows(CaminhoNaoEncontradoException.class, () -> fs.chmod("/naoexiste", "root", "root", "rwx"));
    }

    @Test
    void rm_RemoveArquivoComSucesso() throws Exception {
        fs.mkdir("/docs", "root");
        fs.touch("/docs/arquivo.txt", "root");
        assertDoesNotThrow(() -> fs.rm("/docs/arquivo.txt", "root", false));
        // Verifica se realmente foi removido
        assertThrows(exception.CaminhoNaoEncontradoException.class, () -> {
            fs.rm("/docs/arquivo.txt", "root", false);
        });
    }

    @Test
    void rm_RemoveDiretorioRecursivo() throws Exception {
        fs.mkdir("/docs", "root");
        fs.mkdir("/docs/sub", "root");
        fs.touch("/docs/sub/arquivo.txt", "root");
        assertDoesNotThrow(() -> fs.rm("/docs", "root", true));
        assertThrows(exception.CaminhoNaoEncontradoException.class, () -> {
            fs.rm("/docs", "root", false);
        });
    }

    @Test
    void rm_LancaExcecaoSeDiretorioNaoVazioSemRecursivo() throws Exception {
        fs.mkdir("/docs", "root");
        fs.touch("/docs/arquivo.txt", "root");
        assertThrows(exception.PermissaoException.class, () -> fs.rm("/docs", "root", false));
    }

    @Test
    void write_EscritaSimplesEmArquivo() throws Exception {
        fs.mkdir("/docs", "root");
        fs.touch("/docs/arquivo.txt", "root");
        byte[] dados = "Hello, World!".getBytes();
        fs.write("/docs/arquivo.txt", "root", false, dados);
        model.Arquivo arq = (model.Arquivo) fs.navegarParaTeste("/docs/arquivo.txt");
        assertEquals(dados.length, arq.getTamanho());
    }

    @Test
    void write_AppendEmArquivo() throws Exception {
        fs.mkdir("/docs", "root");
        fs.touch("/docs/arquivo.txt", "root");
        byte[] dados1 = "ABC".getBytes();
        byte[] dados2 = "DEF".getBytes();
        fs.write("/docs/arquivo.txt", "root", false, dados1);
        fs.write("/docs/arquivo.txt", "root", true, dados2);
        model.Arquivo arq = (model.Arquivo) fs.navegarParaTeste("/docs/arquivo.txt");
        assertEquals(6, arq.getTamanho());
    }

    @Test
    void read_LeituraSimplesDeArquivo() throws Exception {
        fs.mkdir("/docs", "root");
        fs.touch("/docs/arquivo.txt", "root");
        byte[] dados = "Hello, World!".getBytes();
        fs.write("/docs/arquivo.txt", "root", false, dados);

        byte[] buffer = new byte[dados.length];
        fs.read("/docs/arquivo.txt", "root", buffer);
        assertArrayEquals(dados, buffer);
    }

    @Test
    void mv_MoverArquivoComSucesso() throws Exception {
        fs.mkdir("/docs", "root");
        fs.touch("/docs/arquivo.txt", "root");
        fs.mkdir("/destino", "root");
        fs.mv("/docs/arquivo.txt", "/destino/novo.txt", "root");
        assertThrows(exception.CaminhoNaoEncontradoException.class, () -> fs.rm("/docs/arquivo.txt", "root", false));
        // Verifica se existe no novo local
        fs.rm("/destino/novo.txt", "root", false);
    }

    @Test
    void ls_ListagemSimplesDeDiretorio() throws Exception {
        fs.mkdir("/docs", "root");
        fs.touch("/docs/arquivo1.txt", "root");
        fs.touch("/docs/arquivo2.txt", "root");

        // Redireciona a saída padrão para capturar o resultado do ls
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        fs.ls("/docs", "root", false);

        System.setOut(originalOut); // Restaura saída padrão

        String output = outContent.toString();
        assertTrue(output.contains("arquivo1.txt"));
        assertTrue(output.contains("arquivo2.txt"));
    }

    @Test
    void ls_ListagemRecursiva() throws Exception {
        fs.mkdir("/docs", "root");
        fs.touch("/docs/arquivo1.txt", "root");
        fs.mkdir("/docs/subdir", "root");
        fs.touch("/docs/subdir/arquivo2.txt", "root");

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        fs.ls("/docs", "root", true);

        System.setOut(originalOut);

        String output = outContent.toString();
        assertTrue(output.contains("arquivo1.txt"));
        assertTrue(output.contains("subdir/"));
        assertTrue(output.contains("arquivo2.txt"));
    }

    @Test
    void cp_CopiaArquivoComSucesso() throws Exception {
        fs.mkdir("/docs", "root");
        fs.touch("/docs/arquivo.txt", "root");
        byte[] dados = "abc".getBytes();
        fs.write("/docs/arquivo.txt", "root", false, dados);

        fs.cp("/docs/arquivo.txt", "/docs/copia.txt", "root", false);

        model.Arquivo arq = (model.Arquivo) fs.navegarParaTeste("/docs/copia.txt");
        assertEquals(dados.length, arq.getTamanho());
    }

    @Test
    void cp_CopiaDiretorioRecursivo() throws Exception {
        fs.mkdir("/docs", "root");
        fs.touch("/docs/arquivo.txt", "root");
        fs.mkdir("/docs/subdir", "root");
        fs.touch("/docs/subdir/arquivo2.txt", "root");
        fs.cp("/docs", "/backup", "root", true);

        // Verifica se os arquivos foram copiados
        model.Arquivo arq1 = (model.Arquivo) fs.navegarParaTeste("/backup/arquivo.txt");
        model.Arquivo arq2 = (model.Arquivo) fs.navegarParaTeste("/backup/subdir/arquivo2.txt");
        assertNotNull(arq1);
        assertNotNull(arq2);
    }
}
