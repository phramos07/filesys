package tests;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import core.Usuario;
import exception.CaminhoJaExistenteException;
import exception.CaminhoNaoEncontradoException;
import exception.PermissaoException;
import filesys.FileSystemImpl;

class FileSystemImplTest {
    private FileSystemImpl sistema;
    List<Usuario> listaUsuarios = new ArrayList<>();

    @BeforeEach
    void inicializar() {
        sistema = new FileSystemImpl(listaUsuarios);
    }

    @Test
    void criaDiretorio_RetornaSucesso() throws Exception {
        sistema.mkdir("/pasta", "root");
        assertDoesNotThrow(() -> sistema.mkdir("/outra", "root"));
    }

    @Test
    void criaDiretorio_JaExiste_LancaErro() throws Exception {
        sistema.mkdir("/pasta", "root");
        assertThrows(CaminhoJaExistenteException.class, () -> sistema.mkdir("/pasta", "root"));
    }

    @Test
    void criaDiretorio_SemPermissao_LancaErro() throws Exception {
        sistema.mkdir("/pasta", "root");
        assertThrows(PermissaoException.class, () -> sistema.mkdir("/pasta/priv", "usuario"));
    }

    @Test
    void criaArquivo_Sucesso() throws Exception {
        sistema.mkdir("/pasta", "root");
        sistema.touch("/pasta/novo.txt", "root");
        assertThrows(CaminhoJaExistenteException.class, () -> sistema.touch("/pasta/novo.txt", "root"));
    }

    @Test
    void criaArquivo_SemPermissao_LancaErro() throws Exception {
        sistema.mkdir("/pasta", "root");
        assertThrows(PermissaoException.class, () -> sistema.touch("/pasta/sempermissao.txt", "usuario"));
    }

    @Test
    void alteraPermissao_Sucesso() throws Exception {
        sistema.mkdir("/pasta", "root");
        assertDoesNotThrow(() -> sistema.chmod("/pasta", "root", "usuario", "rwx"));
    }

    @Test
    void alteraPermissao_SemPermissao_LancaErro() throws Exception {
        sistema.mkdir("/pasta", "root");
        assertThrows(PermissaoException.class, () -> sistema.chmod("/pasta", "usuario", "usuario", "rwx"));
    }

    @Test
    void alteraPermissao_CaminhoInexistente_LancaErro() {
        assertThrows(CaminhoNaoEncontradoException.class, () -> sistema.chmod("/inexistente", "root", "root", "rwx"));
    }

    @Test
    void removeArquivo_Sucesso() throws Exception {
        sistema.mkdir("/pasta", "root");
        sistema.touch("/pasta/arquivo.txt", "root");
        assertDoesNotThrow(() -> sistema.rm("/pasta/arquivo.txt", "root", false));
        assertThrows(CaminhoNaoEncontradoException.class, () -> sistema.rm("/pasta/arquivo.txt", "root", false));
    }

    @Test
    void removeDiretorio_Recursivo_Sucesso() throws Exception {
        sistema.mkdir("/pasta", "root");
        sistema.mkdir("/pasta/sub", "root");
        sistema.touch("/pasta/sub/arquivo.txt", "root");
        assertDoesNotThrow(() -> sistema.rm("/pasta", "root", true));
        assertThrows(CaminhoNaoEncontradoException.class, () -> sistema.rm("/pasta", "root", false));
    }

    @Test
    void removeDiretorio_NaoVazio_SemRecursivo_LancaErro() throws Exception {
        sistema.mkdir("/pasta", "root");
        sistema.touch("/pasta/arquivo.txt", "root");
        assertThrows(PermissaoException.class, () -> sistema.rm("/pasta", "root", false));
    }

    @Test
    void escritaArquivo_Simples() throws Exception {
        sistema.mkdir("/pasta", "root");
        sistema.touch("/pasta/arquivo.txt", "root");
        byte[] conteudo = "Teste123".getBytes();
        sistema.write("/pasta/arquivo.txt", "root", false, conteudo);
        core.Arquivo arquivo = (core.Arquivo) sistema.buscarElementoTeste("/pasta/arquivo.txt");
        assertEquals(conteudo.length, arquivo.getBytesTotais());
    }

