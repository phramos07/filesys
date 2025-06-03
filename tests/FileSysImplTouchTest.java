package tests;
import filesys.Arquivo;
import filesys.Diretorio;
import filesys.FileSystemImpl;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import exception.CaminhoJaExistenteException;
import exception.PermissaoException;

class FileSystemImplTouchTest {

    private FileSystemImpl fsImpl;

    @BeforeEach
    void setUp() {
        // Inicializar um FileSystemImpl limpo antes de cada teste
        fsImpl = new FileSystemImpl();
    }

    /**
     * 1) Teste de criação bem‐sucedida:
     *    - Cria primeiro um diretório "/docs"
     *    - Executa touch("/docs/arquivo.txt", "root")
     *    - Verifica que "arquivo.txt" exista na lista de arquivos de "/docs"
     */
    @Test
    void testTouchSuccess() throws Exception {
        // (1) Cria o diretório "/docs"
        fsImpl.mkdir("/", "docs");
        
        // (2) Garante que "/docs" existe como diretório
        Diretorio raiz = fsImpl.getFileSys().getRaiz();
        Diretorio dirDocs = null;
        for (Diretorio d : raiz.getSubDirs()) {
            if (d.getMetaDados().getNome().equals("docs")) {
                dirDocs = d;
                break;
            }
        }
        assertNotNull(dirDocs, "Diretório '/docs' deveria existir após mkdir.");

        // (3) Executa touch dentro de "/docs"
        fsImpl.touch("/docs/arquivo.txt", "root");

        // (4) Verifica que "arquivo.txt" apareceu em dirDocs.getArquivos()
        boolean encontrou = false;
        for (Arquivo arq : dirDocs.getArquivos()) {
            if (arq.getMetaDados().getNome().equals("arquivo.txt")) {
                encontrou = true;
                // Também podemos checar que tamanho == 0
                assertEquals(0, arq.getMetaDados().getTamanho(),
                             "Arquivo recém‐criado deve ter tamanho 0.");
                // E que o dono é "root"
                assertEquals("root", arq.getMetaDados().getDono(),
                             "Dono do arquivo deve ser 'root'.");
                // E que há permissão "rw" para "root"
                assertTrue(arq.getMetaDados().hasPermissao("root", "r"),
                           "Deve ter permissão de leitura (r) para 'root'.");
                assertTrue(arq.getMetaDados().hasPermissao("root", "w"),
                           "Deve ter permissão de escrita (w) para 'root'.");
                break;
            }
        }
        assertTrue(encontrou, "Esperava‐se que '/docs/arquivo.txt' estivesse presente após touch().");
    }

    /**
     * 2) Quando o diretório‐pai não existir, deve lançar CaminhoJaExistenteException
     *    com mensagem "Caminho não encontrado: <pai>"
     */
    @Test
    void testTouchParentNotFound() {
        CaminhoJaExistenteException ex = assertThrows(
            CaminhoJaExistenteException.class,
            () -> fsImpl.touch("/naoExiste/arquivo.txt", "root")
        );
        assertEquals(
            "Caminho não encontrado: /naoExiste",
            ex.getMessage(),
            "Deve lançar CaminhoJaExistenteException informando que '/naoExiste' não existe."
        );
    }

    /**
     * 3) Se já existir um diretório com mesmo nome (por exemplo, tocar em "/dupdir"),
     *    deve lançar CaminhoJaExistenteException.
     *
     *    Explicação: chamar touch("/dupdir", "root") equivale a:
     *      - pai = "/"
     *      - nomeArquivo = "dupdir"
     *    Se já houver subdiretório chamado "dupdir" em "/", então deve falhar.
     */
    @Test
    void testTouchAlreadyExistsAsDirectory() throws Exception {
        // (1) Cria um diretório "/dupdir"
        fsImpl.mkdir("/", "dupdir");

        // (2) Tenta tocar em "/dupdir" → deveria entrar nessa checagem de conflito com diretório
        CaminhoJaExistenteException ex = assertThrows(
            CaminhoJaExistenteException.class,
            () -> fsImpl.touch("/dupdir", "root")
        );
        assertEquals(
            "Já existe arquivo ou diretório chamado 'dupdir' em: /",
            ex.getMessage(),
            "Ao tocar em caminho que conflita com um diretório, deve lançar CaminhoJaExistenteException."
        );
    }

    /**
     * 4) Se já existir um arquivo com mesmo nome, deve lançar CaminhoJaExistenteException.
     *
     *    Primeiro criamos "/docs2" e dentro dele um arquivo "a.txt", depois
     *    chamamos touch("/docs2/a.txt", "root") novamente.
     */
    @Test
    void testTouchAlreadyExistsAsFile() throws Exception {
        // (1) Cria diretório "/docs2"
        fsImpl.mkdir("/", "docs2");
        
        // (2) Executa touch("/docs2/a.txt", "root") para criar o arquivo
        fsImpl.touch("/docs2/a.txt", "root");

        // (3) Agora tentar touch("/docs2/a.txt", "root") novamente → já existe arquivo
        CaminhoJaExistenteException ex = assertThrows(
            CaminhoJaExistenteException.class,
            () -> fsImpl.touch("/docs2/a.txt", "root")
        );
        assertEquals(
            "Já existe arquivo ou diretório chamado 'a.txt' em: /docs2",
            ex.getMessage(),
            "Quando o arquivo já existe, deve lançar CaminhoJaExistenteException."
        );
    }

    /**
     * 5) Permissão negada: se removermos a permissão 'w' do usuário no diretório, touch deve falhar.
     *
     *    - Cria "/noPermDir"
     *    - Remove permissão "w" de "root" dentro de "/noPermDir"
     *    - Chama touch("/noPermDir/novoArq", "root") → PermissaoException
     */
    @Test
    void testTouchPermissionDenied() throws Exception {
        // (1) Cria o diretório "/noPermDir"
        fsImpl.mkdir("/", "noPermDir");

        // (2) Localiza o objeto Diretório "/noPermDir"
        Diretorio raiz = fsImpl.getFileSys().getRaiz();
        Diretorio dirNoPerm = null;
        for (Diretorio d : raiz.getSubDirs()) {
            if (d.getMetaDados().getNome().equals("noPermDir")) {
                dirNoPerm = d;
                break;
            }
        }
        assertNotNull(dirNoPerm, "O diretório '/noPermDir' deveria existir.");

        // (3) Retira permissão 'w' de "root" naquele diretório
        HashMap<String, String> mapaPerm = dirNoPerm.getMetaDados().getPermissoes();
        // Substitui "rwx" por "rx" (sem o 'w')
        mapaPerm.put("root", "rx");

        // (4) Agora tentar criar um arquivo dentro de "/noPermDir" deve falhar
        PermissaoException pe = assertThrows(
            PermissaoException.class,
            () -> fsImpl.touch("/noPermDir/novoArq.txt", "root")
        );
        assertEquals(
            "Usuário 'root' não tem permissão de escrita em: /noPermDir",
            pe.getMessage(),
            "Quando não há permissão de escrita, touch deve lançar PermissaoException."
        );
    }
}
