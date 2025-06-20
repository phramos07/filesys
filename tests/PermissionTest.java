package tests;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import exception.CaminhoJaExistenteException;
import exception.CaminhoNaoEncontradoException;
import exception.PermissaoException;

import org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import filesys.FileSystemImpl;
import filesys.IFileSystem;
import filesys.FileSystem;

// Essa classe testa cenários de permissão

public class PermissionTest {

    private FileSystemImpl fs;

    @BeforeEach
    public void setup() {
        fs = new FileSystemImpl();
        fs.addUser("alice");
        fs.addUser("bob");
        // Cria estrutura básica para testes
        try {
            fs.mkdir("/dir", "root");
            fs.touch("/dir/file.txt", "root");
            fs.mkdir("/dir/subdir", "root");
            // Define permissões para os usuários
            fs.permissoes.get("/dir").put("alice", "rwx");
            fs.permissoes.get("/dir/file.txt").put("alice", "rw");
            fs.permissoes.get("/dir/subdir").put("alice", "rwx");
            fs.permissoes.get("/dir").put("bob", "r");
            fs.permissoes.get("/dir/file.txt").put("bob", "r");
        } catch (Exception e) {
            fail("Setup falhou: " + e.getMessage());
        }
    }

    @Test
    public void testAddUser_andUsuarioExiste() {
        assertTrue(fs.getPermissoes().containsKey("/"));
        // Usuário root já existe por padrão
        fs.addUser("charlie");
        // Não há método direto para checar usuário, mas mkdir lança PermissaoException para usuário inválido
        assertThrows(PermissaoException.class, () -> fs.mkdir("/dirInvalidUser", "invalidUser"));
    }

    @Test
    public void testMkdirDiretorioPaiNaoExiste() {
        Exception ex = assertThrows(PermissaoException.class, () -> fs.mkdir("/noParent/dir", "root"));
        assertTrue(ex.getMessage().contains("Diretório pai inexistente"));
    }

    @Test
    public void testMkdirCaminhoJaExistente() throws Exception {
        fs.mkdir("/dirExistente", "root");
        assertThrows(CaminhoJaExistenteException.class, () -> fs.mkdir("/dirExistente", "root"));
    }

    @Test
    public void testMkdirSemPermissao() {
        fs.addUser("carol");
        Exception ex = assertThrows(PermissaoException.class, () -> fs.mkdir("/dir2", "carol"));
        assertTrue(ex.getMessage().contains("Usuário não tem permissão de escrita"));
    }

    @Test
    public void testChmodSucesso() throws Exception {
        fs.mkdir("/dirChmod", "root");
        fs.addUser("user1");
        fs.getPermissoes().get("/dirChmod").put("root", "rwx");
        fs.getPermissoes().get("/dirChmod").put("user2", "rw");
        fs.addUser("user2");
        // root pode alterar
        fs.chmod("/dirChmod", "root", "user1", "rwx");
        assertEquals("rwx", fs.getPermissoes().get("/dirChmod").get("user1"));
    }

    @Test
    public void testChmodUsuarioNaoTemPermissao() throws Exception {
        fs.mkdir("/dirChmod2", "root");
        fs.addUser("user3");
        fs.getPermissoes().get("/dirChmod2").put("user3", "r");
        fs.addUser("user4");
        Exception ex = assertThrows(PermissaoException.class, () -> fs.chmod("/dirChmod2", "user3", "user4", "rw"));
        assertTrue(ex.getMessage().contains("não tem permissão"));
    }

    @Test
    public void testChmodCaminhoNaoEncontrado() {
        assertThrows(CaminhoNaoEncontradoException.class,
                () -> fs.chmod("/noExist", "root", "user1", "rw"));
    }

    @Test
    public void testTouchSucesso() throws Exception {
        fs.mkdir("/dirTouch", "root");
        fs.touch("/dirTouch/fileTouch", "root");
        assertTrue(fs.getPermissoes().containsKey("/dirTouch/fileTouch"));
    }

    @Test
    public void testTouchCaminhoJaExistente() throws Exception {
        fs.mkdir("/dirTouch2", "root");
        fs.touch("/dirTouch2/fileTouch2", "root");
        assertThrows(CaminhoJaExistenteException.class,
                () -> fs.touch("/dirTouch2/fileTouch2", "root"));
    }

    @Test
    public void testWriteReadSucesso() throws Exception {
        fs.mkdir("/dirWriteRead", "root");
        fs.touch("/dirWriteRead/fileWriteRead", "root");
        byte[] data = "hello".getBytes();
        fs.write("/dirWriteRead/fileWriteRead", "root", false, data);
        byte[] buffer = new byte[10];
        fs.read("/dirWriteRead/fileWriteRead", "root", buffer);
        String result = new String(buffer, 0, data.length);
        assertEquals("hello", result);
    }

    @Test
    public void testWritePermissaoNegada() throws Exception {
        fs.mkdir("/dirWrite2", "root");
        fs.touch("/dirWrite2/fileWrite2", "root");
        fs.addUser("dave");
        Exception ex = assertThrows(PermissaoException.class, () -> fs.write("/dirWrite2/fileWrite2", "dave", false, "data".getBytes()));
        assertTrue(ex.getMessage().contains("não tem permissão"));
    }

    @Test
    public void testReadPermissaoNegada() throws Exception {
        fs.mkdir("/dirRead2", "root");
        fs.touch("/dirRead2/fileRead2", "root");
        fs.addUser("eve");
        Exception ex = assertThrows(PermissaoException.class, () -> fs.read("/dirRead2/fileRead2", "eve", new byte[10]));
        assertTrue(ex.getMessage().contains("não tem permissão"));
    }

