package tests;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import exception.*;
import filesys.Offset;

public class CpTest extends FileSystemTestBase {

    @BeforeEach
    public void prepararAmbienteTeste() {
        // Usando blocos try-catch para cada operação
        try {
            fileSystem.rm("/testes", ROOT_USER, true);
        } catch (Exception e) {
            // Ignora se não existir
        }

        try {
            fileSystem.mkdir("/testes", ROOT_USER);
            fileSystem.mkdir("/testes/origem", ROOT_USER);
            fileSystem.mkdir("/testes/destino", ROOT_USER);
            
            fileSystem.touch("/testes/origem/arquivo1.txt", ROOT_USER);
            fileSystem.touch("/testes/origem/arquivo2.txt", ROOT_USER);
            
            fileSystem.mkdir("/testes/origem/subpasta", ROOT_USER);
            fileSystem.touch("/testes/origem/subpasta/arquivo3.txt", ROOT_USER);
            
        } catch (Exception e) {
            fail("Falha ao preparar ambiente de teste: " + e.getMessage());
        }
    }
    @AfterEach
    public void limparAmbienteTeste() {
        try {
            fileSystem.rm("/testes", ROOT_USER, true);
        } catch (Exception e) {
            // Ignora erros na limpeza
        }
    }

    @Test
    public void copiarArquivoParaNovoLocal() {
        assertDoesNotThrow(() -> fileSystem.cp("/testes/origem/arquivo1.txt", "/testes/destino/copia_arquivo1.txt",
                ROOT_USER, false));
    }

    @Test
    public void copiarPastaSemFlagRecursivo() {
        assertThrows(PermissaoException.class,
                () -> fileSystem.cp("/testes/origem/subpasta", "/testes/destino/copia_subpasta", ROOT_USER, false));
    }

    @Test
    public void copiarPastaComFlagRecursivo() {
        assertDoesNotThrow(
                () -> fileSystem.cp("/testes/origem/subpasta", "/testes/destino/copia_subpasta", ROOT_USER, true));
    }

    @Test
    public void copiarOrigemInexistente() {
        assertThrows(CaminhoNaoEncontradoException.class,
                () -> fileSystem.cp("/inexistente", "/testes/destino/copia", ROOT_USER, false));
    }

    @Test
    public void copiarParaDestinoInexistente() {
        assertThrows(CaminhoNaoEncontradoException.class,
                () -> fileSystem.cp("/testes/origem/arquivo1.txt", "/inexistente/copia.txt", ROOT_USER, false));
    }

    @Test
    public void copiarArquivoSemPermissaoLeitura() {
        assertThrows(PermissaoException.class,
                () -> fileSystem.cp("/testes/origem/arquivo1.txt", "/testes/destino/copia.txt", "joao", false));
    }

    @Test
    public void copiarArquivoSemPermissaoEscrita() {
        assertThrows(PermissaoException.class,
                () -> fileSystem.cp("/testes/origem/arquivo1.txt", "/users/athos/copia.txt", "athos", false));
    }

    @Test
    public void copiarArquivoParaPasta() {
        assertDoesNotThrow(() -> fileSystem.cp("/testes/origem/arquivo1.txt", "/testes/destino/", ROOT_USER, false));
    }

    @Test
    public void copiarPastaComConteudo() throws Exception {
        fileSystem.cp("/testes/origem/subpasta", "/testes/destino/copia_subpasta", ROOT_USER, true);
        assertDoesNotThrow(() -> fileSystem.read("/testes/destino/copia_subpasta/arquivo3.txt", ROOT_USER,
                new byte[256], new Offset(0)));
    }

    @Test
    public void copiarPastaVazia() throws Exception {
        fileSystem.mkdir("/testes/origem/pastavazia", ROOT_USER);
        assertDoesNotThrow(
                () -> fileSystem.cp("/testes/origem/pastavazia", "/testes/destino/copia_pastavazia", ROOT_USER, true));
    }

    @Test
    public void copiarArquivoGrande() throws Exception {
        byte[] dadosGrandes = new byte[1024]; // 1KB de dados
        fileSystem.touch("/testes/origem/arquivogrande.txt", ROOT_USER);
        fileSystem.write("/testes/origem/arquivogrande.txt", ROOT_USER, false, dadosGrandes);

        assertDoesNotThrow(() -> fileSystem.cp("/testes/origem/arquivogrande.txt", "/testes/destino/copia_grande.txt",
                ROOT_USER, false));
    }
}