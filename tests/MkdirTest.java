package tests;

import filesys.FileSystemImpl;
import filesys.Usuario;
import exception.CaminhoJaExistenteException;
import exception.PermissaoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MkdirTest {
    private FileSystemImpl fs;

    @BeforeEach
    public void setup() {
        fs = new FileSystemImpl(List.of(
                new Usuario("root", "/**", "rwx"),
                new Usuario("maria", "/**", "rwx"),
                new Usuario("joao", "/**", "rw-"),
                new Usuario("carla", "/**", "r--")));
    }

    @Test
    public void testCriarDiretorioComSucesso() {
        assertDoesNotThrow(() -> fs.mkdir("/docs", "maria"));
    }

    @Test
    public void testCriacaoRecursivaPermitida() {
        assertDoesNotThrow(() -> fs.mkdir("/nova/estrutura/teste", "maria"));
    }

    @Test
    public void testCriarDiretorioQueJaExiste() throws Exception {
        fs.mkdir("/docs", "maria");
        // tentar criar novamente o mesmo caminho deve lançar exceção
        assertThrows(CaminhoJaExistenteException.class, () -> fs.mkdir("/docs", "maria"));
    }

    @Test
    public void testCriarDiretorioSemPermissao() {
        assertThrows(PermissaoException.class, () -> fs.mkdir("/docs", "carla"));
    }

    @Test
    public void testCriarSubdiretorioDentroDeOutro() throws Exception {
        fs.mkdir("/docs", "joao");
        assertDoesNotThrow(() -> fs.mkdir("/docs/sub", "joao"));
    }

    @Test
    public void testCriarDiretorioComoRoot() {
        assertDoesNotThrow(() -> fs.mkdir("/admin", "root"));
    }

    @Test
    public void testCriarDiretorioRaizNaoPermitido() {
        assertThrows(CaminhoJaExistenteException.class, () -> fs.mkdir("/", "maria"));
    }

    @Test
    public void testCriarDiretorioComPermissaoGlobalMasSemPermissaoEscrita() {
        // carla tem apenas r-- no /** → não pode criar
        assertThrows(PermissaoException.class, () -> fs.mkdir("/apenasleitura", "carla"));
    }
}