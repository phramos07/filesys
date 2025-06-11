package tests;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;
import exception.*;

public class LsTest extends FileSystemTestBase {
  @BeforeEach
  public void prepararAmbienteTeste() throws Exception {
    fileSystem.mkdir("/testes", ROOT_USER);
    fileSystem.mkdir("/testes/dir1", ROOT_USER);
    fileSystem.touch("/testes/dir1/arquivo1.txt", ROOT_USER);
    fileSystem.mkdir("/testes/dir1/subdir", ROOT_USER);
    fileSystem.touch("/testes/dir1/subdir/arquivo2.txt", ROOT_USER);
  }

  @AfterEach
  public void limparAmbienteTeste() throws Exception {
    try {
      fileSystem.rm("/testes", ROOT_USER, true);
    } catch (Exception e) {
    }
  }

  @Test
  public void listarDiretorioSimples() {
    assertDoesNotThrow(() -> fileSystem.ls("/testes/dir1", ROOT_USER, false));
  }

  @Test
  public void listarDiretorioRecursivo() {
    assertDoesNotThrow(() -> fileSystem.ls("/testes/dir1", ROOT_USER, true));
  }

  @Test
  public void listarDiretorioSemPermissao() {
    assertThrows(PermissaoException.class, () -> fileSystem.ls("/testes/dir1", "joao", false));
  }

  @Test
  public void listarDiretorioInexistente() {
    assertThrows(CaminhoNaoEncontradoException.class, () -> fileSystem.ls("/inexistente", ROOT_USER, false));
  }
}
