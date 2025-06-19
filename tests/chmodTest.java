package tests;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.Test;

import exception.CaminhoNaoEncontradoException;
import exception.PermissaoException;
import filesys.FileSystemImpl;
import filesys.IFileSystem;

public class chmodTest {
  @Test
  public void testChmodPorRoot() throws Exception {
    IFileSystem fs = new FileSystemImpl();
    fs.mkdir("/docs", "root");
    fs.touch("/docs/a.txt", "root");
    fs.chmod("/docs/a.txt", "root", "root", "rw");
    // Não deve lançar exceção
    fs.write("/docs/a.txt", "root", false, new filesys.Offset(0), "abc".getBytes());
  }

  @Test
  public void testChmodPorDono() throws Exception {
    IFileSystem fs = new FileSystemImpl();
    fs.mkdir("/mariadir", "maria");
    fs.touch("/mariadir/file.txt", "maria");
    fs.chmod("/mariadir/file.txt", "maria", "maria", "r");
    // Agora maria não pode mais escrever
    assertThrows(PermissaoException.class, () -> {
      fs.write("/mariadir/file.txt", "maria", false, new filesys.Offset(0), "x".getBytes());
    });
  }

  @Test
  public void testChmodPorNaoDono() throws Exception {
    IFileSystem fs = new FileSystemImpl();
    fs.mkdir("/mariadir", "maria");
    fs.touch("/mariadir/file.txt", "maria");
    assertThrows(PermissaoException.class, () -> {
      fs.chmod("/mariadir/file.txt", "joao", "maria", "rwx");
    });
  }

  @Test
  public void testChmodDiretorio() throws Exception {
    IFileSystem fs = new FileSystemImpl();
    fs.mkdir("/mariadir", "maria");
    fs.chmod("/mariadir", "maria", "maria", "r");
    // Maria não pode mais criar arquivos
    assertThrows(PermissaoException.class, () -> {
      fs.touch("/mariadir/novo.txt", "maria");
    });
  }

  @Test
  public void testChmodCaminhoInvalido() throws Exception {
    IFileSystem fs = new FileSystemImpl();
    assertThrows(CaminhoNaoEncontradoException.class, () -> {
      fs.chmod("/inexistente.txt", "root", "root", "rwx");
    });
  }
}
