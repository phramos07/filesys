package tests;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import exception.CaminhoNaoEncontradoException;
import exception.PermissaoException;

public class ChmodTest extends FileSystemTestBase {

  @Test
  public void testRootPodeAlterarPermissao() throws Exception {
    fileSystem.mkdir("/dirChmod", ROOT_USER);
    // root altera permissão de maria para rwx no diretório /dirChmod
    assertDoesNotThrow(() -> fileSystem.chmod("/dirChmod", ROOT_USER, "maria", "rwx"));
  }

  @Test
  public void testUsuarioComRWXAlteraPermissao() throws Exception {
    fileSystem.mkdir("/dirluzia", "luzia");
    // luzia tem rwx em /dirluzia, pode alterar permissão para joao
    assertDoesNotThrow(() -> fileSystem.chmod("/dirluzia", "luzia", "joao", "r--"));
  }

  @Test
  public void testUsuarioSemPermissaoNaoAltera() throws Exception {
    fileSystem.mkdir("/dirLuzia", ROOT_USER);
    // carla não tem permissão rw em /dirLuzia
    assertThrows(PermissaoException.class, () -> fileSystem.chmod("/dirLuzia", "carla", "joao", "rwx"));
  }

  @Test
  public void testChmodCaminhoInexistente() {
    // root tenta alterar permissão de caminho inexistente
    assertThrows(CaminhoNaoEncontradoException.class, () -> fileSystem.chmod("/naoexiste", ROOT_USER, "maria", "rwx"));
  }

  @Test
  public void testRootConcedePermissaoRWXParaUsuarioNoDiretorioRaiz() throws Exception {
    fileSystem.mkdir("/dirChmodRoot", ROOT_USER);
    // root altera permissão de maria para rwx no diretório raiz
    assertDoesNotThrow(() -> fileSystem.chmod(ROOT_DIR, ROOT_USER, "maria", "rwx"));
    // maria agora pode criar no diretório raiz
    assertDoesNotThrow(() -> fileSystem.mkdir("/dirMaria", "maria"));
  }
}
