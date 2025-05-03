package tests;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import exception.CaminhoJaExistenteException;
import exception.CaminhoNaoEncontradoException;
import exception.PermissaoException;
import filesys.FileSystemImpl;
import filesys.IFileSystem;
import filesys.Usuario;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class PermissionTest {
  private static IFileSystem fileSystem;

  @BeforeAll
  public static void setUp() throws CaminhoJaExistenteException, PermissaoException, CaminhoNaoEncontradoException {
    fileSystem = new FileSystemImpl();
    fileSystem.addUser(new Usuario("maria", "rw-", "/"));
    fileSystem.addUser(new Usuario("joao", "rwx", "/docs"));

    fileSystem.mkdir("/bin", "maria");
    fileSystem.mkdir("/bin/subdir", "maria");
  }

  @Test
  public void testReadPermission() throws CaminhoNaoEncontradoException, PermissaoException, CaminhoJaExistenteException {
    // Captura a saída do console
    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outContent));

    // Maria tem permissão de leitura no diretório raiz e em todos os subdiretórios
    fileSystem.ls("/", "maria", false);
    assertTrue(outContent.toString().contains("/:\n"));

    // Limpa a saída capturada
    outContent.reset();

    // Maria também pode listar subdiretórios como /bin
    fileSystem.ls("/bin", "maria", false);
    assertTrue(outContent.toString().contains("/bin:\n"));

    // Limpa a saída capturada
    outContent.reset();

    // João não tem permissão de leitura no diretório raiz
    assertThrows(PermissaoException.class, () -> fileSystem.ls("/", "joao", false));

    // Restaura a saída padrão
    System.setOut(System.out);
  }

  @Test
  public void testWritePermission() throws CaminhoNaoEncontradoException, PermissaoException, CaminhoJaExistenteException {
    // Captura a saída do console
    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outContent));

    fileSystem.ls("/bin", "maria", false);
    assertTrue(outContent.toString().contains("subdir"));

    // Limpa a saída capturada
    outContent.reset();

    // João não pode criar um diretório no diretório raiz
    assertThrows(PermissaoException.class, () -> fileSystem.mkdir("/bin2", "joao"));

    // Restaura a saída padrão
    System.setOut(System.out);
  }

  @Test
  public void testExecutePermission() throws CaminhoNaoEncontradoException, PermissaoException, CaminhoJaExistenteException {
    // João pode criar um arquivo em /docs porque tem permissão rwx;
    fileSystem.touch("/docs/file.txt", "joao");

    // Captura a saída do console
    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outContent));

    fileSystem.ls("/docs", "joao", false);
    assertTrue(outContent.toString().contains("file.txt"));

    // Limpa a saída capturada
    outContent.reset();

    // Maria pode criar um arquivo em qualquer lugar porque tem permissões herdadas de /
    fileSystem.touch("/docs/file2.txt", "maria");
    fileSystem.ls("/docs", "maria", false);
    assertTrue(outContent.toString().contains("file2.txt"));

    // Restaura a saída padrão
    System.setOut(System.out);
  }

  @Test
  public void testPermissionInheritance() throws CaminhoNaoEncontradoException, PermissaoException, CaminhoJaExistenteException {

    // Captura a saída do console
    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outContent));

    fileSystem.ls("/bin", "maria", false);
    assertTrue(outContent.toString().contains("subdir"));

    // Limpa a saída capturada
    outContent.reset();

    // João não pode criar um subdiretório em /bin porque não tem permissão em /
    assertThrows(PermissaoException.class, () -> fileSystem.mkdir("/bin/subdir2", "joao"));

    // Restaura a saída padrão
    System.setOut(System.out);
  }
}
