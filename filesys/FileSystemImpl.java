package filesys;

import java.util.Arrays;
import java.util.HashMap;

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

    @Override
    public void chmod(String caminho, String usuario, String usuarioAlvo, String permissao)
            throws CaminhoNaoEncontradoException, PermissaoException {
        throw new UnsupportedOperationException("Método não implementado 'chmod'");
    }

    @Override
    public void rm(String caminho, String usuario, boolean recursivo)
            throws CaminhoNaoEncontradoException, PermissaoException {
        throw new UnsupportedOperationException("Método não implementado 'rm'");
    }

    @Override
    public void touch(String caminho, String usuario) throws CaminhoJaExistenteException, PermissaoException {
        throw new UnsupportedOperationException("Método não implementado 'touch'");
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
    public void ls(String caminho, String usuario, boolean recursivo)
            throws CaminhoNaoEncontradoException, PermissaoException {
        throw new UnsupportedOperationException("Método não implementado 'ls'");
    }

    public void addUser(String user) {
        throw new UnsupportedOperationException("Método não implementado 'addUser'");
    }

    @Override
    public void cp(String caminhoOrigem, String caminhoDestino, String usuario, boolean recursivo)
            throws CaminhoNaoEncontradoException, PermissaoException {
        // -- (este código permanece igual ao exemplo anterior) --

        // Localiza origem
        Object origemObj = buscarPorCaminho(caminhoOrigem);
        if (origemObj == null) {
            throw new CaminhoNaoEncontradoException("Origem não encontrada: " + caminhoOrigem);
        }

        // Localiza destino (obrigatoriamente Diretório)
        Object destinoObj = buscarPorCaminho(caminhoDestino);
        if (!(destinoObj instanceof Diretorio)) {
            throw new CaminhoNaoEncontradoException("Destino inválido (não é diretório): " + caminhoDestino);
        }
        Diretorio dirDestino = (Diretorio) destinoObj;

        // Verificação de permissões (leitura na origem e escrita no destino)
        // Esse trecho garante que, independente de origemObj ser um arquivo ou
        // diretório,
        // você sempre obtém o objeto MetaDados correspondente, que será usado para
        // checar permissões de leitura na origem.
        MetaDados mdOrigem = (origemObj instanceof Arquivo)
                ? ((Arquivo) origemObj).getMetaDados()
                : ((Diretorio) origemObj).getMetaDados();

        if (!mdOrigem.hasPermissao(usuario, "r")) {
            throw new PermissaoException("Sem permissão de leitura em: " + caminhoOrigem);
        }
        if (!dirDestino.getMetaDados().hasPermissao(usuario, "w")) {
            throw new PermissaoException("Sem permissão de escrita em: " + caminhoDestino);
        }

        // Decide se é Arquivo ou Diretorio
        if (origemObj instanceof Arquivo) {
            Arquivo arqOrig = (Arquivo) origemObj;
            cpArquivo(arqOrig, dirDestino, arqOrig.getMetaDados().getNome());
        } else if (origemObj instanceof Diretorio) {
            Diretorio dirOrig = (Diretorio) origemObj;
            if (!recursivo) {
                throw new UnsupportedOperationException(
                        "Cópia de diretório requer recursivo=true: " + caminhoOrigem);
            }
            cpDiretorio(dirOrig, dirDestino, dirOrig.getMetaDados().getNome());
        } else {
            throw new UnsupportedOperationException("Tipo de objeto desconhecido em origem.");
        }
    }

    // (NÃO esqueça de incluir também o método buscarPorCaminho(), exatamente como
    // no exemplo anterior.)

    /**
     * Copia um Arquivo (deep‑copy de metadados e blocos) para dentro de 'destino',
     * com o nome 'novoNome'.
     */
    private void cpArquivo(Arquivo origem, Diretorio destino, String novoNome) {
        MetaDados mdOrig = origem.getMetaDados();

        // 1) Cria novo MetaDados com nome=novoNome, mesmo dono e mesmo tamanho
        MetaDados mdNovo = new MetaDados(novoNome, mdOrig.getTamanho(), mdOrig.getDono());
        // 2) Copiamos permissões
        mdNovo.setPermissoes(new HashMap<>(mdOrig.getPermissoes()));

        // 3) Deep‑copy do array de Bloco[]
        Bloco[] blocosOrig = origem.getArquivo();
        Bloco[] blocosNovo = new Bloco[blocosOrig.length];
        for (int i = 0; i < blocosOrig.length; i++) {
            byte[] dadosOrigPrim = blocosOrig[i].getDados();
            byte[] copiaDados = Arrays.copyOf(dadosOrigPrim, dadosOrigPrim.length);
            blocosNovo[i] = new Bloco(dadosOrigPrim.length);
            blocosNovo[i].setDados(copiaDados);
        }

        // 4) Cria novo Arquivo usando o construtor que recebemos (MetaDados + Bloco[])
        Arquivo copia = new Arquivo(mdNovo, blocosNovo);
        // 5) Adiciona ao diretório destino
        destino.addArquivo(copia);
    }

    /**
     * Copia um Diretório inteiro (recursivamente) para dentro de 'destino', com o
     * nome 'novoNome'.
     */
    private void cpDiretorio(Diretorio origem, Diretorio destino, String novoNome) {
        MetaDados mdOrig = origem.getMetaDados();

        // 1) Cria MetaDados para o diretório-cópia
        MetaDados mdNovo = new MetaDados(novoNome, 0, mdOrig.getDono());
        mdNovo.setPermissoes(new HashMap<>(mdOrig.getPermissoes()));

        // 2) Cria o novo Diretorio com nome/dono
        Diretorio copiaDir = new Diretorio(mdNovo.getNome(), mdNovo.getDono());
        copiaDir.setMetaDados(mdNovo);

        // 3) Copiar todos os arquivos diretos (usa cpArquivo)
        for (Arquivo arq : origem.getArquivos()) {
            cpArquivo(arq, copiaDir, arq.getMetaDados().getNome());
        }

        // 4) Copiar recursivamente cada subdiretório
        for (Diretorio sub : origem.getSubDirs()) {
            cpDiretorio(sub, copiaDir, sub.getMetaDados().getNome());
        }

        // 5) Adiciona o diretório-cópia ao destino
        destino.addSubDiretorio(copiaDir);
    }

    /**
     * Busca recursivamente um Arquivo ou Diretório a partir de um caminho POSIX
     * absoluto
     * (por exemplo: "/usr/docs/projeto.txt").
     * Retorna a instância encontrada (Arquivo ou Diretorio) ou lança
     * CaminhoNaoEncontradoException se algum componente não existir.
     */
    private Object buscarPorCaminho(String caminho) throws CaminhoNaoEncontradoException {
        String path = caminho.trim();
        if (!path.startsWith("/")) {
            throw new CaminhoNaoEncontradoException("Caminho deve ser absoluto: " + caminho);
        }
        // Se for exatamente "/", retorna o diretório raiz
        if (path.equals("/")) {
            return fileSys.getRaiz();
        }

        // Divide o caminho em componentes, ignorando a primeira string vazia que surge
        // antes da primeira barra
        String[] partes = path.split("/");
        // Começamos a navegação a partir da raiz
        Diretorio atual = fileSys.getRaiz();

        // Para cada componente após a raiz
        for (int i = 1; i < partes.length; i++) {
            String nome = partes[i];
            boolean ultimo = (i == partes.length - 1);

            if (ultimo) {
                // Se for o último componente, pode ser arquivo OU diretório

                // 1) Tenta encontrar um subdiretório com nome "nome"
                for (Diretorio sub : atual.getSubDirs()) {
                    if (sub.getMetaDados().getNome().equals(nome)) {
                        return sub; // Encontrou como diretório
                    }
                }
                // 2) Se não achou o diretório, tenta encontrar um arquivo com nome "nome"
                for (Arquivo arq : atual.getArquivos()) {
                    if (arq.getMetaDados().getNome().equals(nome)) {
                        return arq; // Encontrou como arquivo
                    }
                }
                // 3) Se não encontrou nenhum, lança exceção
                throw new CaminhoNaoEncontradoException("Componente não encontrado: " + nome);
            }

            // Se não for o último componente, então obrigatoriamente esse nome deve ser um
            // subdiretório
            boolean achouDir = false;
            for (Diretorio sub : atual.getSubDirs()) {
                if (sub.getMetaDados().getNome().equals(nome)) {
                    atual = sub;
                    achouDir = true;
                    break;
                }
            }
            if (!achouDir) {
                throw new CaminhoNaoEncontradoException("Diretório não encontrado no caminho: " + nome);
            }
        }

        // Em teoria não chegamos aqui, pois ou retornamos algo ou lançamos exceção
        throw new CaminhoNaoEncontradoException("Caminho inválido: " + caminho);
    }

}
