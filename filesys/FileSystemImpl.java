package filesys;

import exception.CaminhoJaExistenteException;
import exception.CaminhoNaoEncontradoException;
import exception.PermissaoException;
import model.Arquivo;
import model.Diretorio;
import model.ElementoFS;
import model.Usuario;

import java.util.HashMap;
import java.util.Map;

public final class FileSystemImpl implements IFileSystem {
    private static final String ROOT_USER = "root"; // pode ser necessário
    private Diretorio raiz;
    private Map<String, Usuario> usuarios = new HashMap<>();

    public FileSystemImpl() {
        this.raiz = new Diretorio("/", "rwx", ROOT_USER);
        usuarios.put(ROOT_USER, new Usuario(ROOT_USER, "rwx", "/"));
        // TODO: Carregar usuários adicionais do arquivo users/users se necessário
    }

    // TODO: Validar se o método navegar cobre todos os casos de caminhos relativos e absolutos
    private ElementoFS navegar(String caminho) throws CaminhoNaoEncontradoException {
        if (caminho.equals("/")) return raiz;
        String[] partes = caminho.split("/");
        Diretorio atual = raiz;
        for (int i = 1; i < partes.length; i++) {
            ElementoFS filho = atual.getFilhos().get(partes[i]);
            if (filho == null) throw new CaminhoNaoEncontradoException("Caminho não encontrado: " + caminho);
            if (i == partes.length - 1) return filho;
            if (!filho.isArquivo()) {
                atual = (Diretorio) filho;
            } else {
                throw new CaminhoNaoEncontradoException("Caminho não encontrado (esperado diretório): " + caminho);
            }
        }
        return atual;
    }

    @Override
    public void mkdir(String caminho, String usuario) throws CaminhoJaExistenteException, PermissaoException {
        if (caminho.equals("/")) return;
        String[] partes = caminho.split("/");
        Diretorio atual = raiz;
        for (int i = 1; i < partes.length - 1; i++) {
            ElementoFS filho = atual.getFilhos().get(partes[i]);
            if (filho == null || filho.isArquivo()) throw new PermissaoException("Diretório intermediário não existe: " + partes[i]);
            atual = (Diretorio) filho;
        }
        String nomeNovo = partes[partes.length - 1];
        if (atual.getFilhos().containsKey(nomeNovo)) throw new CaminhoJaExistenteException("Já existe: " + caminho);
        if (!atual.temPermissao(usuario, 'w')) throw new PermissaoException("Sem permissão para criar em: " + caminho);
        atual.adicionarFilho(new Diretorio(nomeNovo, "rwx", usuario));
        // TODO: Permitir criar diretórios recursivamente (mkdir -p) se necessário
    }

    @Override
    public void chmod(String caminho, String usuario, String usuarioAlvo, String permissao)
            throws CaminhoNaoEncontradoException, PermissaoException {
        ElementoFS elem = navegar(caminho);
        if (!elem.getDonoDiretorio().equals(usuario) && !usuario.equals(ROOT_USER)) {
            throw new PermissaoException("Apenas o dono ou root pode alterar permissões.");
        }
        if (elem instanceof Diretorio) {
            ((Diretorio) elem).setPermissaoUsuario(usuarioAlvo, permissao);
        } else {
            elem.setPermissoesPadrao(permissao);
        }
        // TODO: Validar permissões para arquivos se for necessário permissões específicas por usuário
    }

    // TODO: Implementar método rm (remoção de arquivos/diretórios)
    @Override
    public void rm(String caminho, String usuario, boolean recursivo)
            throws CaminhoNaoEncontradoException, PermissaoException {
        // TODO: Implementar lógica de remoção (verificar se é arquivo ou diretório, permissões, recursividade)
        throw new UnsupportedOperationException("Método não implementado 'rm'");
    }

    @Override
    public void touch(String caminho, String usuario) throws CaminhoJaExistenteException, PermissaoException {
        if (caminho.equals("/") || caminho.endsWith("/")) throw new IllegalArgumentException("Nome de arquivo inválido.");
        int lastSlash = caminho.lastIndexOf('/');
        String caminhoPai = (lastSlash <= 0) ? "/" : caminho.substring(0, lastSlash);
        String nomeArquivo = caminho.substring(lastSlash + 1);
        Diretorio pai;
        try {
            pai = (Diretorio) navegar(caminhoPai);
        } catch (CaminhoNaoEncontradoException e) {
            throw new PermissaoException("Diretório pai não encontrado: " + caminhoPai);
        }
        if (pai.getFilhos().containsKey(nomeArquivo)) throw new CaminhoJaExistenteException("Arquivo já existe: " + nomeArquivo);
        if (!pai.temPermissao(usuario, 'w')) throw new PermissaoException("Sem permissão para criar arquivo em: " + caminhoPai);
        pai.adicionarFilho(new Arquivo(nomeArquivo, "rw-", usuario));
        // TODO: Permitir criar arquivos com permissões diferentes se necessário
    }

    // TODO: Implementar método write (escrita em arquivos)
    @Override
    public void write(String caminho, String usuario, boolean anexar, byte[] buffer)
            throws CaminhoNaoEncontradoException, PermissaoException {
        // TODO: Implementar lógica de escrita (dividir em blocos, verificar permissões, append ou sobrescrever)
        throw new UnsupportedOperationException("Método não implementado 'write'");
    }

    // TODO: Implementar método read (leitura de arquivos)
    @Override
    public void read(String caminho, String usuario, byte[] buffer)
            throws CaminhoNaoEncontradoException, PermissaoException {
        // TODO: Implementar lógica de leitura (ler em blocos, permissões, tratar arquivos grandes)
        throw new UnsupportedOperationException("Método não implementado 'read'");
    }

    // TODO: Implementar método mv (movimentação/renomeação)
    @Override
    public void mv(String caminhoAntigo, String caminhoNovo, String usuario)
            throws CaminhoNaoEncontradoException, PermissaoException {
        // TODO: Implementar lógica de movimentação/renomeação (verificar permissões, atualizar árvore)
        throw new UnsupportedOperationException("Método não implementado 'mv'");
    }

    // TODO: Implementar método ls (listagem de diretórios)
    @Override
    public void ls(String caminho, String usuario, boolean recursivo) throws CaminhoNaoEncontradoException, PermissaoException {
        // TODO: Implementar lógica de listagem (mostrar arquivos/diretórios, recursivo ou não, permissões)
        throw new UnsupportedOperationException("Método não implementado 'ls'");
    }

    // TODO: Implementar método cp (cópia de arquivos/diretórios)
    @Override
    public void cp(String caminhoOrigem, String caminhoDestino, String usuario, boolean recursivo)
            throws CaminhoNaoEncontradoException, PermissaoException {
        // TODO: Implementar lógica de cópia (copiar arquivos/diretórios, recursivo, permissões)
        throw new UnsupportedOperationException("Método não implementado 'cp'");
    }

    // TODO: Implementar carregamento de usuários a partir do arquivo users/users se necessário
    public void addUser(String user) {
        throw new UnsupportedOperationException("Método não implementado 'addUser'");
    }

    // TODO: Adicionar métodos auxiliares para facilitar operações recursivas, busca de pai, etc.
}
