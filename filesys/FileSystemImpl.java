package filesys;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import exception.CaminhoJaExistenteException;
import exception.CaminhoNaoEncontradoException;
import exception.PermissaoException;

// Implemente nesta classe o seu código do FileSystem.
// A classe pode ser alterada.
// O construtor, argumentos do construtor podem ser modificados 
// e atributos & métodos privados podem ser adicionados
public final class FileSystemImpl implements IFileSystem {
    private static final String ROOT_USER = "root"; // pode ser necessário
    private Diretorio raiz;
    private Map<String, Usuario> usuarios = new HashMap<>();

    public FileSystemImpl(List<Usuario> u) {
        this.raiz = new Diretorio("/", "rwx", ROOT_USER);
        usuarios.put(ROOT_USER, new Usuario(ROOT_USER, "rwx", "/"));
        for (Usuario usuario : u) {
            if (!usuario.getNome().equalsIgnoreCase("root"))
                usuarios.put(usuario.getNome(), usuario);
        }
    }

    // TODO: Validar se o método navegar cobre todos os casos de caminhos relativos
    // e absolutos
    private ElementoFS navegar(String caminho) throws CaminhoNaoEncontradoException {
        if (caminho.equals("/"))
            return raiz;
        String[] partes = caminho.split("/");
        Diretorio atual = raiz;
        for (int i = 1; i < partes.length; i++) {
            ElementoFS filho = atual.getFilhos().get(partes[i]);
            if (filho == null)
                throw new CaminhoNaoEncontradoException("Caminho não encontrado: " + caminho);
            if (i == partes.length - 1)
                return filho;
            if (!filho.isArquivo()) {
                atual = (Diretorio) filho;
            } else {
                throw new CaminhoNaoEncontradoException("Caminho não encontrado (esperado diretório): " + caminho);
            }
        }
        return atual;
    }

    @Override
    public void mkdir(String caminho, String nome)
            throws CaminhoJaExistenteException, PermissaoException {
        // 1) Tenta localizar o diretório “pai” (o local onde vamos criar o subdir)
        Diretorio dirPai;
        try {
            Object o = buscarPorCaminho(caminho);
            if (!(o instanceof Diretorio)) {
                // Se o objeto encontrado não for um Diretório, não faz sentido criar dentro
                throw new CaminhoJaExistenteException(
                        "Caminho especificado não é um diretório: " + caminho);
            }
            dirPai = (Diretorio) o;
        } catch (CaminhoNaoEncontradoException e) {
            // Como a assinatura de mkdir não permite lançar CaminhoNaoEncontradoException,
            // reaproveitamos CaminhoJaExistenteException para indicar que o caminho não
            // existe.
            throw new CaminhoJaExistenteException(
                    "Caminho não encontrado: " + caminho);
        }

        // 2) Verifica permissão de escrita (“w”) no dirPai para o usuário ROOT_USER
        MetaDados mdPai = dirPai.getMetaDados();
        // hasPermissao(u, “w”) deve retornar true somente se o mapa de permissões
        // contiver “w” para aquele usuário. Se não tiver, lançamos PermissaoException.
        if (!mdPai.hasPermissao(ROOT_USER, "w")) {
            throw new PermissaoException(
                    "Usuário '" + ROOT_USER
                            + "' não tem permissão de escrita em: "
                            + caminho);
        }

        // 3) Verifica se já existe um subdiretório com o mesmo nome em dirPai
        for (Diretorio sub : dirPai.getSubDirs()) {
            if (sub.getMetaDados().getNome().equals(nome)) {
                throw new CaminhoJaExistenteException(
                        "Já existe um diretório chamado '" + nome
                                + "' em: " + caminho);
            }
        }

        // 4) Cria o MetaDados do novo diretório
        MetaDados metaNovo = new MetaDados(nome, 0, ROOT_USER);
        // Concede “rwx” apenas para o dono (“root”) e nenhuma permissão para os demais
        HashMap<String, String> permissoes = new HashMap<>();
        permissoes.put(ROOT_USER, "rwx");
        metaNovo.setPermissoes(permissoes);

        // 5) Cria o novo Diretorio e o adiciona na lista de filhos do dirPai
        Diretorio novoDir = new Diretorio(nome, ROOT_USER);
        novoDir.setMetaDados(metaNovo);
        dirPai.addSubDiretorio(novoDir);
    }

