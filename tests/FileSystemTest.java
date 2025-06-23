package tests;

import exception.CaminhoJaExistenteException;
import exception.CaminhoNaoEncontradoException;
import exception.OperacaoInvalidaException;
import exception.PermissaoException;
import filesys.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FileSystemTest {

  private static IFileSystem fileSystem;
  private static Usuario user;

  @BeforeAll
  static void setUp() {
    fileSystem = new FileSystem();
  }

  @Test
  void testChmod() throws CaminhoNaoEncontradoException, PermissaoException, CaminhoJaExistenteException, OperacaoInvalidaException {
    fileSystem.mkdir("/chmodTest", "root");
    fileSystem.addUser(new Usuario("alvim", "r--", "/chmodTest"));
    assertDoesNotThrow(() -> fileSystem.chmod("/chmodTest", "root", "alvim", "rw-"));
  }

  @Test
  void testMkdir() throws CaminhoJaExistenteException, PermissaoException, CaminhoNaoEncontradoException {
    assertDoesNotThrow(() -> fileSystem.mkdir("/mkdirTest", "root"));
    assertDoesNotThrow(() -> fileSystem.mkdir("/mkdirTest2/subdir", "root"));
  }

  @Test
  void testRm() throws CaminhoNaoEncontradoException, PermissaoException, CaminhoJaExistenteException, OperacaoInvalidaException {
    fileSystem.mkdir("/rmTest", "root");
    assertDoesNotThrow(() -> fileSystem.rm("/rmTest", "root", true));
  }

  @Test
  void testTouch() throws CaminhoJaExistenteException, PermissaoException, CaminhoNaoEncontradoException, OperacaoInvalidaException {
    fileSystem.mkdir("/touchDir", "root");
    assertDoesNotThrow(() -> fileSystem.touch("/touchDir/file.txt", "root"));
    assertDoesNotThrow(() -> fileSystem.touch("/touchDir2/file2.txt", "root"));
  }

  @Test
  void testWriteAndRead() throws CaminhoNaoEncontradoException, PermissaoException, CaminhoJaExistenteException, OperacaoInvalidaException {
    fileSystem.mkdir("/rwDir", "root");
    fileSystem.touch("/rwDir/file", "root");
    byte[] buffer = new byte[]{1, 2, 3};
    fileSystem.write("/rwDir/file", "root", true, buffer);

    byte[] readBuffer = new byte[3];
    Offset offset = new Offset(0);
    fileSystem.read("/rwDir/file", "root", readBuffer, offset);
    assertArrayEquals(buffer, readBuffer);
  }

  @Test
  void testMv() throws CaminhoNaoEncontradoException, PermissaoException, CaminhoJaExistenteException, OperacaoInvalidaException {
    fileSystem.mkdir("/mvDir", "root");
    fileSystem.mv("/mvDir", "/mvDirRenamed", "root");
  }

  @Test
  void testLs() throws CaminhoNaoEncontradoException, PermissaoException, CaminhoJaExistenteException, OperacaoInvalidaException {
    fileSystem.mkdir("/lsTest", "root");
    assertDoesNotThrow(() -> fileSystem.ls("/lsTest", "root", true));
  }

  @Test
  void testCp() throws CaminhoNaoEncontradoException, PermissaoException, CaminhoJaExistenteException, OperacaoInvalidaException {
    fileSystem.mkdir("/cpSrc", "root");
    fileSystem.touch("/cpSrc/file.txt", "root");
    fileSystem.mkdir("/cpDest", "root");
    fileSystem.cp("/cpSrc", "/cpDest", "root", true);
    // poderia testar se "/cpDest" existe se houvesse método acessor
  }

  @Test
  void testAddUser() {
    assertDoesNotThrow(() -> fileSystem.addUser(new Usuario("markids", "rwx", "/")));
  }

  @Test
  void testRemoveUser() {;
    user = new Usuario("marceneiro", "---", "/");
    fileSystem.addUser(user);
    assertDoesNotThrow(() -> fileSystem.removeUser(user.getNome()));
  }
}