package tests;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;
import exception.*;
import filesys.Offset;

public class MvTest extends FileSystemTestBase {

    @BeforeEach
    public void prepararAmbienteTeste() {
        try {
            // Tenta remover se existir (ignora erros)
            try {
                fileSystem.rm("/testes", ROOT_USER, true);
            } catch (Exception e) {
                // Ignora se não existir
            }
            
            // Cria estrutura básica
            fileSystem.mkdir("/testes", ROOT_USER);
            fileSystem.mkdir("/testes/origem", ROOT_USER);
            fileSystem.mkdir("/testes/destino", ROOT_USER);
            
            // Cria arquivos e subpastas
            fileSystem.touch("/testes/origem/arquivo1.txt", ROOT_USER);
            fileSystem.mkdir("/testes/origem/subpasta", ROOT_USER);
            fileSystem.touch("/testes/origem/subpasta/arquivo2.txt", ROOT_USER);
            
        } catch (Exception e) {
            // Se falhar, marca o teste como falho com mensagem clara
            fail("FALHA CRÍTICA no preparo do ambiente: " + e.getMessage(), e);
        }
    }

    @AfterEach
    public void limparAmbienteTeste() {
        try {
            fileSystem.rm("/testes", ROOT_USER, true);
        } catch (Exception e) {
            System.err.println("Aviso: Falha ao limpar ambiente: " + e.getMessage());
        }
    }

    @Test
    public void moverArquivoParaNovoLocal() {
        assertDoesNotThrow(() -> 
            fileSystem.mv("/testes/origem/arquivo1.txt", 
                         "/testes/destino/arquivo1_movido.txt", 
                         ROOT_USER));
    }

    @Test
    public void moverPastaParaNovoLocal() {
        assertDoesNotThrow(() -> 
            fileSystem.mv("/testes/origem/subpasta", 
                         "/testes/destino/subpasta_movida", 
                         ROOT_USER));
    }


    @Test
    public void moverOrigemInexistente() {
        assertThrows(CaminhoNaoEncontradoException.class, () -> 
            fileSystem.mv("/testes/naoexiste", 
                         "/testes/destino/movido", 
                         ROOT_USER));
    }

    @Test
    public void moverSemPermissaoLeitura() {
        assertThrows(PermissaoException.class, () -> 
            fileSystem.mv("/testes/origem/arquivo1.txt", 
                         "/testes/destino/arquivo_movido.txt", 
                         "joao"));
    }

    @Test
    public void moverSemPermissaoEscritaDestino() {
        assertThrows(PermissaoException.class, () -> 
            fileSystem.mv("/testes/origem/arquivo1.txt", 
                         "/users/athos/arquivo_movido.txt", 
                         "athos"));
    }

    @Test
    public void moverParaSubpasta() {
        assertDoesNotThrow(() -> {
            fileSystem.mv("/testes/origem/arquivo1.txt", 
                         "/testes/origem/subpasta/arquivo1_movido.txt", 
                         ROOT_USER);
            // Verifica se o arquivo foi movido
            assertDoesNotThrow(() -> 
                fileSystem.read("/testes/origem/subpasta/arquivo1_movido.txt", 
                               ROOT_USER, new byte[256], new Offset(0)));
        });
    }

    @Test
    public void moverPastaComConteudo() {
        assertDoesNotThrow(() -> {
            fileSystem.mv("/testes/origem/subpasta", 
                         "/testes/destino/subpasta_movida", 
                         ROOT_USER);
            // Verifica se o conteúdo foi preservado
            assertDoesNotThrow(() -> 
                fileSystem.read("/testes/destino/subpasta_movida/arquivo2.txt", 
                               ROOT_USER, new byte[256], new Offset(0)));
        });
    }

    @Test
    public void moverParaMesmoLocal() {
        assertThrows(CaminhoJaExistenteException.class, () -> 
            fileSystem.mv("/testes/origem/arquivo1.txt", 
                         "/testes/origem/arquivo1.txt", 
                         ROOT_USER));
    }

    @Test
    public void moverRaiz() {
        assertThrows(CaminhoNaoEncontradoException.class, () -> 
            fileSystem.mv("/", "/novaraiz", ROOT_USER));
    }
}