    @Override
    public void chmod(String caminho, String usuario, String usuarioAlvo, String permissao)
            throws CaminhoNaoEncontradoException, PermissaoException {
        // 1) Validar string de permissão: deve ter exatamente 3 caracteres, cada um
        // 'r','w','x' ou '-'
        if (permissao == null || permissao.length() != 3) {
            throw new IllegalArgumentException("Permissão inválida (deve ter 3 chars): " + permissao);
        }
        for (char c : permissao.toCharArray()) {
            if (c != 'r' && c != 'w' && c != 'x' && c != '-') {
                throw new IllegalArgumentException("Permissão contém caractere inválido: " + c);
            }
        }

        // 2) Localizar o objeto (Arquivo ou Diretório) em 'caminho'
        Object objAlvo = navegar(caminho);

        // 3) Verificar se 'usuario' tem permissão para executar chmod:
        // - Se for ROOT_USER, sempre permitido.
        // - Caso contrário, somente se for dono do objeto
        String dono;
        if (objAlvo instanceof Arquivo) {
            dono = ((Arquivo) objAlvo).getDonoDiretorio();
        } else if (objAlvo instanceof Diretorio) {
            dono = ((Diretorio) objAlvo).getDonoDiretorio();
        } else {
            throw new CaminhoNaoEncontradoException("Caminho encontrado não é arquivo nem diretório: " + caminho);
        }
        if (!usuario.equals("root") && !usuario.equals(dono)) {
            throw new PermissaoException(
                    "Usuário '" + usuario + "' não tem permissão para alterar direitos em: " + caminho);
        }

        // 4) Alterar (ou inserir) a permissão de 'usuarioAlvo' para 'permissao'
        if (objAlvo instanceof Arquivo) {
            ((Arquivo) objAlvo).setPermissoesPadrao(permissao);
        } else if (objAlvo instanceof Diretorio) {
            ((Diretorio) objAlvo).setPermissaoUsuario(usuarioAlvo, permissao);
        }
    }

    @Override
    public void rm(String caminho, String usuario, boolean recursivo)
            throws CaminhoNaoEncontradoException, PermissaoException {
        throw new UnsupportedOperationException("Método não implementado 'rm'");
    }

    /**
     * Método touch: cria um arquivo vazio em 'caminho'.
     * Exemplo: touch("/usr/local/meuarquivo.txt", "alice")
     * -> pai = "/usr/local", nome = "meuarquivo.txt"
     */
    @Override
    public void touch(String caminho, String usuario)
            throws CaminhoJaExistenteException, PermissaoException {
        // 1) Separar o caminho em 'pai' e 'nomeDoArquivo'
        String path = caminho.trim();
        if (!path.startsWith("/")) {
            throw new CaminhoJaExistenteException("Caminho inválido (deve começar com '/'): " + caminho);
        }
        if (path.equals("/")) {
            throw new CaminhoJaExistenteException("Não é possível criar arquivo na raiz sem nome: " + caminho);
        }

        int indexSlash = path.lastIndexOf("/");
        String paiPath = (indexSlash == 0) ? "/" : path.substring(0, indexSlash);
        String nomeArquivo = path.substring(indexSlash + 1);
        if (nomeArquivo.isEmpty()) {
            throw new CaminhoJaExistenteException("Nome de arquivo vazio em: " + caminho);
        }

        // 2) Localizar o diretório pai
        Diretorio dirPai;
        try {
            Object o = navegar(paiPath);
            if (!(o instanceof Diretorio)) {
                throw new CaminhoJaExistenteException("Caminho pai não é um diretório: " + paiPath);
            }
            dirPai = (Diretorio) o;
        } catch (CaminhoNaoEncontradoException e) {
            throw new CaminhoJaExistenteException("Caminho não encontrado: " + paiPath);
        }

        // 3) Verificar permissão de escrita no dirPai para o usuário informado
        if (!dirPai.temPermissao(usuario, 'w')) {
            throw new PermissaoException(
                    "Usuário '" + usuario + "' não tem permissão de escrita em: " + paiPath);
        }

        // 4) Verificar se já existe um arquivo ou diretório com esse nome em dirPai
        if (dirPai.getFilhos().containsKey(nomeArquivo)) {
            throw new CaminhoJaExistenteException(
                    "Já existe arquivo ou diretório chamado '" + nomeArquivo + "' em: " + paiPath);
        }

        // 5) Cria o arquivo vazio (sem blocos)
        // Permissões padrão: "rw-" para o dono
        Arquivo novoArq = new Arquivo(nomeArquivo, "rw-", usuario);
        // Nenhum bloco é adicionado (arquivo vazio)

        // 6) Adiciona ao diretório pai
        dirPai.adicionarFilho(novoArq);
    }

    @Override
    public void write(String caminho, String usuario, boolean anexar, byte[] buffer)
            throws CaminhoNaoEncontradoException, PermissaoException {
        throw new UnsupportedOperationException("Método não implementado 'write'");
    }

