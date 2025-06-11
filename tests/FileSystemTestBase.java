package tests;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.junit.jupiter.api.BeforeAll;

import filesys.FileSystem;
import filesys.IFileSystem;
import filesys.Offset;
import filesys.Usuario;

public abstract class FileSystemTestBase {
    protected static IFileSystem fileSystem;
    protected static final String ROOT_USER = "root";
    protected static final String ROOT_DIR = "/";
    protected static final int READ_BUFFER_SIZE = 256;
    protected static final Offset offset = new Offset(0);
    protected static final byte[] buffer = new byte[READ_BUFFER_SIZE];

    @BeforeAll
    public static void setUp() throws Exception {
        Map<String, Usuario> usuariosMap = new HashMap<>();
        try (Scanner userScanner = new Scanner(new File("users/users"))) {
            while (userScanner.hasNextLine()) {
                String line = userScanner.nextLine().trim();
                if (!line.isEmpty()) {
                    String[] parts = line.split(" ");
                    if (parts.length == 3) {
                        String userListed = parts[0];
                        String dir = parts[1];
                        String dirPermission = parts[2];
                        Usuario usuario = usuariosMap.getOrDefault(userListed, new Usuario(userListed));
                        usuario.adicionarPermissao(dir, dirPermission);
                        usuariosMap.put(userListed, usuario);
                    }
                }
            }
        }
        fileSystem = new FileSystem(usuariosMap);
        fileSystem.mkdir(ROOT_DIR, ROOT_USER);
        fileSystem.mkdir("/users", ROOT_USER);
        fileSystem.mkdir("/users/athos", ROOT_USER);
        fileSystem.mkdir("/users/joao", ROOT_USER);
    }
}
