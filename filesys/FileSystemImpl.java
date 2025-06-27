package filesys;

import exception.CaminhoJaExistenteException;
import exception.CaminhoNaoEncontradoException;
import exception.OperacaoInvalidaException;
import exception.PermissaoException;
import filesys.util.ArquivoUtil;
import filesys.util.DiretorioUtil;
import filesys.util.UsuarioUtil;
import filesys.util.VerificacaoUtil;

import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * A classe {@code FileSystemImpl} é a implementação principal da interface {@link IFileSystem},
 * responsável por gerenciar toda a lógica de um sistema de arquivos hierárquico com suporte a permissões,
 * usuários, arquivos e diretórios.
 *
 * <p>
 * Esta classe mantém um conjunto de usuários, um diretório raiz e provê métodos para operações como:
 * criação, remoção, leitura, escrita, cópia, movimentação e listagem de arquivos e diretórios.
 * Também permite a configuração de permissões específicas para diferentes usuários em cada caminho do sistema.
 * </p>
 *
 * <p>
 * Ao ser inicializada, garante a existência do diretório raiz ("/") e do usuário padrão ("root") com permissões totais.
 * Usuários adicionais podem ser adicionados e gerenciados dinamicamente.
 * </p>
 *
 * <p>
 * Funcionalidades principais:
 * <ul>
 *   <li>Gerenciamento de usuários e permissões.</li>
 *   <li>Criação e remoção de arquivos e diretórios.</li>
 *   <li>Leitura e escrita em arquivos.</li>
 *   <li>Movimentação e cópia de arquivos e diretórios.</li>
 *   <li>Listagem de conteúdo de diretórios, com suporte a recursividade.</li>
 * </ul>
 * </p>
 *
 * <p>
 * Exceções específicas são lançadas para tratar situações como permissões insuficientes,
 * caminhos inexistentes ou operações inválidas.
 * </p>
 *
 * <p>
 * Embora os atributos internos não possam ser modificados diretamente, a classe fornece métodos
 * para manipular o estado do sistema de arquivos de forma controlada e segura.
 * </p>
 */

public final class FileSystemImpl implements IFileSystem {
    private static final String ROOT_USER = "root";
    private Set<Usuario> users = new HashSet<>();
    private Diretorio root;

    public FileSystemImpl() {
        users.add(new Usuario(ROOT_USER, "rwx", "/"));
        root = new Diretorio("/", "rwx", ROOT_USER);
    }

    @Override
    public void addUser(Usuario user) {
        if (users.stream().anyMatch(u -> u.getNome().equals(user.getNome()))) {
            throw new IllegalArgumentException("Usuário com o mesmo nome já existe: " + user.getNome());
        }
        users.add(user);
        try {
            DiretorioUtil.navegar(user.getDir(), root, user.getNome())
                    .setPermissaoUsuario(user.getNome(), user.getPermissao());
        } catch (CaminhoNaoEncontradoException | PermissaoException e) {
            try {
                mkdir(user.getDir(), ROOT_USER);
                DiretorioUtil.navegar(user.getDir(), root, user.getNome())
                        .setPermissaoUsuario(user.getNome(), user.getPermissao());
            } catch (CaminhoJaExistenteException | PermissaoException | CaminhoNaoEncontradoException
                    | OperacaoInvalidaException ex) {
                throw new RuntimeException("Erro ao criar diretório para o usuário: " + user.getNome(), ex);
            }
        }

    }

