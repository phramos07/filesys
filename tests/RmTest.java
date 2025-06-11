package tests;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;
import exception.*;

public class RmTest extends FileSystemTestBase {
  @BeforeEach
  public void prepararAmbienteTeste() throws Exception {
    fileSystem.mkdir("/testes", ROOT_USER);
    fileSystem.mkdir("/testes/origem", ROOT_USER);
    fileSystem.touch("/testes/origem/arquivo1.txt", ROOT_USER);
    fileSystem.mkdir("/testes/origem/subpasta", ROOT_USER);
    fileSystem.touch("/testes/origem/subpasta/arquivo2.txt", ROOT_USER);
  }

  @AfterEach
  public void limparAmbienteTeste() throws Exception {
    try {
      fileSystem.rm("/testes", ROOT_USER, true);
    } catch (Exception e) {
    }
  }

  @Test
  public void removerArquivoSimples() {
    assertDoesNotThrow(() -> fileSystem.rm("/testes/origem/arquivo1.txt", ROOT_USER, false));
  }

  @Test
  public void removerDiretorioRecursivo() {
    assertDoesNotThrow(() -> fileSystem.rm("/testes/origem", ROOT_USER, true));
  }

  @Test
  public void removerDiretorioSemRecursivo() {
    assertThrows(PermissaoException.class, () -> fileSystem.rm("/testes/origem", ROOT_USER, false));
  }

  @Test
  public void removerArquivoInexistente() {
    assertThrows(CaminhoNaoEncontradoException.class,
        () -> fileSystem.rm("/testes/origem/jooj.txt", ROOT_USER, false));
  }

  @Test
  public void removerSemPermissao() {
    assertThrows(PermissaoException.class, () -> fileSystem.rm("/testes/origem/arquivo1.txt", "joao", false));
  }
}
