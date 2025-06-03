package filesys;

import java.util.Arrays;
import java.util.HashMap;
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
    private FileSys fileSys;

    public FileSys getFileSys() {
        return fileSys;
    }

    public FileSystemImpl() {
        this.fileSys = new FileSys();
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

    /**
     * chmod: altera a permissão de 'usuarioAlvo' no arquivo ou diretório em 'caminho'.
     *
     * @param caminho       caminho absoluto para um arquivo OU diretório (ex: "/usr/bin/arquivo.txt" ou "/usr/bin")
     * @param usuario       quem está executando o comando (deve ser root OU dono do objeto)
     * @param usuarioAlvo   usuário cujas permissões serão ajustadas
     * @param permissao     string de 3 caracteres, cada um em { 'r', 'w', 'x' } ou '-'
     *                      Ex.: "rwx", "r--", "-w-", "---"
     *
     * @throws CaminhoNaoEncontradoException se não encontrar o arquivo/diretório em 'caminho'
     * @throws PermissaoException            se 'usuario' não for root nem dono do objeto
     */
    @Override
    public void chmod(String caminho, String usuario, String usuarioAlvo, String permissao)
            throws CaminhoNaoEncontradoException, PermissaoException 
    {
        // 1) Validar string de permissão: deve ter exatamente 3 caracteres, cada um seja 'r','w','x' ou '-'
        if (permissao == null || permissao.length() != 3) {
            throw new IllegalArgumentException("Permissão inválida (deve ter 3 chars): " + permissao);
        }
        for (char c : permissao.toCharArray()) {
            if (c != 'r' && c != 'w' && c != 'x' && c != '-') {
                throw new IllegalArgumentException("Permissão contém caractere inválido: " + c);
            }
        }

        // 2) Localizar o objeto (Arquivo ou Diretório) em 'caminho'
        Object objAlvo = buscarPorCaminho(caminho);
        // buscarPorCaminho lança CaminhoNaoEncontradoException se não existir

        MetaDados mdAlvo;
        if (objAlvo instanceof Arquivo) {
            mdAlvo = ((Arquivo) objAlvo).getMetaDados();
        } else if (objAlvo instanceof Diretorio) {
            mdAlvo = ((Diretorio) objAlvo).getMetaDados();
        } else {
            // Nunca deveria acontecer, mas para garantir:
            throw new CaminhoNaoEncontradoException("Caminho encontrado não é arquivo nem diretório: " + caminho);
        }

        // 3) Verificar se 'usuario' tem permissão para executar chmod:
        //    - Se for ROOT_USER, sempre permitido.
        //    - Caso contrário, somente se for dono do objeto
        String dono = mdAlvo.getDono();
        if (!usuario.equals(ROOT_USER) && !usuario.equals(dono)) {
            throw new PermissaoException(
                "Usuário '" + usuario + "' não tem permissão para alterar direitos em: " 
                + caminho
            );
        }

        // 4) Alterar (ou inserir) a permissão de 'usuarioAlvo' para 'permissao'
        //    Se o usuárioAlvo for "root", deixamos que ele fique “rwx” obrigatoriamente,
        //    independentemente do que se passe em 'permissao'? 
        //    Depende do requisito, mas aqui vamos aceitar qualquer string para qualquer usuárioAlvo,
        //    pois, se o root quiser revogar até de si mesmo, é opção dele.
        //
        //    Simplesmente atualizamos o HashMap<String,String>:
        HashMap<String, String> mapaPerm = mdAlvo.getPermissoes();
        mapaPerm.put(usuarioAlvo, permissao);
        mdAlvo.setPermissoes(mapaPerm);
    }

    @Override
    public void rm(String caminho, String usuario, boolean recursivo)
            throws CaminhoNaoEncontradoException, PermissaoException {
        throw new UnsupportedOperationException("Método não implementado 'rm'");
    }

    /**
     * Método touch: cria um arquivo vazio em 'caminho'.
     * Exemplo: touch("/usr/local/meuarquivo.txt", "alice")
     *  -> pai = "/usr/local", nome = "meuarquivo.txt"
     */
    @Override
    public void touch(String caminho, String usuario) 
            throws CaminhoJaExistenteException, PermissaoException 
    {
        // 1) Separar o caminho em 'pai' e 'nomeDoArquivo'
        String path = caminho.trim();
        if (!path.startsWith("/")) {
            // Para simplificar, consideramos que caminhos devem ser absolutos
            throw new CaminhoJaExistenteException("Caminho inválido (deve começar com '/'): " + caminho);
        }
        if (path.equals("/")) {
            // Não faz sentido chamar touch("/") → não se pode criar arquivo com nome vazio
            throw new CaminhoJaExistenteException("Não é possível criar arquivo na raiz sem nome: " + caminho);
        }

        // Exemplo: "/usr/local/meuarquivo.txt"
        // indexSlash = posição da última barra antes do nome do arquivo
        int indexSlash = path.lastIndexOf("/");
        // Se a última barra for a própria raiz, então pai = "/"
        String paiPath = (indexSlash == 0) ? "/" : path.substring(0, indexSlash);
        String nomeArquivo = path.substring(indexSlash + 1);
        if (nomeArquivo.isEmpty()) {
            throw new CaminhoJaExistenteException("Nome de arquivo vazio em: " + caminho);
        }

        // 2) Localizar o diretório pai
        Diretorio dirPai;
        try {
            Object o = buscarPorCaminho(paiPath);
            if (!(o instanceof Diretorio)) {
                // Se não for diretório (por exemplo, for um arquivo), não podemos criar dentro
                throw new CaminhoJaExistenteException(
                    "Caminho pai não é um diretório: " + paiPath
                );
            }
            dirPai = (Diretorio) o;
        }
        catch (CaminhoNaoEncontradoException e) {
            // Se pai não existe, relançamos como CaminhoJaExistenteException
            throw new CaminhoJaExistenteException("Caminho não encontrado: " + paiPath);
        }

        // 3) Verificar permissão de escrita no dirPai para o usuário informado
        MetaDados mdPai = dirPai.getMetaDados();
        if (!mdPai.hasPermissao(usuario, "w")) {
            throw new PermissaoException(
                "Usuário '" + usuario 
                + "' não tem permissão de escrita em: " 
                + paiPath
            );
        }

        // 4) Verificar se já existe um arquivo ou diretório com esse nome em dirPai
        // 4.1) Checa subdiretórios
        for (Diretorio sub : dirPai.getSubDirs()) {
            if (sub.getMetaDados().getNome().equals(nomeArquivo)) {
                throw new CaminhoJaExistenteException(
                    "Já existe arquivo ou diretório chamado '" + nomeArquivo 
                    + "' em: " + paiPath
                );
            }
        }
        // 4.2) Checa arquivos
        for (Arquivo arq : dirPai.getArquivos()) {
            if (arq.getMetaDados().getNome().equals(nomeArquivo)) {
                throw new CaminhoJaExistenteException(
                    "Já existe arquivo ou diretório chamado '" + nomeArquivo 
                    + "' em: " + paiPath
                );
            }
        }

        // 5) Se chegamos aqui, podemos criar o novo arquivo vazio
        //    5.1) Criar MetaDados com tamanho = 0
        MetaDados metaArq = new MetaDados(nomeArquivo, 0, usuario);
        //         conceder permissão “rw” para o dono
        HashMap<String, String> mapaPerm = new HashMap<>();
        mapaPerm.put(usuario, "rw");
        metaArq.setPermissoes(mapaPerm);

        //    5.2) Criar o objeto Arquivo com 0 blocos (array vazio)
        Arquivo novoArq = new Arquivo(metaArq, new Bloco[0]);

        //    5.3) Adicionar ao diretório pai
        dirPai.addArquivo(novoArq);
    }

    @Override
    public void write(String caminho, String usuario, boolean anexar, byte[] buffer)
            throws CaminhoNaoEncontradoException, PermissaoException {
        throw new UnsupportedOperationException("Método não implementado 'write'");
    }

    @Override
    public void read(String caminho, String usuario, byte[] buffer)
            throws CaminhoNaoEncontradoException, PermissaoException {
        throw new UnsupportedOperationException("Método não implementado 'read'");
    }

    @Override
    public void mv(String caminhoAntigo, String caminhoNovo, String usuario)
            throws CaminhoNaoEncontradoException, PermissaoException {
        throw new UnsupportedOperationException("Método não implementado 'mv'");
    }

    @Override
    public void ls(String caminho, String usuario, boolean recursivo) throws CaminhoNaoEncontradoException, PermissaoException {
        // Busca o objeto (diretório ou arquivo) pelo caminho fornecido
        Object obj = buscarPorCaminho(caminho);

        // Se não for um diretório, lança exceção
        if (!(obj instanceof Diretorio)) {
            throw new CaminhoNaoEncontradoException("Não é um diretório: " + caminho);
        }
        Diretorio dir = (Diretorio) obj;
        MetaDados meta = dir.getMetaDados();

        // Verifica se o usuário tem permissão de leitura, ou se é dono, ou se é root
        if (!meta.getPermissao(usuario).contains("r") && !meta.isDono(usuario) && !usuario.equals(ROOT_USER)) {
            throw new PermissaoException("Sem permissão de leitura em: " + caminho);
        }

        // Chama o método auxiliar para listar o conteúdo do diretório
        listarConteudo(dir, caminho, recursivo, "");
    }

    private void listarConteudo(Diretorio dir, String caminho, boolean recursivo, String prefixo) {
        // Lista todos os arquivos do diretório atual
        for (Arquivo arq : dir.getArquivos()) {
            System.out.println(prefixo + arq.getMetaDados().getNome());
        }
        // Lista todos os subdiretórios do diretório atual
        for (Diretorio sub : dir.getSubDirs()) {
            System.out.println(prefixo + sub.getMetaDados().getNome() + "/"); // "/" indica diretório
            // Se for recursivo, chama novamente para o subdiretório, aumentando o prefixo (indentação)
            if (recursivo) {
                listarConteudo(sub, caminho + "/" + sub.getMetaDados().getNome(), true, prefixo + "  ");
            }
        }
    }

    public void addUser(String user) {
        throw new UnsupportedOperationException("Método não implementado 'addUser'");
    }

    @Override
     public void cp(String caminhoOrigem, String caminhoDestino, String usuario, boolean recursivo)
            throws CaminhoNaoEncontradoException, PermissaoException {

        Object origemObj = buscarPorCaminho(caminhoOrigem);
        if (origemObj == null) {
            throw new CaminhoNaoEncontradoException("Origem não encontrada: " + caminhoOrigem);
        }

        Object destinoObj = buscarPorCaminho(caminhoDestino);
        if (!(destinoObj instanceof Diretorio)) {
            throw new CaminhoNaoEncontradoException("Destino inválido (não é diretório): " + caminhoDestino);
        }
        Diretorio dirDestino = (Diretorio) destinoObj;

        MetaDados mdOrigem = (origemObj instanceof Arquivo)
                ? ((Arquivo) origemObj).getMetaDados()
                : ((Diretorio) origemObj).getMetaDados();

        if (!mdOrigem.hasPermissao(usuario, "r")) {
            throw new PermissaoException("Sem permissão de leitura em: " + caminhoOrigem);
        }
        if (!dirDestino.getMetaDados().hasPermissao(usuario, "w")) {
            throw new PermissaoException("Sem permissão de escrita em: " + caminhoDestino);
        }

        if (origemObj instanceof Arquivo) {
            cpArquivo((Arquivo) origemObj, dirDestino);
        } else {
            Diretorio dirOrig = (Diretorio) origemObj;
            if (!recursivo) {
                throw new UnsupportedOperationException(
                        "Cópia de diretório requer recursivo=true: " + caminhoOrigem);
            }
            cpDiretorio(dirOrig, dirDestino);
        }
    }

    private void cpArquivo(Arquivo origem, Diretorio destino) {
        MetaDados mdOrig = origem.getMetaDados();
        MetaDados mdNovo = clonarMetaDados(mdOrig, mdOrig.getNome(), mdOrig.getTamanho(), mdOrig.getDono());

        Bloco[] blocosOrig = origem.getArquivo();
        Bloco[] blocosNovo = new Bloco[blocosOrig.length];
        for (int i = 0; i < blocosOrig.length; i++) {
            byte[] dadosOrigem = blocosOrig[i].getDados();
            byte[] copiaDados = Arrays.copyOf(dadosOrigem, dadosOrigem.length);
            Bloco novoBloco = new Bloco(dadosOrigem.length);
            novoBloco.setDados(copiaDados);
            blocosNovo[i] = novoBloco;
        }

        Arquivo copia = new Arquivo(mdNovo, blocosNovo);
        destino.addArquivo(copia);
    }

    private void cpDiretorio(Diretorio origem, Diretorio destino) {
        MetaDados mdOrig = origem.getMetaDados();
        MetaDados mdNovo = clonarMetaDados(mdOrig, mdOrig.getNome(), 0, mdOrig.getDono());

        Diretorio copiaDir = new Diretorio(mdNovo);
        for (Arquivo arqFilho : origem.getArquivos()) {
            cpArquivo(arqFilho, copiaDir);
        }
        for (Diretorio subOrig : origem.getSubDirs()) {
            cpDiretorio(subOrig, copiaDir);
        }
        destino.addSubDiretorio(copiaDir);
    }

    private MetaDados clonarMetaDados(MetaDados original, String nome, int tamanho, String dono) {
        MetaDados copia = new MetaDados(nome, tamanho, dono);
        Map<String, String> permissoesOrig = original.getPermissoes();
        if (permissoesOrig != null) {
            copia.setPermissoes(new HashMap<>(permissoesOrig));
        }
        return copia;
    }

    private Object buscarPorCaminho(String caminho) throws CaminhoNaoEncontradoException {
        if (caminho == null || !caminho.startsWith("/")) {
            throw new CaminhoNaoEncontradoException("Caminho deve ser absoluto: " + caminho);
        }
        String path = caminho.trim();
        if (path.equals("/")) {
            return fileSys.getRaiz();
        }

        String[] partes = path.split("/");
        Diretorio atual = fileSys.getRaiz();

        for (int i = 1; i < partes.length; i++) {
            String nomeComponente = partes[i];
            boolean ultimo = (i == partes.length - 1);

            if (ultimo) {
                for (Diretorio sub : atual.getSubDirs()) {
                    if (sub.getMetaDados().getNome().equals(nomeComponente)) {
                        return sub;
                    }
                }
                for (Arquivo arq : atual.getArquivos()) {
                    if (arq.getMetaDados().getNome().equals(nomeComponente)) {
                        return arq;
                    }
                }
                throw new CaminhoNaoEncontradoException("Componente não encontrado: " + nomeComponente);
            }

            boolean achou = false;
            for (Diretorio sub : atual.getSubDirs()) {
                if (sub.getMetaDados().getNome().equals(nomeComponente)) {
                    atual = sub;
                    achou = true;
                    break;
                }
            }
            if (!achou) {
                throw new CaminhoNaoEncontradoException(
                        "Diretório não encontrado no caminho: " + nomeComponente);
            }
        }

        throw new CaminhoNaoEncontradoException("Caminho inválido: " + caminho);
    }

}
