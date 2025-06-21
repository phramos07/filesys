package tests;

import exception.*;
import filesys.FileSystemImpl;
import filesys.IFileSystem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class FileSystemImplTest {

    private IFileSystem fs;
    private final String rootUser = "root";
    private final String user = "mirelly";

    @BeforeEach
    public void setup() throws Exception {
        fs = new FileSystemImpl();
        // Cria um ambiente base para os testes
        fs.mkdir("/home", rootUser);
        fs.mkdir("/home/" + user, rootUser);
        fs.chmod("/home/" + user, rootUser, user, "rwx"); // Dá permissão total para o usuário em seu /home
    }

    @Test
    public void testMkdirTouchLs() throws Exception {
        // Usa o /home/mirelly criado no setup
        fs.touch("/home/" + user + "/arq.txt", user);
        // O comando ls imprime no console, o teste aqui é para garantir que não lance exceção
        assertDoesNotThrow(() -> fs.ls("/home", rootUser, false));
        assertDoesNotThrow(() -> fs.ls("/home", rootUser, true));
    }

    @Test
    public void testTouchDuplicado() throws Exception {
        fs.mkdir("/d", "root");
        fs.chmod("/d", "root", user, "rwx");
        fs.touch("/d/a.txt", user);
        assertThrows(CaminhoJaExistenteException.class, () -> {
            fs.touch("/d/a.txt", user);
        });
    }

    @Test
    public void testPermissaoEscrita() throws Exception {
        fs.mkdir("/priv", "root");
        fs.chmod("/priv", "root", user, "r--"); // Apenas leitura
        assertThrows(PermissaoException.class, () -> {
            fs.touch("/priv/novo.txt", user);
        });
    }

    @Test
    public void testChmodPermissao() throws Exception {
        fs.mkdir("/t", "root");
        fs.chmod("/t", "root", user, "rwx");
        fs.touch("/t/x.txt", user);
        // Root e o dono (mirelly) podem alterar permissões
        assertDoesNotThrow(() -> fs.chmod("/t/x.txt", rootUser, "outro", "rw-"));
        assertDoesNotThrow(() -> fs.chmod("/t/x.txt", user, "outro", "rwx"));
    }

    @Test
    public void testChmodSemPermissao() throws Exception {
        fs.mkdir("/abc", "root");
        fs.chmod("/abc", "root", user, "rwx");
        fs.touch("/abc/y.txt", user);
        // Um terceiro usuário não pode alterar a permissão
        assertThrows(PermissaoException.class, () -> {
            fs.chmod("/abc/y.txt", "outro_usuario", user, "rwx");
        });
    }

    @Test
    void testWriteAndReadFile() throws Exception {
        String filePath = "/home/" + user + "/teste.txt";
        fs.touch(filePath, user);

        String content = "Este é um teste de escrita e leitura de arquivo.";
        byte[] writeBuffer = content.getBytes();
        fs.write(filePath, user, false, writeBuffer);

        byte[] readBuffer = new byte[100];
        fs.read(filePath, user, readBuffer);

        String readContent = new String(readBuffer).trim();
        assertEquals(content, readContent);
    }

    @Test
    void testWriteLargeFile() throws Exception {
        String filePath = "/home/" + user + "/grande.txt";
        fs.touch(filePath, user);

        // Cria um conteúdo com 3000 bytes, o que deve gerar 3 blocos (1024 * 2 + 952)
        StringBuilder sb = new StringBuilder(3000);
        for(int i = 0; i < 300; i++) {
            sb.append("0123456789");
        }
        byte[] largeBuffer = sb.toString().getBytes();

        fs.write(filePath, user, false, largeBuffer);

        byte[] readBuffer = new byte[3000];
        fs.read(filePath, user, readBuffer);

        assertArrayEquals(largeBuffer, readBuffer);
    }

    @Test
    void testAppendToFile() throws Exception {
        String filePath = "/home/" + user + "/append.txt";
        fs.touch(filePath, user);

        String part1 = "Primeira parte.";
        fs.write(filePath, user, false, part1.getBytes());

        String part2 = " Segunda parte.";
        fs.write(filePath, user, true, part2.getBytes());

        byte[] readBuffer = new byte[100];
        fs.read(filePath, user, readBuffer);

        assertEquals((part1 + part2), new String(readBuffer).trim());
    }

    @Test
    void testRemoveFile() throws Exception {
        String filePath = "/home/" + user + "/para_remover.txt";
        fs.touch(filePath, user);
        fs.rm(filePath, user, false);

        assertThrows(CaminhoNaoEncontradoException.class, () -> fs.ls(filePath, user, false));
    }

    @Test
    void testRemoveNonEmptyDirFailsWithoutRecursive() throws Exception {
        String dirPath = "/home/" + user + "/docs";
        fs.mkdir(dirPath, user);
        fs.touch(dirPath + "/doc1.txt", user);

        assertThrows(DiretorioNaoVazioException.class, () -> fs.rm(dirPath, user, false));
    }

    @Test
    void testRemoveDirRecursively() throws Exception {
        String dirPath = "/home/" + user + "/docs_rec";
        fs.mkdir(dirPath, user);
        fs.touch(dirPath + "/doc1.txt", user);
        fs.mkdir(dirPath + "/subdir", user);
        fs.touch(dirPath + "/subdir/doc2.txt", user);

        fs.rm(dirPath, user, true);

        assertThrows(CaminhoNaoEncontradoException.class, () -> fs.ls(dirPath, user, true));
    }

    @Test
    void testMoveFile() throws Exception {
        String oldPath = "/home/" + user + "/original.txt";
        String newPath = "/home/novo.txt";
        fs.touch(oldPath, user);
        
        // Concede permissão de escrita ao usuário "mirelly" em /home
        fs.chmod("/home", rootUser, user, "rwx");

        fs.mv(oldPath, newPath, user);

        assertThrows(CaminhoNaoEncontradoException.class, () -> fs.ls(oldPath, user, false));
        assertDoesNotThrow(() -> fs.ls(newPath, rootUser, false));
    }

    @Test
    void testRenameFile() throws Exception {
        String oldPath = "/home/" + user + "/antigo.txt";
        String newPath = "/home/" + user + "/novo_nome.txt";
        fs.touch(oldPath, user);

        // Concede permissão de escrita ao usuário "mirelly" em /home
        fs.chmod("/home", rootUser, user, "rwx");
        fs.mv(oldPath, newPath, user);

        assertThrows(CaminhoNaoEncontradoException.class, () -> fs.ls(oldPath, user, false));
        assertDoesNotThrow(() -> fs.ls(newPath, user, false));
    }

    @Test
    void testMoveDirectory() throws Exception {
        String oldPath = "/home/" + user + "/pasta_movida";
        String newPath = "/home/pasta_movida";
        fs.mkdir(oldPath, user);
        fs.touch(oldPath + "/file.txt", user);
                
        // Concede permissão de escrita ao usuário "mirelly" em /home
        fs.chmod("/home", rootUser, user, "rwx");
        fs.mv(oldPath, newPath, user);

        assertThrows(CaminhoNaoEncontradoException.class, () -> fs.ls(oldPath, user, false));
        assertDoesNotThrow(() -> fs.ls(newPath, rootUser, false));
        assertDoesNotThrow(() -> fs.ls(newPath + "/file.txt", rootUser, false));
    }

    @Test
    void testCopyFile() throws Exception {
        String sourcePath = "/home/" + user + "/fonte.txt";
        String destPath = "/home/copia.txt";
        fs.touch(sourcePath, user);
        String content = "conteudo para copiar";
        fs.write(sourcePath, user, false, content.getBytes());
                
        // Concede permissão de escrita ao usuário "mirelly" em /home
        fs.chmod("/home", rootUser, user, "rwx");
        fs.cp(sourcePath, destPath, user, false);

        byte[] readBuffer = new byte[50];
        fs.read(destPath, rootUser, readBuffer);
        assertEquals(content, new String(readBuffer).trim());

        // Verifica que o original ainda existe
        assertDoesNotThrow(() -> fs.ls(sourcePath, user, false));
    }

    @Test
    void testCopyDirectoryRecursively() throws Exception {
        String sourcePath = "/home/" + user + "/dir_fonte";
        String destPath = "/home/dir_copia";
        fs.mkdir(sourcePath, user);
        fs.touch(sourcePath + "/file1.txt", user);
        fs.mkdir(sourcePath + "/subdir", user);
        fs.touch(sourcePath + "/subdir/file2.txt", user);
                
        // Concede permissão de escrita ao usuário "mirelly" em /home
        fs.chmod("/home", rootUser, user, "rwx");
        fs.cp(sourcePath, destPath, user, true);

        assertDoesNotThrow(() -> fs.ls(destPath, rootUser, false));
        assertDoesNotThrow(() -> fs.ls(destPath + "/file1.txt", rootUser, false));
        assertDoesNotThrow(() -> fs.ls(destPath + "/subdir", rootUser, false));
        assertDoesNotThrow(() -> fs.ls(destPath + "/subdir/file2.txt", rootUser, false));
    }

    @Test
    void testCopyDirectoryFailsWithoutRecursive() throws Exception {
        String sourcePath = "/home/" + user + "/dir_fonte2";
        String destPath = "/home/dir_copia2";
        fs.mkdir(sourcePath, user);

        assertThrows(PermissaoException.class, () -> fs.cp(sourcePath, destPath, user, false));
    }
}