package tests;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import exception.CaminhoJaExistenteException;
import exception.CaminhoNaoEncontradoException;
import exception.OperacaoInvalidaException;
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
  public static void setUp()
      throws CaminhoJaExistenteException,
          PermissaoException,
          CaminhoNaoEncontradoException,
          OperacaoInvalidaException {
    fileSystem = new FileSystemImpl();
    fileSystem.addUser(new Usuario("completo", "rwx", "/"));
    fileSystem.addUser(new Usuario("leitura_execucao", "r-x", "/"));
    fileSystem.addUser(new Usuario("so_leitura", "r--", "/"));
    fileSystem.addUser(new Usuario("so_escrita", "-w-", "/"));
    fileSystem.addUser(new Usuario("so_execucao", "--x", "/"));
    fileSystem.addUser(new Usuario("nenhuma", "---", "/"));

    fileSystem.mkdir("/bin", "completo");
    fileSystem.touch("/bin/arquivo.txt", "completo");
  }

  @Test
  public void testPermissaoLeitura() throws Exception {
    // Usuário com r pode ler
    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outContent));
    fileSystem.ls("/bin", "leitura_execucao", false);
    assertTrue(outContent.toString().contains("arquivo.txt"));
    outContent.reset();

    // Usuário só com r NÃO pode ler porque não tem x para navegar
    assertThrows(PermissaoException.class, () -> fileSystem.ls("/bin", "so_leitura", false));

    // Usuário só com x NÃO pode ler porque não tem r
    assertThrows(PermissaoException.class, () -> fileSystem.ls("/bin", "so_execucao", false));

    // Usuário sem r nem x não pode ler
    assertThrows(PermissaoException.class, () -> fileSystem.ls("/bin", "so_escrita", false));
    assertThrows(PermissaoException.class, () -> fileSystem.ls("/bin", "nenhuma", false));
    System.setOut(System.out);
  }

  @Test
  public void testPermissaoEscrita() throws Exception {
    // Usuário com w pode criar arquivos/diretórios
    fileSystem.touch("/bin/novo.txt", "completo");

    // Usuário sem w não pode criar arquivos/diretórios
    assertThrows(PermissaoException.class, () -> fileSystem.touch("/bin/somenter.txt", "so_escrita"));
    assertThrows(PermissaoException.class, () -> fileSystem.touch("/bin/semw.txt", "so_leitura"));
    assertThrows(PermissaoException.class, () -> fileSystem.touch("/bin/semw2.txt", "so_execucao"));
    assertThrows(PermissaoException.class, () -> fileSystem.touch("/bin/semw3.txt", "nenhuma"));
  }

  @Test
  public void testPermissaoExecucaoNavegacao() throws Exception {
    // Usuário com x pode navegar/criar subdiretórios
    fileSystem.mkdir("/bin/subx", "completo");

    // Usuário sem x não pode navegar/criar subdiretórios
    assertThrows(PermissaoException.class, () -> fileSystem.mkdir("/bin/somentex", "so_execucao"));
    assertThrows(PermissaoException.class, () -> fileSystem.mkdir("/bin/semx", "so_leitura"));
    assertThrows(PermissaoException.class, () -> fileSystem.mkdir("/bin/semx2", "so_escrita"));
    assertThrows(PermissaoException.class, () -> fileSystem.mkdir("/bin/semx3", "nenhuma"));
  }

  @Test
  public void testPermissaoExecucaoLeituraArquivo() throws Exception {
    // Usuário sem x não pode nem ler arquivos em subdiretórios
    assertThrows(
        PermissaoException.class, () -> fileSystem.ls("/bin", "so_leitura", false)); // sem x
    assertThrows(
        PermissaoException.class, () -> fileSystem.ls("/bin", "so_escrita", false)); // sem x
    assertThrows(PermissaoException.class, () -> fileSystem.ls("/bin", "nenhuma", false)); // sem x

    // Usuário com x mas sem r NÃO pode ler (deve lançar exceção)
    assertThrows(PermissaoException.class, () -> fileSystem.ls("/bin", "so_execucao", false));
  }

  @Test
  public void testNenhumaPermissao() {
    // Usuário sem nenhuma permissão não pode fazer nada
    assertThrows(PermissaoException.class, () -> fileSystem.ls("/", "nenhuma", false));
    assertThrows(
        PermissaoException.class, () -> fileSystem.touch("/bin/arquivo_novo.txt", "nenhuma"));
    assertThrows(PermissaoException.class, () -> fileSystem.mkdir("/bin/dir_novo", "nenhuma"));
  }
}