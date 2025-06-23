package filesys.util;

import java.util.Set;

import exception.PermissaoException;
import filesys.Usuario;

public class UsuarioUtil {

  private UsuarioUtil() {
  }

  public static Usuario buscarUsuario(String nome, Set<Usuario> users) throws PermissaoException {
    return users.stream()
        .filter(u -> u.getNome().equals(nome))
        .findFirst()
        .orElseThrow(() -> new PermissaoException("Usuário não encontrado: " + nome));
  }
}