package filesys;

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
        this.raiz = new Diretorio("/", "rwx", ROOT_USER); // adicionando um diretorio raiz, usuário ROOT tendo todas as
                                                          // permissões
        usuarios.put(ROOT_USER, new Usuario(ROOT_USER, "rwx", "/")); // criando um usuário com seu diretorio e colocando
                                                                     // dentro de usuarios
        for (Usuario usuario : u) {
            if (!usuario.getNome().equalsIgnoreCase("root"))
                usuarios.put(usuario.getNome(), usuario);
        }
    }

    private ElementoFS navegar(String caminho) throws CaminhoNaoEncontradoException {
        // Se o caminho for apenas "/", retorna o diretório raiz
        if (caminho.equals("/"))
            return raiz;

        // Divide o caminho pelos separadores "/"
        // Ex: "/home/user/docs" -> ["", "home", "user", "docs"]
        String[] partes = caminho.split("/");

        // Começa a navegação a partir do diretório raiz
        Diretorio atual = raiz;

        // Itera pelas partes do caminho (ignorando a primeira que é vazia)
        for (int i = 1; i < partes.length; i++) {
            // Tenta obter o filho (arquivo ou diretório) com o nome da parte atual
            ElementoFS filho = atual.getFilhos().get(partes[i]);

            // Se não existir esse filho, lança exceção indicando que o caminho não foi
            // encontrado
            if (filho == null)
                throw new CaminhoNaoEncontradoException("Caminho não encontrado: " + caminho);

            // Se esta é a última parte do caminho, retorna o elemento encontrado (pode ser
            // arquivo ou diretório)
            if (i == partes.length - 1)
                return filho;

            // Se ainda há partes para percorrer e o elemento atual não for um arquivo (ou
            // seja, é um diretório), continua a navegação
            if (!filho.isArquivo()) {
                atual = (Diretorio) filho;
            } else {
                // Se encontrou um arquivo no meio do caminho, lança exceção (pois não pode
                // navegar dentro de um arquivo)
                throw new CaminhoNaoEncontradoException("Caminho não encontrado (esperado diretório): " + caminho);
            }
        }

        // Retorna o último diretório acessado (caso o caminho terminasse em diretório)
        return atual;
    }

    /**
     * Cria diretórios no caminho especificado, incluindo diretórios intermediários
     * caso necessário,
     * semelhante ao comando `mkdir -p` do Linux.
     *
     * @param caminho Caminho absoluto do diretório a ser criado.
     * @param usuario Usuário que está realizando a operação.
     * @throws CaminhoJaExistenteException Se houver um arquivo no meio do caminho
     *                                     com o mesmo nome.
     * @throws PermissaoException          Se o usuário não tiver permissão de
     *                                     escrita em algum diretório do caminho.
     */
    @Override
    public void mkdir(String caminho, String usuario) throws CaminhoJaExistenteException, PermissaoException {
        // ignora casos inválidos
        if (caminho == null || caminho.isEmpty() || caminho.equals("/"))
            return;

        // divide o caminho nas partes
        // começa da raiz o sistema de arquivos
        String[] partes = caminho.split("/");
        Diretorio atual = raiz;

        for (int i = 1; i < partes.length; i++) {
            String nomeDir = partes[i];
            if (nomeDir.isEmpty())
                continue; // Ignora barras duplas //

            // busca se o filho ja possui diretorios com esse nome
            ElementoFS filho = atual.getFilhos().get(nomeDir);

            if (filho == null) {
                // Verifica permissão de escrita antes de criar
                if (!atual.temPermissao(usuario, 'w')) {
                    throw new PermissaoException("Sem permissão para criar em: " + getCaminhoCompleto(atual, nomeDir));
                }

                // criando o diretorio e adicionando
                Diretorio novoDir = new Diretorio(nomeDir, "rwx", usuario);
                atual.adicionarFilho(novoDir);
                atual = novoDir;
            } else if (filho.isArquivo()) {
                throw new CaminhoJaExistenteException(
                        "Já existe arquivo com esse nome: " + getCaminhoCompleto(atual, nomeDir));
            } else {
                atual = (Diretorio) filho;
            }
        }
    }

    /**
     * Monta o caminho completo de um diretório, adicionando opcionalmente o nome de
     * um novo elemento.
     *
     * @param atual    Diretório de referência.
     * @param nomeNovo Nome do novo diretório ou arquivo a ser incluído no final do
     *                 caminho.
     * @return String com o caminho completo.
     */
    private String getCaminhoCompleto(Diretorio atual, String nomeNovo) {
        StringBuilder sb = new StringBuilder();

        while (atual != null && atual.getNomeDiretorio() != null && !atual.getNomeDiretorio().equals("/")) {
            sb.insert(0, "/" + atual.getNomeDiretorio());
            atual = atual.getDiretorioPai();
        }
        sb.insert(0, "/");

        if (nomeNovo != null && !nomeNovo.isEmpty()) {
            if (sb.length() > 1)
                sb.append("/");
            sb.append(nomeNovo);
        }

        return sb.toString().replaceAll("//+", "/");
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
        if (caminho == null || caminho.equals("/") || caminho.isEmpty()) {
            throw new CaminhoNaoEncontradoException("Não é permitido remover a raiz ou caminho vazio.");
        }

        // Descobre diretório pai e nome do elemento a remover
        int idx = caminho.lastIndexOf("/");
        String paiPath = (idx == 0) ? "/" : caminho.substring(0, idx);
        String nome = caminho.substring(idx + 1);

        ElementoFS paiElem = navegar(paiPath);
        if (!(paiElem instanceof Diretorio)) {
            throw new CaminhoNaoEncontradoException("Diretório pai não encontrado: " + paiPath);
        }
        Diretorio dirPai = (Diretorio) paiElem;

        // Verifica permissão de escrita no diretório pai
        if (!dirPai.temPermissao(usuario, 'w')) {
            throw new PermissaoException("Sem permissão de escrita em: " + paiPath);
        }

        ElementoFS alvo = dirPai.getFilhos().get(nome);
        if (alvo == null) {
            throw new CaminhoNaoEncontradoException("Arquivo ou diretório não encontrado: " + caminho);
        }

        if (!alvo.isArquivo()) {
            Diretorio dirAlvo = (Diretorio) alvo;
            if (!recursivo && !dirAlvo.getFilhos().isEmpty()) {
                throw new PermissaoException("Diretório não está vazio. Use o modo recursivo para remover: " + caminho);
            }
            if (recursivo) {
                removerDiretorioRecursivo(dirAlvo, usuario);
            } else if (dirAlvo.getFilhos().isEmpty()) {
                dirPai.removerFilho(nome);
            }
        } else {
            dirPai.removerFilho(nome);
        }
    }

    // Método auxiliar para remoção recursiva de diretórios
    private void removerDiretorioRecursivo(Diretorio dir, String usuario) throws PermissaoException {
        for (ElementoFS filho : dir.getFilhos().values().toArray(new ElementoFS[0])) {
            if (!filho.isArquivo()) {
                removerDiretorioRecursivo((Diretorio) filho, usuario);
            }
            dir.removerFilho(filho.getNomeDiretorio());
        }
        // Após remover todos os filhos, remove o próprio diretório do pai
        Diretorio pai = dir.getDiretorioPai();
        if (pai != null) {
            pai.removerFilho(dir.getNomeDiretorio());
        }
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
        // 1) Localiza o arquivo
        Object obj = navegar(caminho);
        if (!(obj instanceof Arquivo)) {
            throw new CaminhoNaoEncontradoException("Arquivo não encontrado: " + caminho);
        }
        Arquivo arquivo = (Arquivo) obj;

        // 2) Verifica permissão de escrita
        if (!usuario.equals(ROOT_USER) && !arquivo.donoDiretorio.equals(usuario)
                && !arquivo.permissoesPadrao.contains("w")) {
            throw new PermissaoException("Sem permissão de escrita em: " + caminho);
        }

        // 3) Se não for append, sobrescreve o arquivo (limpa blocos)
        if (!anexar) {
            arquivo.limparBlocos();
        }

        // 4) Escreve o buffer em blocos de 4096 bytes (tamanho do bloco)
        // buffer -> conteudo completo a ser salvo
        int TAMANHO_BLOCO = 4096; // define o tamanho de cada bloco
                                  // evita escrever tudo de uma vez no buffer
                                  // se diminuir o tamanho do bloco, o desperdício cai, porém a memória para
                                  // manter metadados cresce
        int bufferOffset = 0; // se o bufferOffset não tem controler de pro onde continuar a leitura do buffer
        while (bufferOffset < buffer.length) {
            int bytesParaEscrever = Math.min(TAMANHO_BLOCO, buffer.length - bufferOffset);
            byte[] bloco = new byte[bytesParaEscrever];
            System.arraycopy(buffer, bufferOffset, bloco, 0, bytesParaEscrever);
            arquivo.adicionarBloco(bloco);
            bufferOffset += bytesParaEscrever;
        }
    }

    @Override
    public void read(String caminho, String usuario, byte[] buffer, Offset offset)
            throws CaminhoNaoEncontradoException, PermissaoException {
        // 1) Localiza o arquivo
        Object obj = navegar(caminho);
        if (!(obj instanceof Arquivo)) {
            throw new CaminhoNaoEncontradoException("Arquivo não encontrado: " + caminho);
        }
        Arquivo arquivo = (Arquivo) obj;

        if (!usuario.equals(ROOT_USER) && !arquivo.permissoesPadrao.contains("r")) {
            throw new PermissaoException("Sem permissão de leitura em: " + caminho);
        }

        int readOffset = (offset != null) ? offset.getValue() : 0; // onde dentro do arquivo vc deve começar a ler
        int bufferPos = 0; // indica em que posição do buffer de destino você ja escreveu dados lidos
        int filePos = 0; // marca qual posição de leitura do arquivo vc ja consumiu

        for (byte[] bloco : arquivo.getBlocos()) {
            // 1) Se todo o bloco ainda está antes do offset, pule-o por inteiro:
            if (filePos + bloco.length <= readOffset) {
                filePos += bloco.length; // avança a posição no arquivo
                continue; // vai para o próximo bloco
            }

            // 2) Determina onde, dentro do bloco atual, começar a copiar:
            // - Se readOffset > filePos, significa que já consumimos parte desse bloco
            int blocoOffset = Math.max(0, readOffset - filePos);

            // 3) Quantos bytes desse bloco cabem no buffer restante?
            int bytesParaLer = Math.min(
                    bloco.length - blocoOffset, // do bloco a partir de blocoOffset
                    buffer.length - bufferPos // até encher o buffer destino
            );

            // Se não há mais nada a ler (buffer cheio ou bloco já totalmente antes do
            // offset), encerra:
            if (bytesParaLer <= 0)
                break;

            // 4) Copia bytesParaLer do bloco para o buffer:
            // de bloco[blocoOffset … blocoOffset+bytesParaLer-1]
            // para buffer[bufferPos … bufferPos+bytesParaLer-1]
            System.arraycopy(bloco, blocoOffset, buffer, bufferPos, bytesParaLer);

            // 5) Atualiza índices:
            bufferPos += bytesParaLer; // avançou no buffer destino
            filePos += bloco.length; // bloco todo foi “lido” em termos de filePos
            readOffset += bytesParaLer; // avança a posição real de leitura

            // 6) Se o buffer ficou cheio, pare de ler:
            if (bufferPos >= buffer.length)
                break;
        }

        if (offset != null)
            offset.setValue(readOffset);
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

        // **Novo**: se for arquivo e não for recursivo, imprime o próprio nome e
        // retorna
        if (obj instanceof Arquivo && !recursivo) {
            System.out.println(((Arquivo) obj).getNomeDiretorio());
            return;
        }

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
                listarConteudo((Diretorio) filho,
                        caminho + "/" + filho.getNomeDiretorio(),
                        true,
                        prefixo + "  ");
            }
        }
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
