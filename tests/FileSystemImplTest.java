package tests;

import exception.CaminhoJaExistenteException;
import exception.CaminhoNaoEncontradoException;
import exception.PermissaoException;
import filesys.FileSystemImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FileSystemImplTest {
    private FileSystemImpl fs;

    @BeforeEach
    void setUp() {
        fs = new FileSystemImpl();
    }

    @Test
    void mkdir_CriaDiretorioComSucesso() throws Exception {
        fs.mkdir("/docs", "root");
        assertDoesNotThrow(() -> fs.mkdir("/docs2", "root"));
    }

    @Test
    void mkdir_LancaExcecaoSeDiretorioJaExiste() throws Exception {
        fs.mkdir("/docs", "root");
        assertThrows(CaminhoJaExistenteException.class, () -> fs.mkdir("/docs", "root"));
    }

    @Test
    void mkdir_LancaExcecaoSeUsuarioSemPermissao() throws Exception {
        fs.mkdir("/docs", "root");
        assertThrows(PermissaoException.class, () -> fs.mkdir("/docs/privado", "usuario"));
    }

    @Test
    void touch_CriaArquivoComSucesso() throws Exception {
        fs.mkdir("/docs", "root");
        fs.touch("/docs/arquivo.txt", "root");
        assertThrows(CaminhoJaExistenteException.class, () -> fs.touch("/docs/arquivo.txt", "root"));
    }

    @Test
    void touch_LancaExcecaoSeUsuarioSemPermissao() throws Exception {
        fs.mkdir("/docs", "root");
        assertThrows(PermissaoException.class, () -> fs.touch("/docs/novo.txt", "usuario"));
    }

    @Test
    void chmod_AlteraPermissaoComSucesso() throws Exception {
        fs.mkdir("/docs", "root");
        assertDoesNotThrow(() -> fs.chmod("/docs", "root", "usuario", "rwx"));
    }

    @Test
    void chmod_LancaExcecaoSeUsuarioSemPermissao() throws Exception {
        fs.mkdir("/docs", "root");
        assertThrows(PermissaoException.class, () -> fs.chmod("/docs", "usuario", "usuario", "rwx"));
    }

    @Test
    void chmod_LancaExcecaoSeCaminhoNaoExiste() {
        assertThrows(CaminhoNaoEncontradoException.class, () -> fs.chmod("/naoexiste", "root", "root", "rwx"));
    }

    // TODO: Adicionar testes unitários para rm
    // TODO: Adicionar testes unitários para write/read
    // TODO: Adicionar testes unitários para mv
    // TODO: Adicionar testes unitários para ls
    // TODO: Adicionar testes unitários para cp
}
