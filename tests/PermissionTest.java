package tests;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import filesys.FileSystemImpl;
import filesys.IFileSystem;
import core.Usuario;
import exception.PermissaoException;

public class PermissionTest {
    private static IFileSystem sistema;
    static List<Usuario> usuarios = new ArrayList<>();

    @BeforeAll
    public static void preparar() {
        sistema = new FileSystemImpl(usuarios);
    }

    @Test
    public void rootSemprePodeCriarDiretorio() {
        // O usuário root deve conseguir criar qualquer diretório
        assertDoesNotThrow(() -> sistema.mkdir("/diretorio", "root"));
    }

    @Test
    public void leituraDiretorio_RespeitaPermissao() throws Exception {
        sistema.mkdir("/area", "root");
        sistema.touch("/area/doc.txt", "root");

        // root tem leitura garantida
        assertDoesNotThrow(() -> sistema.ls("/area", "root", false));

        // Retira permissão de leitura do usuário joao
        sistema.chmod("/area", "root", "joao", "---");
        assertThrows(PermissaoException.class, () -> sistema.ls("/area", "joao", false));

        // Concede permissão de leitura novamente
        sistema.chmod("/area", "root", "joao", "r--");
        assertDoesNotThrow(() -> sistema.ls("/area", "joao", false));
    }

    @Test
    public void escritaArquivo_RespeitaPermissao() throws Exception {
        sistema.mkdir("/privado", "root");

        // root pode criar normalmente
        assertDoesNotThrow(() -> sistema.touch("/privado/novo.txt", "root"));

        // Bloqueia escrita para joao
        sistema.chmod("/privado", "root", "joao", "r--");
        assertThrows(PermissaoException.class, () -> sistema.touch("/privado/joao.txt", "joao"));

        // Libera escrita para joao
        sistema.chmod("/privado", "root", "joao", "rw-");
        assertDoesNotThrow(() -> sistema.touch("/privado/joao2.txt", "joao"));
    }

    @Test
    public void execucaoDiretorio_RespeitaPermissao() throws Exception {
        sistema.mkdir("/execs", "root");

        // Remove permissão de execução de joao
        sistema.chmod("/execs", "root", "joao", "rw-");
        // Não deve conseguir criar subdiretório sem 'x'
        assertThrows(PermissaoException.class, () -> sistema.mkdir("/execs/sub", "joao"));

        // Permite execução para joao
        sistema.chmod("/execs", "root", "joao", "rwx");
        assertDoesNotThrow(() -> sistema.mkdir("/execs/sub", "joao"));
    }

    @Test
    public void herancaPermissaoDiretorio() throws Exception {
        sistema.mkdir("/topo", "root");
        sistema.mkdir("/topo/ramo", "root");

        // Permissão concedida apenas no topo
        sistema.chmod("/topo", "root", "joao", "rwx");
        // Deve herdar permissão do topo
        assertDoesNotThrow(() -> sistema.mkdir("/topo/ramo/folha", "joao"));

        // Remove permissão do topo, concede só no ramo
        sistema.chmod("/topo", "root", "joao", "---");
        sistema.chmod("/topo/ramo", "root", "joao", "rwx");
        // Agora só pode criar em ramo, não em topo
        assertThrows(PermissaoException.class, () -> sistema.mkdir("/topo/novo", "joao"));
        assertDoesNotThrow(() -> sistema.mkdir("/topo/ramo/novo", "joao"));
    }
}