    @Test
    void escritaArquivo_Append() throws Exception {
        sistema.mkdir("/pasta", "root");
        sistema.touch("/pasta/arquivo.txt", "root");
        byte[] parte1 = "ABC".getBytes();
        byte[] parte2 = "DEF".getBytes();
        sistema.write("/pasta/arquivo.txt", "root", false, parte1);
        sistema.write("/pasta/arquivo.txt", "root", true, parte2);
        core.Arquivo arquivo = (core.Arquivo) sistema.buscarElementoTeste("/pasta/arquivo.txt");
        assertEquals(6, arquivo.getBytesTotais());
    }

    @Test
    void leituraArquivo_Simples() throws Exception {
        sistema.mkdir("/pasta", "root");
        sistema.touch("/pasta/arquivo.txt", "root");
        byte[] conteudo = "Leitura".getBytes();
        sistema.write("/pasta/arquivo.txt", "root", false, conteudo);

        byte[] buffer = new byte[conteudo.length];
        sistema.read("/pasta/arquivo.txt", "root", buffer);
        assertArrayEquals(conteudo, buffer);
    }

    @Test
    void moverArquivo_Sucesso() throws Exception {
        sistema.mkdir("/origem", "root");
        sistema.touch("/origem/arquivo.txt", "root");
        sistema.mkdir("/destino", "root");
        sistema.mv("/origem/arquivo.txt", "/destino/novo.txt", "root");
        assertThrows(CaminhoNaoEncontradoException.class, () -> sistema.rm("/origem/arquivo.txt", "root", false));
        sistema.rm("/destino/novo.txt", "root", false);
    }

    @Test
    void listarDiretorio_Simples() throws Exception {
        sistema.mkdir("/pasta", "root");
        sistema.touch("/pasta/arq1.txt", "root");
        sistema.touch("/pasta/arq2.txt", "root");

        ByteArrayOutputStream saida = new ByteArrayOutputStream();
        PrintStream saidaOriginal = System.out;
        System.setOut(new PrintStream(saida));

        sistema.ls("/pasta", "root", false);

        System.setOut(saidaOriginal);

        String resultado = saida.toString();
        assertTrue(resultado.contains("arq1.txt"));
        assertTrue(resultado.contains("arq2.txt"));
    }

    @Test
    void listarDiretorio_Recursivo() throws Exception {
        sistema.mkdir("/pasta", "root");
        sistema.touch("/pasta/arq1.txt", "root");
        sistema.mkdir("/pasta/subpasta", "root");
        sistema.touch("/pasta/subpasta/arq2.txt", "root");

        ByteArrayOutputStream saida = new ByteArrayOutputStream();
        PrintStream saidaOriginal = System.out;
        System.setOut(new PrintStream(saida));

        sistema.ls("/pasta", "root", true);

        System.setOut(saidaOriginal);

        String resultado = saida.toString();
        assertTrue(resultado.contains("arq1.txt"));
        assertTrue(resultado.contains("subpasta"));
        assertTrue(resultado.contains("arq2.txt"));
    }

    @Test
    void copiaArquivo_Sucesso() throws Exception {
        sistema.mkdir("/pasta", "root");
        sistema.touch("/pasta/original.txt", "root");
        byte[] dados = "xyz".getBytes();
        sistema.write("/pasta/original.txt", "root", false, dados);

        sistema.cp("/pasta/original.txt", "/pasta/copia.txt", "root", false);

        core.Arquivo arquivo = (core.Arquivo) sistema.buscarElementoTeste("/pasta/copia.txt");
        assertEquals(dados.length, arquivo.getBytesTotais());
    }

    @Test
    void copiaDiretorio_Recursivo() throws Exception {
        sistema.mkdir("/pasta", "root");
        sistema.touch("/pasta/original.txt", "root");
        sistema.mkdir("/pasta/subpasta", "root");
        sistema.touch("/pasta/subpasta/arq2.txt", "root");
        sistema.cp("/pasta", "/backup", "root", true);

        core.Arquivo arq1 = (core.Arquivo) sistema.buscarElementoTeste("/backup/original.txt");
        core.Arquivo arq2 = (core.Arquivo) sistema.buscarElementoTeste("/backup/subpasta/arq2.txt");
        assertNotNull(arq1);
        assertNotNull(arq2);
    }
}