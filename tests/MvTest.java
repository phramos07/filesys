package tests;

import filesys.FileSystemImpl;
import filesys.IFileSystem;

import exception.CaminhoNaoEncontradoException;
import exception.PermissaoException;

import static org.junit.Assert.assertThrows;

import org.junit.Test;

public class MvTest {

    @Test
    public void testMvArquivoComSucesso() throws Exception {
        IFileSystem fs = new FileSystemImpl();
        fs.mkdir("/docs", "root");
        fs.touch("/docs/arquivo.txt", "root");
        fs.mkdir("/backup", "root");

        fs.mv("/docs/arquivo.txt", "/backup/arquivo_movido.txt", "root");

        // Deve estar no novo lugar
        assertThrows(CaminhoNaoEncontradoException.class, () -> fs.ls("/docs/arquivo.txt", "root", false));
        fs.ls("/backup", "root", false); // Não lança exceção = sucesso
    }

    @Test
    public void testMvSemPermissaoDeEscrita() throws Exception {
        IFileSystem fs = new FileSystemImpl();
        fs.mkdir("/docs", "root");
        fs.touch("/docs/arquivo.txt", "root");
        fs.mkdir("/restrito", "root");

        // Simula usuário sem permissão (não é root e não é dono)
        assertThrows(PermissaoException.class, () -> fs.mv("/docs/arquivo.txt", "/restrito/arquivo.txt", "joao"));
    }

}
