package tests;

import filesys.Usuario;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class UsuarioTest {

    @Test
    public void testRootUser() {
        Usuario user = new Usuario("root", "rwx");
        assertEquals("root", user.getNome());
        assertEquals("rwx", user.getPermissao());
        assertTrue(user.podeLer());
        assertTrue(user.podeEscrever());
        assertTrue(user.podeExecutar());

        user.adicionarPermissao("/home", "rw-");
        assertEquals("rw-", user.getPermissaoParaCaminho("/home"));
        assertEquals("rwx", user.getPermissaoParaCaminho("/"));
    }

    @Test
    public void testMariaUser() {
        Usuario user = new Usuario("maria", "rw-");
        assertEquals("maria", user.getNome());
        assertEquals("rw-", user.getPermissao());
        assertTrue(user.podeLer());
        assertTrue(user.podeEscrever());
        assertFalse(user.podeExecutar());

        user.adicionarPermissao("/docs", "r--");
        assertEquals("r--", user.getPermissaoParaCaminho("/docs"));
        assertEquals("rw-", user.getPermissaoParaCaminho("/"));
    }

    @Test
    public void testJoaoUser() {
        Usuario user = new Usuario("joao", "rw-");
        assertEquals("joao", user.getNome());
        assertEquals("rw-", user.getPermissao());
        assertTrue(user.podeLer());
        assertTrue(user.podeEscrever());
        assertFalse(user.podeExecutar());
    }

    @Test
    public void testPedroUser() {
        Usuario user = new Usuario("pedro", "rw-");
        assertEquals("pedro", user.getNome());
        assertEquals("rw-", user.getPermissao());
        assertTrue(user.podeLer());
        assertTrue(user.podeEscrever());
        assertFalse(user.podeExecutar());
    }

    @Test
    public void testTiagoUser() {
        Usuario user = new Usuario("tiago", "rw-");
        assertEquals("tiago", user.getNome());
        assertEquals("rw-", user.getPermissao());
        assertTrue(user.podeLer());
        assertTrue(user.podeEscrever());
        assertFalse(user.podeExecutar());
    }

    @Test
    public void testLuziaUser() {
        Usuario user = new Usuario("luzia", "rwx");
        assertEquals("luzia", user.getNome());
        assertEquals("rwx", user.getPermissao());
        assertTrue(user.podeLer());
        assertTrue(user.podeEscrever());
        assertTrue(user.podeExecutar());
    }

    @Test
    public void testCarlaUser() {
        Usuario user = new Usuario("carla", "r--");
        assertEquals("carla", user.getNome());
        assertEquals("r--", user.getPermissao());
        assertTrue(user.podeLer());
        assertFalse(user.podeEscrever());
        assertFalse(user.podeExecutar());

        user.adicionarPermissao("/tmp", "rw-");
        assertEquals("rw-", user.getPermissaoParaCaminho("/tmp"));
        assertEquals("r--", user.getPermissaoParaCaminho("/"));
    }

    @Test
    public void testPermissaoParaCaminhoPatterns() {
        Usuario user = new Usuario("test", "---");
        user.adicionarPermissao("/home/**", "rwx");
        user.adicionarPermissao("/home/*", "rw-");
        user.adicionarPermissao("/home/user", "r--");

        // /home/user/docs should match /home/** (subpath)
        assertEquals("rwx", user.getPermissaoParaCaminho("/home/user/docs"));
        // /home/user should match /home/user (exact)
        assertEquals("r--", user.getPermissaoParaCaminho("/home/user"));
        // /home/abc should match /home/* (direct child)
        assertEquals("rw-", user.getPermissaoParaCaminho("/home/abc"));
        // / should fallback to default
        assertEquals("---", user.getPermissaoParaCaminho("/"));
    }
}