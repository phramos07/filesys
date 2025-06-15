package tests;

import filesys.Bloco;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class BlocoTest {

@Test
public void testConstrutorETamanho() {
    Bloco bloco = new Bloco(10);
    assertEquals(10, bloco.getDados().length);
}

@Test
public void testSetDados() {
    Bloco bloco = new Bloco(5);
    byte[] novo = {1,2,3,4,5};
    bloco.setDados(novo);
    assertArrayEquals(novo, bloco.getDados());
}
}