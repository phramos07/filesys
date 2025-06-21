package tests;

import filesys.Arquivo;
import filesys.Bloco;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ArquivoTest {

@Test
public void testConstrutor() {
    Arquivo arq = new Arquivo("meu.txt", "mirelly");
    assertEquals("meu.txt", arq.getMetaDados().getNome());
    assertEquals("mirelly", arq.getMetaDados().getDono());
    assertNotNull(arq.getBlocos());
    assertEquals(0, arq.getBlocos().size());
}

@Test
public void testAdicionarBloco() {
    Arquivo arq = new Arquivo("meu.txt", "mirelly");
    Bloco b = new Bloco(8);
    arq.getBlocos().add(b);
    assertEquals(1, arq.getBlocos().size());
    assertSame(b, arq.getBlocos().get(0));
}
}