    @Override
    public void read(String caminho, String usuario, byte[] buffer)
            throws CaminhoNaoEncontradoException, PermissaoException {
        // 1) Localiza o arquivo
        Object obj = navegar(caminho);
        if (!(obj instanceof Arquivo)) {
            throw new CaminhoNaoEncontradoException("Arquivo não encontrado: " + caminho);
        }
        Arquivo arquivo = (Arquivo) obj;

        // 2) Verifica permissão de leitura
        // Aqui, supondo que permissoesPadrao seja do tipo "rw-" ou "r--"
        if (!usuario.equals(ROOT_USER) && !arquivo.donoDiretorio.equals(usuario)
                && !arquivo.permissoesPadrao.contains("r")) {
            throw new PermissaoException("Sem permissão de leitura em: " + caminho);
        }

        // 3) Lê sequencialmente os blocos do arquivo para o buffer
        int bufferPos = 0;
        for (byte[] bloco : arquivo.getBlocos()) {
            int bytesParaLer = Math.min(bloco.length, buffer.length - bufferPos);
            if (bytesParaLer <= 0)
                break;
            System.arraycopy(bloco, 0, buffer, bufferPos, bytesParaLer);
            bufferPos += bytesParaLer;
            if (bufferPos >= buffer.length)
                break;
        }
        // buffer agora contém o conteúdo lido (até buffer.length ou fim do arquivo)
    }

    @Override
    public void mv(String caminhoAntigo, String caminhoNovo, String usuario)
            throws CaminhoNaoEncontradoException, PermissaoException {
        // 1) Localiza o elemento a ser movido
        ElementoFS elemento = navegar(caminhoAntigo);

        // 2) Verifica permissão de escrita no diretório pai do antigo e do novo caminho
        String antigoPaiPath = caminhoAntigo.substring(0, caminhoAntigo.lastIndexOf("/"));
        if (antigoPaiPath.isEmpty())
            antigoPaiPath = "/";
        Diretorio dirPaiAntigo = (Diretorio) navegar(antigoPaiPath);
        if (!dirPaiAntigo.temPermissao(usuario, 'w')) {
            throw new PermissaoException("Sem permissão de escrita no diretório de origem: " + antigoPaiPath);
        }

        int idxNovo = caminhoNovo.lastIndexOf("/");
        String novoPaiPath = (idxNovo == 0) ? "/" : caminhoNovo.substring(0, idxNovo);
        String novoNome = caminhoNovo.substring(idxNovo + 1);
        Diretorio dirPaiNovo = (Diretorio) navegar(novoPaiPath);
        if (!dirPaiNovo.temPermissao(usuario, 'w')) {
            throw new PermissaoException("Sem permissão de escrita no diretório de destino: " + novoPaiPath);
        }

        // 3) Não sobrescreve arquivos/diretórios existentes
        if (dirPaiNovo.getFilhos().containsKey(novoNome)) {
            throw new CaminhoNaoEncontradoException(
                    "Já existe arquivo ou diretório com esse nome no destino: " + caminhoNovo);
        }

        // 4) Remove do diretório antigo
        dirPaiAntigo.removerFilho(elemento.getNomeDiretorio());

        // 5) Atualiza nome se for renomeação
        elemento.setNomeDiretorio(novoNome);

        // 6) Adiciona ao novo diretório
        dirPaiNovo.adicionarFilho(elemento);

        // 7) Se for diretório, atualiza referência de pai
        if (!elemento.isArquivo() && elemento instanceof Diretorio) {
            ((Diretorio) elemento).setDiretorioPai(dirPaiNovo);
        }
    }

    @Override
    public void ls(String caminho, String usuario, boolean recursivo)
            throws CaminhoNaoEncontradoException, PermissaoException {
        // 1) Busca o objeto pelo caminho fornecido
        Object obj = navegar(caminho);

        // 2) Se não for um diretório, lança exceção
        if (!(obj instanceof Diretorio)) {
            throw new CaminhoNaoEncontradoException("Não é um diretório: " + caminho);
        }
        Diretorio dir = (Diretorio) obj;

        // 3) Verifica permissão de leitura
        if (!dir.temPermissao(usuario, 'r')) {
            throw new PermissaoException("Sem permissão de leitura em: " + caminho);
        }

        // 4) Lista o conteúdo do diretório
        listarConteudo(dir, caminho, recursivo, "");
    }