    @Test
    public void testMvSucesso() throws Exception {
        fs.mkdir("/dirMv", "root");
        fs.touch("/dirMv/fileMv", "root");
        fs.mkdir("/dirDest", "root");
        fs.mv("/dirMv/fileMv", "/dirDest/fileMvNew", "root");
        assertFalse(fs.getPermissoes().containsKey("/dirMv/fileMv"));
        assertTrue(fs.getPermissoes().containsKey("/dirDest/fileMvNew"));
    }

    @Test
    public void testMvPermissaoNegada() throws Exception {
        fs.mkdir("/dirMv2", "root");
        fs.touch("/dirMv2/fileMv2", "root");
        fs.mkdir("/dirDest2", "root");
        fs.addUser("frank");
        Exception ex = assertThrows(PermissaoException.class, () -> fs.mv("/dirMv2/fileMv2", "/dirDest2/fileMv2New", "frank"));
        assertTrue(ex.getMessage().contains("não tem permissão"));
    }

    @Test
    public void testMvCaminhoAntigoNaoExiste() {
        assertThrows(CaminhoNaoEncontradoException.class,
                () -> fs.mv("/noOldPath/file", "/someNewPath/file", "root"));
    }

    @Test
    public void testMvDiretorioPaiNovoNaoExiste() throws Exception {
        fs.mkdir("/dirMv3", "root");
        fs.touch("/dirMv3/fileMv3", "root");
        Exception ex = assertThrows(PermissaoException.class,
                () -> fs.mv("/dirMv3/fileMv3", "/noParentDir/fileNew", "root"));
        assertTrue(ex.getMessage().contains("Diretório pai do caminho novo não existe"));
    }

    @Test
    public void testLs_SucessoNaoRecursivo() throws Exception {
        assertDoesNotThrow(() -> fs.ls("/dir", "alice", false));
    }

    @Test
    public void testLs_SucessoRecursivo() throws Exception {
        assertDoesNotThrow(() -> fs.ls("/dir", "alice", true));
    }

    @Test
    public void testLs_CaminhoNaoEncontrado() {
        Exception ex = assertThrows(CaminhoNaoEncontradoException.class, () -> fs.ls("/inexistente", "alice", false));
        assertTrue(ex.getMessage().contains("Caminho não encontrado"));
    }

    // ------------------- TESTES CP -------------------

    @Test
    public void testCp_ArquivoSucesso() throws Exception {
        fs.cp("/dir/file.txt", "/dir/file_copia.txt", "alice", false);
        assertTrue(fs.permissoes.containsKey("/dir/file_copia.txt"));
    }

    @Test
    public void testCp_CaminhoOrigemNaoEncontrado() {
        Exception ex = assertThrows(CaminhoNaoEncontradoException.class,
            () -> fs.cp("/dir/inexistente.txt", "/dir/file_copia.txt", "alice", false));
        assertTrue(ex.getMessage().contains("origem não encontrado"));
    }

    @Test
    public void testCp_SemPermissaoLeituraOrigem() {
        fs.permissoes.get("/dir/file.txt").put("bob", ""); // sem permissão leitura
        Exception ex = assertThrows(PermissaoException.class,
            () -> fs.cp("/dir/file.txt", "/dir/file_copia.txt", "bob", false));
        assertTrue(ex.getMessage().contains("não tem permissão para ler"));
    }

    @Test
    public void testCp_SemPermissaoEscritaDestino() {
        fs.permissoes.get("/dir").put("bob", "r"); // só leitura, sem escrita
        Exception ex = assertThrows(PermissaoException.class,
            () -> fs.cp("/dir/file.txt", "/dir/file_copia.txt", "bob", false));
        assertTrue(ex.getMessage().contains("não tem permissão para escrever"));
    }

    @Test
    public void testRead_ArquivoSucesso() throws Exception {
        byte[] buffer = new byte[10];
        fs.read("/dir/file.txt", "alice", buffer);
        // Se não lançar exceção, sucesso
    }

    @Test
    public void testRead_CaminhoNaoEncontrado() {
        byte[] buffer = new byte[10];
        Exception ex = assertThrows(CaminhoNaoEncontradoException.class,
            () -> fs.read("/dir/inexistente.txt", "alice", buffer));
        assertTrue(ex.getMessage().contains("não encontrado"));
    }

    @Test
    public void testRead_CaminhoNaoArquivo() throws Exception {
        fs.mkdir("/dir/subdir2", "alice");
        byte[] buffer = new byte[10];
        Exception ex = assertThrows(PermissaoException.class,
            () -> fs.read("/dir/subdir2", "alice", buffer));
        assertTrue(ex.getMessage().contains("não é um arquivo"));
    }

    @Test
    public void testRm_ArquivoSucesso() throws Exception {
        fs.touch("/dir/file_para_remover.txt", "alice");
        fs.rm("/dir/file_para_remover.txt", "alice", false);
        assertFalse(fs.permissoes.containsKey("/dir/file_para_remover.txt"));
    }

    @Test
    public void testRm_SemPermissao() throws Exception {
        fs.touch("/dir/file_para_remover.txt", "alice");
        fs.permissoes.get("/dir/file_para_remover.txt").put("bob", "r"); // só leitura
        Exception ex = assertThrows(PermissaoException.class,
            () -> fs.rm("/dir/file_para_remover.txt", "bob", false));
        assertTrue(ex.getMessage().contains("não tem permissão de escrita"));
    }

    @Test
    public void testRm_CaminhoNaoEncontrado() {
        Exception ex = assertThrows(CaminhoNaoEncontradoException.class,
            () -> fs.rm("/inexistente", "alice", false));
        assertTrue(ex.getMessage().contains("não encontrado"));
    }

}
    

