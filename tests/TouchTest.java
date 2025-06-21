package tests;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;
import exception.*;

public class TouchTest extends FileSystemTestBase {
  @BeforeEach
  public void prepararAmbienteTeste() throws Exception {
    fileSystem.mkdir("/testes", ROOT_USER);
  }

  @AfterEach
  public void limparAmbienteTeste() throws Exception {
    try {
      fileSystem.rm("/testes", ROOT_USER, true);
    } catch (Exception e) {
    }
  }

  @Test
  public void criarArquivoSimples() {
    assertDoesNotThrow(() -> fileSystem.touch("/testes/arquivo1.txt", ROOT_USER));
  }

  @Test
  public void criarArquivoExistente() throws Exception {
    fileSystem.touch("/testes/arquivo1.txt", ROOT_USER);
    assertThrows(CaminhoJaExistenteException.class, () -> fileSystem.touch("/testes/arquivo1.txt", ROOT_USER));
  }

  @Test
  public void criarArquivoSemPermissao() {
    assertThrows(PermissaoException.class, () -> fileSystem.touch("/testes/arquivo2.txt", "joao"));
  }

  @Test
  public void criarArquivoEmDiretorioInexistente() {
    assertThrows(CaminhoNaoEncontradoException.class, () -> fileSystem.touch("/jooj/arquivo.txt", ROOT_USER));
  }
}
