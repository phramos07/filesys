package tests;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;
import exception.*;

public class ReadTest extends FileSystemTestBase {
  @BeforeEach
  public void prepararAmbienteTeste() throws Exception {
    fileSystem.mkdir("/testes", ROOT_USER);
    fileSystem.touch("/testes/arquivo1.txt", ROOT_USER);
    fileSystem.write("/testes/arquivo1.txt", ROOT_USER, false, "abcde".getBytes());
  }

  @AfterEach
  public void limparAmbienteTeste() throws Exception {
    try {
      fileSystem.rm("/testes", ROOT_USER, true);
    } catch (Exception e) {
    }
  }

  @Test
  public void lerArquivoSimples() {
    offset.setValue(0);
    assertDoesNotThrow(() -> fileSystem.read("/testes/arquivo1.txt", ROOT_USER, buffer, offset));
  }

  @Test
  public void lerArquivoSemPermissao() {
    offset.setValue(0);
    assertThrows(PermissaoException.class, () -> fileSystem.read("/testes/arquivo1.txt", "joao", buffer, offset));
  }

  @Test
  public void lerArquivoInexistente() {
    offset.setValue(0);
    assertThrows(CaminhoNaoEncontradoException.class,
        () -> fileSystem.read("/testes/jooj.txt", ROOT_USER, buffer, offset));
  }

  @Test
  public void lerComOffset() throws Exception {
    offset.setValue(2);
    assertDoesNotThrow(() -> fileSystem.read("/testes/arquivo1.txt", ROOT_USER, buffer, offset));
  }
}
