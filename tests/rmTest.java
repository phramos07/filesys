package tests;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.Test;

import exception.PermissaoException;
import filesys.FileSystemImpl;
import filesys.IFileSystem;

public class rmTest {
  @Test
  public void testRemoveArquivoSimples() throws Exception {
    IFileSystem fs = new FileSystemImpl();
    fs.mkdir("/docs", "root");
    fs.touch("/docs/a.txt", "root");
    fs.rm("/docs/a.txt", "root", false);
    // Não deve lançar exceção ao listar diretório, mas arquivo não deve estar lá
    fs.ls("/docs", "root", false);
  }

  @Test
  public void testRemoveDiretorioVazio() throws Exception {
    IFileSystem fs = new FileSystemImpl();
    fs.mkdir("/vazio", "root");
    fs.rm("/vazio", "root", false);
    fs.ls("/", "root", false);
  }

  @Test
  public void testRemoveDiretorioNaoVazioSemRecursivo() throws Exception {
    IFileSystem fs = new FileSystemImpl();
    fs.mkdir("/dir", "root");
    fs.touch("/dir/file.txt", "root");
    assertThrows(PermissaoException.class, () -> {
      fs.rm("/dir", "root", false);
    });
  }

  @Test
  public void testRemoveDiretorioNaoVazioComRecursivo() throws Exception {
    IFileSystem fs = new FileSystemImpl();
    fs.mkdir("/dir", "root");
    fs.touch("/dir/file.txt", "root");
    fs.mkdir("/dir/sub", "root");
    fs.touch("/dir/sub/abc.txt", "root");
    fs.rm("/dir", "root", true);
    fs.ls("/", "root", false);
  }

  @Test
  public void testRemoveSemPermissao() throws Exception {
    IFileSystem fs = new FileSystemImpl();
    fs.mkdir("/docs", "root");
    fs.touch("/docs/a.txt", "root");
    assertThrows(PermissaoException.class, () -> {
      fs.rm("/docs/a.txt", "bob", false);
    });
  }

  @Test
  public void testRemoveRaiz() throws Exception {
    IFileSystem fs = new FileSystemImpl();
    assertThrows(PermissaoException.class, () -> {
      fs.rm("/", "root", true);
    });
  }
}
