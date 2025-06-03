package tests;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import exception.CaminhoJaExistenteException;
import exception.CaminhoNaoEncontradoException;
import exception.PermissaoException;
import filesys.FileSystemImpl;
import filesys.IFileSystem;
import model.Usuario;

public class PermissionTest {
    private static IFileSystem fileSystem;
   static List<Usuario> usuarios = new ArrayList<>();

    @BeforeAll
    public static void setUp() {
        fileSystem = new FileSystemImpl(usuarios);
    }

    @Test
    public void testPermission() {
        // Teste básico de permissão: root sempre pode criar diretório
        assertDoesNotThrow(() -> fileSystem.mkdir("/test", "root"));
    }

    @Test
    public void testReadPermission() throws Exception {
        fileSystem.mkdir("/docs", "root");
        fileSystem.touch("/docs/arquivo.txt", "root");

        // root pode listar
        assertDoesNotThrow(() -> fileSystem.ls("/docs", "root", false));

        // chmod para negar leitura a outro usuário
        fileSystem.chmod("/docs", "root", "joao", "---");
        assertThrows(PermissaoException.class, () -> fileSystem.ls("/docs", "joao", false));

        // chmod para permitir leitura
        fileSystem.chmod("/docs", "root", "joao", "r--");
        assertDoesNotThrow(() -> fileSystem.ls("/docs", "joao", false));
    }

    @Test
    public void testWritePermission() throws Exception {
        fileSystem.mkdir("/priv", "root");

        // root pode criar arquivo
        assertDoesNotThrow(() -> fileSystem.touch("/priv/novo.txt", "root"));

        // chmod para negar escrita a joao
        fileSystem.chmod("/priv", "root", "joao", "r--");
        assertThrows(PermissaoException.class, () -> fileSystem.touch("/priv/joao.txt", "joao"));

        // chmod para permitir escrita
        fileSystem.chmod("/priv", "root", "joao", "rw-");
        assertDoesNotThrow(() -> fileSystem.touch("/priv/joao2.txt", "joao"));
    }

    @Test
    public void testExecutePermission() throws Exception {
        fileSystem.mkdir("/exec", "root");

        // chmod para negar execução a joao
        fileSystem.chmod("/exec", "root", "joao", "rw-");
        // Tenta criar subdiretório (precisa de x)
        assertThrows(PermissaoException.class, () -> fileSystem.mkdir("/exec/sub", "joao"));

        // chmod para permitir execução
        fileSystem.chmod("/exec", "root", "joao", "rwx");
        assertDoesNotThrow(() -> fileSystem.mkdir("/exec/sub", "joao"));
    }

    @Test
    public void testPermissionInheritance() throws Exception {
        fileSystem.mkdir("/pai", "root");
        fileSystem.mkdir("/pai/filho", "root");

        // Permissão só no pai
        fileSystem.chmod("/pai", "root", "joao", "rwx");
        // Deve herdar permissão do pai
        assertDoesNotThrow(() -> fileSystem.mkdir("/pai/filho/nieto", "joao"));

        // Remove permissão do pai, adiciona só no filho
        fileSystem.chmod("/pai", "root", "joao", "---");
        fileSystem.chmod("/pai/filho", "root", "joao", "rwx");
        // Agora só pode criar dentro de filho, não no pai
        assertThrows(PermissaoException.class, () -> fileSystem.mkdir("/pai/novo", "joao"));
        assertDoesNotThrow(() -> fileSystem.mkdir("/pai/filho/novo", "joao"));
    }
}
