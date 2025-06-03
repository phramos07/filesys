package filesys;

import exception.CaminhoJaExistenteException;
import exception.CaminhoNaoEncontradoException;
import exception.PermissaoException;
import model.Arquivo;
import model.Diretorio;
import model.ElementoFS;
import model.Usuario;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class FileSystemImpl implements IFileSystem {
    private static final String ROOT_USER = "root"; // pode ser necessário
    private Diretorio raiz;
    private Map<String, Usuario> usuarios = new HashMap<>();
    private static Map<String, Usuario> usuariosGlobais = new HashMap<>();

    public FileSystemImpl(List<Usuario> usuarios) {
        this.raiz = new Diretorio("/", "rwx", ROOT_USER);
        this.usuarios.put(ROOT_USER, new Usuario(ROOT_USER, "rwx", "/"));
        for (Usuario usuario : usuarios) {
            this.usuarios.put(usuario.getNome(), usuario);
            usuariosGlobais.put(usuario.getNome(), usuario); // Adicione aqui
        }
        // TODO: Carregar usuários adicionais do arquivo users/users se necessário
    }

    public static Usuario getUsuarioGlobal(String nome) {
        return usuariosGlobais.get(nome);
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

    public ElementoFS navegarParaTeste(String caminho) throws CaminhoNaoEncontradoException {
        return navegar(caminho);
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
        // Exigir permissão de escrita E execução
        if (!atual.temPermissao(usuario, 'w') || !atual.temPermissao(usuario, 'x'))
            throw new PermissaoException("Sem permissão para criar em: " + caminho);
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
        if (caminho.equals("/")) {
            throw new PermissaoException("Não é permitido remover o diretório raiz.");
        }
        // Navega até o elemento a ser removido
        ElementoFS elemento = navegar(caminho);

        // Descobre o diretório pai
        int lastSlash = caminho.lastIndexOf('/');
        String caminhoPai = (lastSlash <= 0) ? "/" : caminho.substring(0, lastSlash);
        Diretorio pai = (Diretorio) navegar(caminhoPai);

        // Verifica permissão de escrita no pai
        if (!pai.temPermissao(usuario, 'w')) {
            throw new PermissaoException("Sem permissão para remover em: " + caminhoPai);
        }

        if (!elemento.isArquivo()) {
            Diretorio dir = (Diretorio) elemento;
            if (!recursivo && !dir.getFilhos().isEmpty()) {
                throw new PermissaoException("Diretório não está vazio. Use recursivo=true para remover tudo.");
            }
            // Remover recursivamente todos os filhos
            if (recursivo) {
                for (String filho : new java.util.ArrayList<>(dir.getFilhos().keySet())) {
                    rm(caminho + "/" + filho, usuario, true);
                }
            }
        }
        // Remove do pai
        pai.removerFilho(elemento.getNomeDiretorio());
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
        ElementoFS elemento = navegar(caminho);

        if (!elemento.isArquivo()) {
            throw new CaminhoNaoEncontradoException("O caminho não é um arquivo.");
        }
        Arquivo arquivo = (Arquivo) elemento;

        if (!arquivo.temPermissao(usuario, 'w')) {
            throw new PermissaoException("Sem permissão de escrita no arquivo.");
        }

        final int TAMANHO_BLOCO = 4096;

        if (!anexar) {
            arquivo.limparBlocos();
        }

        int pos = 0;
        while (pos < buffer.length) {
            int tamanhoRestante = buffer.length - pos;
            int tamanhoBloco = Math.min(TAMANHO_BLOCO, tamanhoRestante);
            byte[] bloco = new byte[tamanhoBloco];
            System.arraycopy(buffer, pos, bloco, 0, tamanhoBloco);
            arquivo.adicionarBloco(bloco);
            pos += tamanhoBloco;
        }
    }

    // TODO: Implementar método read (leitura de arquivos)
    @Override
    public void read(String caminho, String usuario, byte[] buffer)
            throws CaminhoNaoEncontradoException, PermissaoException {
        ElementoFS elemento = navegar(caminho);

        if (!elemento.isArquivo()) {
            throw new CaminhoNaoEncontradoException("O caminho não é um arquivo.");
        }
        Arquivo arquivo = (Arquivo) elemento;

        if (!arquivo.temPermissao(usuario, 'r')) {
            throw new PermissaoException("Sem permissão de leitura no arquivo.");
        }

        // Lê os blocos do arquivo e copia para o buffer até preencher ou acabar o arquivo
        int posBuffer = 0;
        for (byte[] bloco : arquivo.getBlocos()) {
            int bytesParaCopiar = Math.min(bloco.length, buffer.length - posBuffer);
            System.arraycopy(bloco, 0, buffer, posBuffer, bytesParaCopiar);
            posBuffer += bytesParaCopiar;
            if (posBuffer >= buffer.length) break; // buffer cheio
        }
    }

    // TODO: Implementar método mv (movimentação/renomeação)
    @Override
    public void mv(String caminhoAntigo, String caminhoNovo, String usuario)
            throws CaminhoNaoEncontradoException, PermissaoException {
        if (caminhoAntigo.equals("/") || caminhoNovo.equals("/")) {
            throw new PermissaoException("Não é permitido mover ou renomear o diretório raiz.");
        }

        // Navega até o elemento a ser movido
        ElementoFS elemento = navegar(caminhoAntigo);

        // Descobre o diretório pai do antigo e do novo caminho
        int lastSlashAntigo = caminhoAntigo.lastIndexOf('/');
        String caminhoPaiAntigo = (lastSlashAntigo <= 0) ? "/" : caminhoAntigo.substring(0, lastSlashAntigo);
        Diretorio paiAntigo = (Diretorio) navegar(caminhoPaiAntigo);

        int lastSlashNovo = caminhoNovo.lastIndexOf('/');
        String caminhoPaiNovo = (lastSlashNovo <= 0) ? "/" : caminhoNovo.substring(0, lastSlashNovo);
        Diretorio paiNovo = (Diretorio) navegar(caminhoPaiNovo);

        String nomeNovo = caminhoNovo.substring(lastSlashNovo + 1);

        // Permissão de escrita nos dois diretórios
        if (!paiAntigo.temPermissao(usuario, 'w')) {
            throw new PermissaoException("Sem permissão para remover do diretório antigo: " + caminhoPaiAntigo);
        }
        if (!paiNovo.temPermissao(usuario, 'w')) {
            throw new PermissaoException("Sem permissão para criar no diretório novo: " + caminhoPaiNovo);
        }

        // Remove do diretório antigo
        paiAntigo.removerFilho(elemento.getNomeDiretorio());

        // Renomeia se necessário
        elemento.setNomeDiretorio(nomeNovo);

        // Adiciona ao novo diretório
        paiNovo.adicionarFilho(elemento);
    }

    // TODO: Implementar método ls (listagem de diretórios)
    @Override
    public void ls(String caminho, String usuario, boolean recursivo) throws CaminhoNaoEncontradoException, PermissaoException {
        ElementoFS elemento = navegar(caminho);

        if (elemento.isArquivo()) {
            if (!elemento.temPermissao(usuario, 'r')) {
                throw new PermissaoException("Sem permissão de leitura no arquivo.");
            }
            System.out.println(elemento.getNomeDiretorio());
            return;
        }

        Diretorio dir = (Diretorio) elemento;
        if (!dir.temPermissao(usuario, 'r')) {
            throw new PermissaoException("Sem permissão de leitura no diretório.");
        }

        listarDiretorio(dir, caminho.equals("/") ? "" : caminho, usuario, recursivo, 0);
    }

    private void listarDiretorio(Diretorio dir, String caminho, String usuario, boolean recursivo, int nivel) {
        String prefixo = "  ".repeat(nivel);
        for (ElementoFS filho : dir.getFilhos().values()) {
            System.out.println(prefixo + filho.getNomeDiretorio() +
                    (filho.isArquivo() ? "" : "/") +
                    " [owner=" + filho.getDonoDiretorio() + ", perms=" + filho.getPermissoesPadrao() + "]");
            if (recursivo && !filho.isArquivo()) {
                listarDiretorio((Diretorio) filho, caminho + "/" + filho.getNomeDiretorio(), usuario, true, nivel + 1);
            }
        }
    }

    // TODO: Implementar método cp (cópia de arquivos/diretórios)
    @Override
    public void cp(String caminhoOrigem, String caminhoDestino, String usuario, boolean recursivo)
            throws CaminhoNaoEncontradoException, PermissaoException {
        ElementoFS origem = navegar(caminhoOrigem);

        int lastSlash = caminhoDestino.lastIndexOf('/');
        String caminhoPaiDestino = (lastSlash <= 0) ? "/" : caminhoDestino.substring(0, lastSlash);
        String nomeDestino = caminhoDestino.substring(lastSlash + 1);
        Diretorio paiDestino = (Diretorio) navegar(caminhoPaiDestino);

        if (!origem.temPermissao(usuario, 'r')) {
            throw new PermissaoException("Sem permissão de leitura na origem.");
        }
        if (!paiDestino.temPermissao(usuario, 'w')) {
            throw new PermissaoException("Sem permissão de escrita no destino.");
        }

        if (origem.isArquivo()) {
            // Copia arquivo
            Arquivo arqOrigem = (Arquivo) origem;
            Arquivo novoArquivo = new Arquivo(nomeDestino, arqOrigem.getPermissoesPadrao(), usuario);
            // Copia blocos
            for (byte[] bloco : arqOrigem.getBlocos()) {
                novoArquivo.adicionarBloco(bloco.clone());
            }
            paiDestino.adicionarFilho(novoArquivo);
        } else {
            // Copia diretório
            if (!recursivo) {
                throw new PermissaoException("Diretório só pode ser copiado com recursivo=true.");
            }
            Diretorio dirOrigem = (Diretorio) origem;
            Diretorio novoDir = new Diretorio(nomeDestino, dirOrigem.getPermissoesPadrao(), usuario);
            paiDestino.adicionarFilho(novoDir);
            // Copia recursivamente os filhos
            for (ElementoFS filho : dirOrigem.getFilhos().values()) {
                cp(caminhoOrigem + "/" + filho.getNomeDiretorio(),
                   caminhoDestino + "/" + filho.getNomeDiretorio(),
                   usuario, true);
            }
        }
    }

    // TODO: Implementar carregamento de usuários a partir do arquivo users/users se necessário
    public void addUser(String user) {
        throw new UnsupportedOperationException("Método não implementado 'addUser'");
    }

    // TODO: Adicionar métodos auxiliares para facilitar operações recursivas, busca de pai, etc.
}
