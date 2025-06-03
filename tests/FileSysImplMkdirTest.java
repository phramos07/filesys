package tests;

import filesys.Diretorio;
import filesys.FileSystemImpl;
import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import exception.CaminhoJaExistenteException;
import exception.PermissaoException;

/**
 * Classe de testes JUnit 5 para o método mkdir(...) de FileSystemImpl.
 * 
 * Aborda os seguintes cenários:
 *  1) Criação bem‐sucessida de um diretório.
 *  2) Caminho‐pai não existe → CaminhoJaExistenteException.
 *  3) Já existe subdiretório com mesmo nome → CaminhoJaExistenteException.
 *  4) Permissão negada (sem 'w' no MetaDados) → PermissaoException.
 */
class FileSystemImplMkdirTest {

    private FileSystemImpl fsImpl;

    @BeforeEach
    void setUp() {
        // Inicializa um FileSystemImpl “vazio” antes de cada teste
        fsImpl = new FileSystemImpl();
    }

    /**
     * 1) Teste de criação bem‐sucessido:
     *    mkdir("/", "usr") deve criar o subdiretório “usr” imediatamente abaixo da raiz, sem lançar exceção.
     */
    @Test
    void testMkdirSuccess() throws Exception {
        // Raiz inicialmente só tem “/” e nenhuma subpasta
        Diretorio raiz = fsImpl.getFileSys().getRaiz();
        assertTrue(raiz.getSubDirs().isEmpty(), "Antes de criar, raiz não deveria ter subdiretórios.");

        // Cria "/usr"
        fsImpl.mkdir("/", "usr");

        // Após mkdir, deve existir um subdiretório com nome “usr” em raiz
        boolean encontrou = false;
        for (Diretorio d : raiz.getSubDirs()) {
            if (d.getMetaDados().getNome().equals("usr")) {
                encontrou = true;
                break;
            }
        }
        assertTrue(encontrou, "Espere‐se que o diretório '/usr' tenha sido criado com sucesso.");
    }

    /**
     * 2) Quando o caminho‐pai não existir, deve lançar CaminhoJaExistenteException com a mensagem correta.
     *    Ex.: mkdir("/inexistente", "dirnovo") → “Caminho não encontrado: /inexistente”
     */
    @Test
    void testMkdirParentNotFound() {
        CaminhoJaExistenteException ex = assertThrows(
            CaminhoJaExistenteException.class,
            () -> fsImpl.mkdir("/inexistente", "dirnovo")
        );
        assertEquals(
            "Caminho não encontrado: /inexistente",
            ex.getMessage(),
            "Deve lançar CaminhoJaExistenteException dizendo que o caminho não foi encontrado."
        );
    }

    /**
     * 3) Se já existir um subdiretório com o mesmo nome, deve lançar CaminhoJaExistenteException.
     *    Primeiro criamos "/abc", depois tentamos criar novamente "/abc".
     */
    @Test
    void testMkdirAlreadyExists() throws Exception {
        // Cria o diretório “/abc”
        fsImpl.mkdir("/", "abc");

        // Agora, tentar criar de novo “/abc” deve falhar
        CaminhoJaExistenteException ex = assertThrows(
            CaminhoJaExistenteException.class,
            () -> fsImpl.mkdir("/", "abc")
        );
        assertEquals(
            "Já existe um diretório chamado 'abc' em: /",
            ex.getMessage(),
            "Quando já existe, a mensagem deve indicar que o diretório já existe."
        );
    }

    /**
     * 4) Permissão negada: se removermos a permissão 'w' do usuário ROOT
     *    em algum subdiretório, mkdir dentro dele deve lançar PermissaoException.
     */
    @Test
    void testMkdirPermissionDenied() throws Exception {
        // 1) Criamos um subdiretório “/noPerm”
        fsImpl.mkdir("/", "noPerm");

        // 2) Localiza o diretório criado
        Diretorio raiz = fsImpl.getFileSys().getRaiz();
        Diretorio dirNoPerm = null;
        for (Diretorio d : raiz.getSubDirs()) {
            if (d.getMetaDados().getNome().equals("noPerm")) {
                dirNoPerm = d;
                break;
            }
        }
        assertNotNull(dirNoPerm, "O diretório '/noPerm' deveria existir.");

        // 3) Remove explicitamente a permissão 'w' do usuário "root" neste diretório:
        //    (MetaDados.getPermissoes() retorna o HashMap que guarda <usuário, “rwx”>)
        HashMap<String,String> mapaPerm = dirNoPerm.getMetaDados().getPermissoes();
        // Substitui permissão “rwx” por apenas “rx” (sem 'w')
        mapaPerm.put("root", "rx");
        // Agora root só pode ler/executar, não pode escrever.

        // 4) Tenta criar subdiretório dentro de “/noPerm” → deve lançar PermissaoException
        PermissaoException pe = assertThrows(
            PermissaoException.class,
            () -> fsImpl.mkdir("/noPerm", "subdir")
        );
        assertEquals(
            "Usuário 'root' não tem permissão de escrita em: /noPerm",
            pe.getMessage(),
            "Quando não há permissão de escrita deve lançar PermissaoException com a mensagem adequada."
        );
    }
}
