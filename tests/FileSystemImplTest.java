package tests;

import filesys.FileSystemImpl;
import exception.CaminhoJaExistenteException;
import exception.PermissaoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class FileSystemImplTest {
  private FileSystemImpl fs;

  @BeforeEach
  public void setup() {
    fs = new FileSystemImpl();
  }

  @Test
  public void testMkdirTouchLs() throws Exception {
    fs.mkdir("/home", "root");
    fs.mkdir("/home/mirelly", "root");
    fs.chmod("/home/mirelly", "root", "mirelly", "rwx");
    fs.touch("/home/mirelly/arq.txt", "mirelly");
    fs.ls("/home", "root", false);
    fs.ls("/home", "root", true);
  }

  @Test
  public void testTouchDuplicado() throws Exception {
    fs.mkdir("/d", "root");
    fs.chmod("/d", "root", "mirelly", "rwx");
    fs.touch("/d/a.txt", "mirelly");
    assertThrows(CaminhoJaExistenteException.class, () -> {
      fs.touch("/d/a.txt", "mirelly");
    });
  }

  @Test
  public void testPermissaoEscrita() throws Exception {
    fs.mkdir("/priv", "root");
    fs.chmod("/priv", "root", "mirelly", "r--");
    assertThrows(PermissaoException.class, () -> {
      fs.touch("/priv/novo.txt", "mirelly");
    });
  }

  @Test
  public void testChmodPermissao() throws Exception {
    fs.mkdir("/t", "root");
    fs.chmod("/t", "root", "mirelly", "rwx");
    fs.touch("/t/x.txt", "mirelly");
    fs.chmod("/t/x.txt", "root", "outro", "rw-");
    fs.chmod("/t/x.txt", "mirelly", "outro", "rwx");
  }

  @Test
  public void testChmodSemPermissao() throws Exception {
    fs.mkdir("/abc", "root");
    fs.chmod("/abc", "root", "mirelly", "rwx");
    fs.touch("/abc/y.txt", "mirelly");
    assertThrows(PermissaoException.class, () -> {
      fs.chmod("/abc/y.txt", "outro", "mirelly", "rwx");
    });
  }
}