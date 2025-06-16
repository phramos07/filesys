package tests;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.Test;

import exception.PermissaoException;
import filesys.FileSystemImpl;
import filesys.IFileSystem;

public class CpTest {

    @Test
    public void testCpArquivoComSucesso() throws Exception {
        IFileSystem fs = new FileSystemImpl();
        fs.mkdir("/projetos", "root");
        fs.touch("/projetos/main.java", "root");
        fs.mkdir("/copia", "root");

        fs.cp("/projetos/main.java", "/copia/main.java", "root", false);

        // Arquivo original e cópia devem existir
        fs.ls("/projetos", "root", false);
        fs.ls("/copia", "root", false);
    }

    @Test
    public void testCpDiretorioSemRecursivo() throws Exception {
        IFileSystem fs = new FileSystemImpl();
        fs.mkdir("/projetos", "root");

        assertThrows(PermissaoException.class, () -> {
            fs.cp("/projetos", "/copia", "root", false); // faltando recursivo = false
        });
    }

}
