package tests;

import filesys.Diretorio;
import filesys.Arquivo;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class DiretorioTest {

@Test
public void testConstrutorEGetters() {
    Diretorio d = new Diretorio("home", "mirelly", null);
    assertEquals("home", d.getMetaDados().getNome());
    assertEquals("mirelly", d.getMetaDados().getDono());
    assertNull(d.getPai());
    assertTrue(d.getSubDiretorio().isEmpty());
    assertTrue(d.getArquivos().isEmpty());
}

@Test
public void testAddSubDiretorioEExiste() {
    Diretorio pai = new Diretorio("pai", "mirelly", null);
    Diretorio filho = new Diretorio("filho", "mirelly", pai);
    pai.addSubDiretorio(filho);
    assertTrue(pai.existeSubDiretorio("filho"));
    assertSame(filho, pai.getSubDiretorio().get("filho"));
}

@Test
public void testAddArquivoEExiste() {
    Diretorio dir = new Diretorio("docs", "mirelly", null);
    Arquivo arq = new Arquivo("a.txt", "mirelly");
    dir.addArquivo(arq);
    assertTrue(dir.existeArquivo("a.txt"));
    assertSame(arq, dir.getArquivos().get("a.txt"));
}
}