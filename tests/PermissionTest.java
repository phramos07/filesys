package tests;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import filesys.FileSystemImpl;
import filesys.IFileSystem;
import exception.*;

public class PermissionTest {
    private IFileSystem fs;

    @BeforeEach
    public void setUp() {
        fs = new FileSystemImpl();
    }

    @Test
    public void testRootCanCreateDir() throws Exception {
        // A raiz já existe, tentar criar de novo deve lançar exceção
        assertThrows(CaminhoJaExistenteException.class, () -> {
            fs.mkdir("/", "root");
        });
        // Criar subdiretório
        fs.mkdir("/sub", "root");
    }

    @Test
    public void testUserCannotWriteWithoutPermission() {
        // Usuário joao não tem permissão na raiz
        assertThrows(PermissaoException.class, () -> {
            fs.touch("/proibido.txt", "joao");
        });
    }

    @Test
    public void testUserCanWriteAfterChmod() throws Exception {
        fs.mkdir("/home", "root");
        fs.chmod("/home", "root", "maria", "rwx");
        fs.touch("/home/arq.txt", "maria");
    }

    @Test
    public void testReadWriteFlow() throws Exception {
        fs.mkdir("/docs", "root");
        fs.touch("/docs/texto.txt", "root");
        fs.write("/docs/texto.txt", "root", false, "conteudo".getBytes());
        byte[] buffer = new byte[50];
        fs.read("/docs/texto.txt", "root", buffer);
        String lido = new String(buffer).trim().replaceAll("\0", "");
        assertTrue(lido.startsWith("conteudo"));
    }

    @Test
    public void testMvAndCp() throws Exception {
        fs.mkdir("/tmp", "root");
        fs.touch("/tmp/a.txt", "root");
        fs.write("/tmp/a.txt", "root", false, "abc".getBytes());
        fs.cp("/tmp/a.txt", "/tmp/b.txt", "root", false);
        fs.mv("/tmp/b.txt", "/tmp/c.txt", "root");
        byte[] buffer = new byte[10];
        fs.read("/tmp/c.txt", "root", buffer);
        String lido = new String(buffer).trim().replaceAll("\0", "");
        System.out.println("Conteúdo lido: [" + lido + "]"); // <-- Adicione esta linha
        assertTrue(lido.startsWith("abc"));
    }
}