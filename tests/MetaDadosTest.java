package tests;

import filesys.MetaDados;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MetaDadosTest {

@Test
public void testConstrutorEGetters() {
    MetaDados md = new MetaDados("arquivo.txt", "mirelly");
    assertEquals("arquivo.txt", md.getNome());
    assertEquals("mirelly", md.getDono());
    assertEquals(0, md.getTamanho());
    assertEquals("rwx", md.getPermissao("mirelly"));
}

@Test
public void testSetters() {
    MetaDados md = new MetaDados("a", "b");
    md.setNome("novo");
    md.setDono("root");
    md.setTamanho(42);
    assertEquals("novo", md.getNome());
    assertEquals("root", md.getDono());
    assertEquals(42, md.getTamanho());
}

@Test
public void testPermissoesPadrao() {
    MetaDados md = new MetaDados("arq", "user1");
    assertEquals("rwx", md.getPermissao("user1"));
    assertEquals("---", md.getPermissao("user2"));
}

@Test
public void testSetPermissao() {
    MetaDados md = new MetaDados("arq", "user1");
    md.setPermissao("user2", "rw-");
    assertEquals("rw-", md.getPermissao("user2"));
}

@Test
public void testHasPermissao() {
    MetaDados md = new MetaDados("arq", "user1");
    md.setPermissao("user2", "rw-");
    assertTrue(md.hasPermissao("user1", 'r'));
    assertTrue(md.hasPermissao("user2", 'w'));
    assertFalse(md.hasPermissao("user2", 'x'));
    assertTrue(md.hasPermissao("root", 'x'));
}
}