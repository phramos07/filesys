package tests;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import exception.CaminhoJaExistenteException;

public class MkdirTest extends FileSystemTestBase {

  @Test
  public void testCriarDiretorio() throws Exception {
    fileSystem.mkdir("/rootTest", ROOT_USER);
  }

  @Test
  public void testCriarDiretorioJaExistente() throws Exception {
    fileSystem.mkdir("/home", "root");
    assertThrows(CaminhoJaExistenteException.class, () -> fileSystem.mkdir("/home", "root"));
  }

  @Test
  public void testCriarDiretorioComPai() throws Exception {
    fileSystem.mkdir("/pai", "root");
    assertDoesNotThrow(() -> fileSystem.mkdir("/pai/filho", "root"));
  }
}
