package tests;

import filesys.FileSystem;
import filesys.Usuario;
import filesys.core.Offset;
import exception.CaminhoNaoEncontradoException;
import exception.PermissaoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class FileSystemTest {

    private FileSystem fs;
    private Map<String, Usuario> usuarios;

    @BeforeEach
    public void setup() {
        usuarios = new HashMap<>();
        usuarios.put("root", new Usuario("root", "rwx"));
        usuarios.put("maria", new Usuario("maria", "rw-"));
        usuarios.put("joao", new Usuario("joao", "rw-"));
        fs = new FileSystem(usuarios);
    }

    @Test
    public void testMkdirAndLs() throws Exception {
        fs.mkdir("/home", "root");
        fs.mkdir("/home/maria", "root");
        // Não lança exceção ao listar diretórios criados
        assertDoesNotThrow(() -> fs.ls("/home", "root", false));
        assertDoesNotThrow(() -> fs.ls("/home/maria", "root", false));
    }

    @Test
    public void testTouchAndWriteAndRead() throws Exception {
        fs.mkdir("/docs", "root");
        fs.touch("/docs/arquivo.txt", "root");
        byte[] conteudo = "Hello World".getBytes();
        fs.write("/docs/arquivo.txt", "root", false, conteudo);

        byte[] buffer = new byte[20];
        Offset offset = new Offset(0);
        fs.read("/docs/arquivo.txt", "root", buffer, offset);
        String lido = new String(buffer).trim();
        assertTrue(lido.startsWith("Hello World"));
    }

    @Test
    public void testPermissaoException() throws Exception {
        fs.mkdir("/privado", "root");
        // Maria não tem permissão de execução em /privado
        assertThrows(PermissaoException.class, () -> fs.mkdir("/privado/novo", "maria"));
    }

    @Test
    public void testChmod() throws Exception {
        fs.mkdir("/area", "root");
        // root dá permissão para maria
        fs.chmod("/area", "root", "maria", "rwx");
        // Agora maria pode criar subdiretório
        assertDoesNotThrow(() -> fs.mkdir("/area/sub", "maria"));
    }

    @Test
    public void testRm() throws Exception {
        fs.mkdir("/tmp", "root");
        fs.touch("/tmp/file.txt", "root");
        fs.rm("/tmp/file.txt", "root", false);
        // Arquivo removido, não pode ler
        assertThrows(CaminhoNaoEncontradoException.class, () -> {
            byte[] buf = new byte[10];
            fs.read("/tmp/file.txt", "root", buf, new Offset(0));
        });
    }

    @Test
    public void testMv() throws Exception {
        fs.mkdir("/dir1", "root");
        fs.touch("/dir1/file.txt", "root");
        fs.mkdir("/dir2", "root");
        fs.mv("/dir1/file.txt", "/dir2/file2.txt", "root");
        // Agora só existe em /dir2
        assertThrows(CaminhoNaoEncontradoException.class, () -> {
            byte[] buf = new byte[10];
            fs.read("/dir1/file.txt", "root", buf, new Offset(0));
        });
        assertDoesNotThrow(() -> {
            byte[] buf = new byte[10];
            fs.read("/dir2/file2.txt", "root", buf, new Offset(0));
        });
    }

    @Test
    public void testCp() throws Exception {
        fs.mkdir("/origem", "root");
        fs.touch("/origem/arq.txt", "root");
        fs.write("/origem/arq.txt", "root", false, "abc".getBytes());
        fs.mkdir("/destino", "root");
        fs.cp("/origem/arq.txt", "/destino/arq_copia.txt", "root", false);
        byte[] buf = new byte[10];
        fs.read("/destino/arq_copia.txt", "root", buf, new Offset(0));
        assertEquals("abc", new String(buf).trim());
    }

    @Test
    public void testLsRecursivo() throws Exception {
        fs.mkdir("/a", "root");
        fs.mkdir("/a/b", "root");
        fs.touch("/a/b/c.txt", "root");
        // Não lança exceção ao listar recursivamente
        assertDoesNotThrow(() -> fs.ls("/a", "root", true));
    }

    @Test
    public void testNaoPermiteRemoverRaiz() {
        assertThrows(PermissaoException.class, () -> fs.rm("/", "root", true));
    }

    @Test
    public void testNaoPermiteMoverRaiz() {
        assertThrows(Exception.class, () -> fs.mv("/", "/nova", "root"));
    }
}