    // Método auxiliar para listar conteúdo
    private void listarConteudo(Diretorio dir, String caminho, boolean recursivo, String prefixo) {
        for (ElementoFS filho : dir.getFilhos().values()) {
            System.out.println(prefixo + filho.getNomeDiretorio());
            if (recursivo && !filho.isArquivo()) {
                listarConteudo((Diretorio) filho, caminho + "/" + filho.getNomeDiretorio(), true, prefixo + "  ");
            }
        }
    }

    public void addUser(String user) {
        throw new UnsupportedOperationException("Método não implementado 'addUser'");
    }

    @Override
    public void cp(String caminhoOrigem, String caminhoDestino, String usuario, boolean recursivo)
            throws CaminhoNaoEncontradoException, PermissaoException {
        ElementoFS origem = navegar(caminhoOrigem);

        // Verifica permissão de leitura na origem
        if (origem.isArquivo()) {
            if (!usuario.equals(ROOT_USER) && !origem.donoDiretorio.equals(usuario)
                    && !origem.permissoesPadrao.contains("r")) {
                throw new PermissaoException("Sem permissão de leitura em: " + caminhoOrigem);
            }
            try {
                copiarArquivo((Arquivo) origem, caminhoDestino, usuario);
            } catch (CaminhoJaExistenteException e) {
                throw new PermissaoException("Já existe arquivo ou diretório em: " + caminhoDestino);
            }
        } else {
            Diretorio dirOrigem = (Diretorio) origem;
            if (!usuario.equals(ROOT_USER) && !dirOrigem.donoDiretorio.equals(usuario)
                    && !dirOrigem.permissoesPadrao.contains("r")) {
                throw new PermissaoException("Sem permissão de leitura em: " + caminhoOrigem);
            }
            if (!recursivo) {
                throw new PermissaoException("Cópia de diretório requer opção recursiva.");
            }
            try {
                copiarDiretorioRecursivo(dirOrigem, caminhoDestino, usuario);
            } catch (CaminhoJaExistenteException e) {
                throw new PermissaoException("Já existe arquivo ou diretório em: " + caminhoDestino);
            }
        }
    }

    private void copiarArquivo(Arquivo origem, String caminhoDestino, String usuario)
            throws CaminhoNaoEncontradoException, PermissaoException, CaminhoJaExistenteException {
        // Verifica se já existe algo no destino
        try {
            navegar(caminhoDestino);
            throw new CaminhoJaExistenteException("Já existe arquivo ou diretório em: " + caminhoDestino);
        } catch (CaminhoNaoEncontradoException e) {
            // ok, pode criar
        }

        // Descobre diretório pai e nome do novo arquivo
        int idx = caminhoDestino.lastIndexOf("/");
        String paiPath = (idx == 0) ? "/" : caminhoDestino.substring(0, idx);
        String nomeArquivo = caminhoDestino.substring(idx + 1);

        ElementoFS pai = navegar(paiPath);
        if (!(pai instanceof Diretorio)) {
            throw new CaminhoNaoEncontradoException("Diretório pai não encontrado: " + paiPath);
        }
        Diretorio dirPai = (Diretorio) pai;

        // Cria novo arquivo com mesmo conteúdo e permissões do usuário
        Arquivo novoArq = new Arquivo(nomeArquivo, origem.permissoesPadrao, usuario);
        for (byte[] bloco : origem.getBlocos()) {
            novoArq.adicionarBloco(bloco.clone());
        }
        dirPai.adicionarFilho(novoArq);
    }

    private void copiarDiretorioRecursivo(Diretorio origem, String caminhoDestino, String usuario)
            throws CaminhoNaoEncontradoException, PermissaoException, CaminhoJaExistenteException {
        // Descobre diretório pai e nome do novo diretório
        int idx = caminhoDestino.lastIndexOf("/");
        String paiPath = (idx == 0) ? "/" : caminhoDestino.substring(0, idx);
        String nomeDir = caminhoDestino.substring(idx + 1);

        ElementoFS pai = navegar(paiPath);
        if (!(pai instanceof Diretorio)) {
            throw new CaminhoNaoEncontradoException("Diretório pai não encontrado: " + paiPath);
        }
        Diretorio dirPai = (Diretorio) pai;

        // Cria novo diretório
        Diretorio novoDir = new Diretorio(nomeDir, origem.permissoesPadrao, usuario);
        dirPai.adicionarFilho(novoDir);

        // Copia arquivos
        for (ElementoFS filho : origem.getFilhos().values()) {
            if (filho.isArquivo()) {
                copiarArquivo((Arquivo) filho, caminhoDestino + "/" + filho.nomeDiretorio, usuario);
            } else {
                copiarDiretorioRecursivo((Diretorio) filho, caminhoDestino + "/" + filho.nomeDiretorio, usuario);
            }
        }
    }

}