    @Override
    public void removeUser(String username) {
        Usuario user = users.stream()
                .filter(u -> u.getNome().equals(username))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + username));
        if (user.getNome().equals(ROOT_USER))
            throw new IllegalArgumentException("Não é possível remover o usuário root.");
        users.remove(user);
    }

    @Override
    public void mkdir(String caminho, String usuario)
            throws CaminhoJaExistenteException, PermissaoException, OperacaoInvalidaException {
        if (caminho.equals("/"))
            return;
        StringTokenizer tokenizer = new StringTokenizer(caminho, "/");
        Diretorio atual = root;
        StringBuilder caminhoAtual = new StringBuilder();
        while (tokenizer.hasMoreTokens()) {
            String parte = tokenizer.nextToken();
            caminhoAtual.append("/").append(parte);
            if (!existeFilho(atual, parte)) {
                atual = DiretorioUtil.criarDiretorioFilho(atual, parte, caminhoAtual.toString(), usuario, users);
            } else {
                atual = DiretorioUtil.avancarParaDiretorioFilho(atual, parte);
            }
        }
    }

    private boolean existeFilho(Diretorio atual, String nome) {
        return atual.getFilhos().containsKey(nome);
    }

    @Override
    public void chmod(String caminho, String usuario, String usuarioAlvo, String permissao)
            throws CaminhoNaoEncontradoException, PermissaoException {
        Diretorio dir = DiretorioUtil.navegar(caminho, root, usuario);
        VerificacaoUtil.verificarPermissaoEscrita(dir, usuario, caminho);
        dir.setPermissaoUsuario(usuarioAlvo, permissao);
    }

    @Override
    public void rm(String caminho, String usuario, boolean recursivo)
            throws CaminhoNaoEncontradoException, PermissaoException {
        if (caminho.equals("/")) {
            throw new PermissaoException("Não é possível remover o diretório raiz.");
        }

        Diretorio pai = DiretorioUtil.obterDiretorioPai(caminho, root, usuario);
        String nomeAlvo = DiretorioUtil.obterNomeAlvo(caminho);
        Diretorio alvo = DiretorioUtil.obterAlvo(pai, nomeAlvo, caminho);
        VerificacaoUtil.verificarPermissaoEscrita(alvo, usuario, caminho);
        if (!alvo.isArquivo()) {
            VerificacaoUtil.verificarRemocaoDiretorio(alvo, recursivo);
            if (recursivo) {
                DiretorioUtil.removerRecursivo(alvo, usuario);
            }
        }
        pai.removerFilho(nomeAlvo);
    }


    @Override
    public void touch(String caminho, String usuario)
            throws CaminhoJaExistenteException, PermissaoException,
            CaminhoNaoEncontradoException, OperacaoInvalidaException {

        VerificacaoUtil.verificarCaminhoArquivo(caminho);
        String caminhoPai = DiretorioUtil.extrairCaminhoPai(caminho);
        String nomeArquivo = DiretorioUtil.extrairNomeArquivo(caminho);

        mkdir(caminhoPai, usuario);
        Diretorio parent = DiretorioUtil.navegar(caminhoPai, root, usuario);
        VerificacaoUtil.verificarPermissaoEscrita(parent, usuario, caminhoPai);
        VerificacaoUtil.verificarSeEhArquivo(parent);

        VerificacaoUtil.verificarSeArquivoExiste(parent, nomeArquivo);
        Usuario user = UsuarioUtil.buscarUsuario(usuario, users);
        Arquivo novoArquivo = ArquivoUtil.criarNovoArquivo(nomeArquivo, user);

        parent.adicionarFilho(novoArquivo);
    }

    @Override
    public void write(String caminho, String usuario, boolean anexar, byte[] buffer)
            throws CaminhoNaoEncontradoException, PermissaoException, OperacaoInvalidaException {
        Diretorio dir = DiretorioUtil.navegar(caminho, root,usuario);
        VerificacaoUtil.verificarEscritaArquivo(dir, usuario);
        ArquivoUtil.escreverBufferNoArquivo((Arquivo) dir, buffer, anexar);
    }

    @Override
    public void read(String caminho, String usuario, byte[] buffer, Offset offset)
            throws CaminhoNaoEncontradoException, PermissaoException, OperacaoInvalidaException {
        Diretorio dir = DiretorioUtil.navegar(caminho, root,usuario);
        VerificacaoUtil.verificarLeituraArquivo(dir, usuario);
        Arquivo arquivo = (Arquivo) dir;
        ArquivoUtil.atualizarOffsetLimite(offset, arquivo.getTamanho());
        if (offset.getValue() >= arquivo.getTamanho())
            return;
        ArquivoUtil.lerDadosDoArquivo(arquivo, buffer, offset);
    }

    @Override
    public void mv(String caminhoAntigo, String caminhoNovo, String usuario)
            throws CaminhoNaoEncontradoException, PermissaoException {
        VerificacaoUtil.verificarMovimentacaoPermitida(caminhoAntigo, caminhoNovo);

        Diretorio paiAntigo = DiretorioUtil.obterDiretorioPai(caminhoAntigo, root, usuario);
        String nomeAntigo = DiretorioUtil.extrairNomeArquivo(caminhoAntigo);
        Diretorio alvo = DiretorioUtil.obterAlvo(paiAntigo, nomeAntigo, caminhoAntigo);

        VerificacaoUtil.verificarPermissaoEscrita(alvo, usuario, caminhoAntigo);

        boolean destinoExiste = false;
        Diretorio destino;
        try {
            destino = DiretorioUtil.navegar(caminhoNovo, root, usuario);
            destinoExiste = true;
        } catch (CaminhoNaoEncontradoException e) {
            destinoExiste = false;
            destino = null;
        }

        if (destinoExiste && !destino.isArquivo()) {
            // Se destino é um diretório existente, mova para dentro dele
            VerificacaoUtil.verificarPermissaoEscrita(destino, usuario, caminhoNovo);
            if (destino.getFilhos().containsKey(nomeAntigo)) {
                throw new PermissaoException("Já existe um item com o mesmo nome no diretório de destino.");
            }
            ArquivoUtil.executarMovimentacao(paiAntigo, destino, nomeAntigo, nomeAntigo, alvo);
        } else {
            // Caso seja renomeação ou mudança para novo caminho
            Diretorio paiNovo = DiretorioUtil.obterDiretorioPai(caminhoNovo, root, usuario);
            String nomeNovo = DiretorioUtil.extrairNomeArquivo(caminhoNovo);

            VerificacaoUtil.verificarPermissaoEscrita(paiNovo, usuario, caminhoNovo);
            VerificacaoUtil.verificarDestinoDisponivel(paiNovo, nomeNovo);

            ArquivoUtil.executarMovimentacao(paiAntigo, paiNovo, nomeAntigo, nomeNovo, alvo);
        }
    }

    @Override
    public void ls(String caminho, String usuario, boolean recursivo)
            throws CaminhoNaoEncontradoException, PermissaoException {
        Diretorio dir = DiretorioUtil.navegar(caminho, root, usuario);
        VerificacaoUtil.verificarLeituraDiretorio(dir, usuario, caminho);
        String output = DiretorioUtil.listar(dir, caminho, recursivo, usuario);
        System.out.print(output);
    }

    @Override
