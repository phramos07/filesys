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