public void cp(String caminhoOrigem, String caminhoDestino, String usuario, boolean recursivo)
        throws CaminhoNaoEncontradoException, PermissaoException {
    Diretorio origem = DiretorioUtil.navegar(caminhoOrigem, root, usuario);

    // Tenta navegar até o destino. Se não existir, cria o diretório de destino.
    Diretorio destino;
    try {
        destino = DiretorioUtil.navegar(caminhoDestino, root, usuario);
    } catch (CaminhoNaoEncontradoException e) {
        // Cria o diretório de destino se não existir
        try {
            mkdir(caminhoDestino, usuario);
            destino = DiretorioUtil.navegar(caminhoDestino, root, usuario);
        } catch (Exception ex) {
            throw new CaminhoNaoEncontradoException("Não foi possível criar o diretório de destino: " + caminhoDestino);
        }
    }

    VerificacaoUtil.verificarPermissoesParaCopia(origem, destino, usuario);

    if (origem.isArquivo()) {
        ArquivoUtil.copiarArquivo((Arquivo) origem, destino);
    } else {
        if (!recursivo) {
            throw new PermissaoException("Diretório precisa de recursividade para cópia.");
        }
        ArquivoUtil.copiarDiretorio((Diretorio) origem, destino, usuario);
    }
    }
}