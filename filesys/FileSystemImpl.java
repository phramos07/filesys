package filesys;

import java.util.ArrayList;
import java.util.Map;

import exception.CaminhoJaExistenteException;
import exception.CaminhoNaoEncontradoException;
import exception.PermissaoException;
import filesys.core.Arquivo;
import filesys.core.Bloco;
import filesys.core.Diretorio;
import filesys.core.Offset;

// Implemente nesta classe o seu código do FileSystem.
// A classe pode ser alterada.
// O construtor, argumentos do construtor podem ser modificados 
// e atributos & métodos privados podem ser adicionados
public final class FileSystemImpl implements IFileSystem {
    private static final String ROOT_USER = "root"; // pode ser necessário
    private final FileSys fileSys;
    private final Map<String, Usuario> usuarios;

    public FileSystemImpl(Map<String, Usuario> usuarios) {
        this.fileSys = new FileSys(ROOT_USER);
        this.usuarios = usuarios;
    }

    @Override // Necessário permissão wx
    public void mkdir(String caminho, String usuario)
            throws CaminhoJaExistenteException, PermissaoException, CaminhoNaoEncontradoException {
        checarUsuarioV(usuario);
        verificarFormatoCaminho(caminho);

        if (caminho.equals("/"))
            return;

        String[] partes = caminho.split("/");
        Diretorio atual = fileSys.getRaiz();
        StringBuilder caminhoAtual = new StringBuilder();

        for (int i = 1; i < partes.length; i++) {
            if (partes[i].isEmpty())
                continue;

            caminhoAtual.append("/").append(partes[i]);
            Diretorio proximo = atual.buscarSubdiretorio(partes[i]);
            boolean ultimo = (i == partes.length - 1);

            if (proximo == null) {
                String paiPath = obterCaminhoPai(caminhoAtual.toString());
                checarPermissaoExecucaoRecursiva(paiPath, usuario);
                validarPermissao(paiPath, usuario, 'w');

                atual = criarSubdiretorio(atual, partes[i], usuario);
            } else {
                if (ultimo)
                    throw new CaminhoJaExistenteException("O diretório '" + partes[i] + "' já existe.");
                atual = proximo;
            }
        }
    }

    @Override // Necessario permissão rw
    public void chmod(String caminho, String usuario, String usuarioAlvo, String permissao)
            throws CaminhoNaoEncontradoException, PermissaoException {
        checarUsuarioV(usuario);
        checarUsuarioV(usuarioAlvo);

        boolean isRoot = usuario.equals(ROOT_USER);
        if (!isRoot && !(checarPermissao(caminho, usuario, 'r') && checarPermissao(caminho, usuario, 'w'))) {
            throw new PermissaoException("Sem permissão para alterar permissões em " + caminho);
        }

        if (caminho.equals("/")) {
            fileSys.getRaiz().getMetaDados().setPermissao(usuarioAlvo, permissao);
            return;
        }

        Diretorio pai = navegarParaDiretorioPai(caminho);
        String nome = caminho.substring(caminho.lastIndexOf('/') + 1);

        Arquivo arq = pai.buscarArquivo(nome);
        if (arq != null) {
            arq.getMetaDados().setPermissao(usuarioAlvo, permissao);
            return;
        }

        Diretorio dir = pai.buscarSubdiretorio(nome);
        if (dir != null) {
            dir.getMetaDados().setPermissao(usuarioAlvo, permissao);
            return;
        }

        throw new CaminhoNaoEncontradoException("Caminho " + caminho + " não encontrado.");
    }

    @Override // Necessario permissão rw
    public void rm(String caminho, String usuario, boolean recursivo)
            throws CaminhoNaoEncontradoException, PermissaoException {
        checarUsuarioV(usuario);
        if (caminho.equals("/"))
            throw new PermissaoException("Não é permitido remover o diretório raiz.");

        String paiPath = obterCaminhoPai(caminho);
        validarPermissoesParaRemocao(caminho, paiPath, usuario);

        Diretorio pai = navegarParaDiretorioPai(caminho);
        String nome = extrairNomeFinal(caminho);

        Arquivo arq = pai.buscarArquivo(nome);
        if (arq != null) {
            pai.removerArquivo(nome);
            return;
        }

        Diretorio dir = pai.buscarSubdiretorio(nome);
        if (dir == null)
            throw new CaminhoNaoEncontradoException("Caminho '" + caminho + "' não encontrado.");

        if (!recursivo && (!dir.getArquivos().isEmpty() || !dir.getSubdiretorios().isEmpty()))
            throw new PermissaoException("Diretório não vazio. Use o modo recursivo.");

        if (recursivo)
            removerDiretorioRecursivamente(dir);

        pai.removerSubdiretorio(nome);
    }

    @Override // Necessario permissão wx
    public void touch(String caminho, String usuario)
            throws CaminhoJaExistenteException, CaminhoNaoEncontradoException, PermissaoException {
        checarUsuarioV(usuario);

        if (caminho.equals("/") || caminho.endsWith("/"))
            throw new CaminhoNaoEncontradoException("Caminho inválido para arquivo: " + caminho);

        String paiPath = obterCaminhoPai(caminho);
        checarPermissaoExecucaoRecursiva(paiPath, usuario);

        Diretorio pai = navegarParaDiretorioPai(caminho);
        String nomeArquivo = extrairNomeFinal(caminho);

        if (pai.buscarArquivo(nomeArquivo) != null)
            throw new CaminhoJaExistenteException("O arquivo '" + nomeArquivo + "' já existe.");

        validarPermissao(paiPath, usuario, 'w');

        Arquivo novoArquivo = new Arquivo(nomeArquivo, usuario);
        pai.adicionarArquivo(novoArquivo);
    }

    @Override // Necessario permissão rw
    public void write(String caminho, String usuario, boolean anexar, byte[] buffer)
            throws CaminhoNaoEncontradoException, PermissaoException {
        checarUsuarioV(usuario);
        validarPermissao(caminho, usuario, 'w');
        checarPermissaoExecucaoRecursiva(obterCaminhoPai(caminho), usuario);

        Arquivo arquivo = obterArquivo(caminho);

        if (!anexar) {
            limparArquivo(arquivo);
        }

        escreverBufferEmBlocos(arquivo, buffer);
    }

    @Override // Necessario permissão r
    public void read(String caminho, String usuario, byte[] buffer, Offset offset)
            throws CaminhoNaoEncontradoException, PermissaoException {
        checarUsuarioV(usuario);
        validarPermissao(caminho, usuario, 'r');
        checarPermissaoExecucaoRecursiva(obterCaminhoPai(caminho), usuario);

        Arquivo arquivo = obterArquivo(caminho);

        lerArquivoComOffset(arquivo, buffer, offset);
    }

    @Override // Necessario permissão rw
    public void mv(String caminhoAntigo, String caminhoNovo, String usuario)
            throws CaminhoNaoEncontradoException, PermissaoException, CaminhoJaExistenteException {
        checarUsuarioV(usuario);

        if (caminhoAntigo.equals("/") || caminhoNovo.equals("/")) {
            throw new CaminhoNaoEncontradoException("Não é possível mover o diretório raiz.");
        }

        checarPermissaoExecucaoRecursiva(caminhoAntigo, usuario);
        checarPermissaoExecucaoRecursiva(caminhoNovo, usuario);

        validarPermissao(caminhoAntigo, usuario, 'r');
        validarPermissao(obterCaminhoPai(caminhoNovo), usuario, 'w');
        validarPermissao(obterCaminhoPai(caminhoAntigo), usuario, 'w');

        Diretorio diretorioOrigemPai = navegarParaDiretorioPai(caminhoAntigo);
        Diretorio diretorioDestinoPai = navegarParaDiretorioPai(caminhoNovo);

        String nomeOrigem = extrairNomeFinal(caminhoAntigo);
        String nomeDestino = extrairNomeFinal(caminhoNovo);

        checarExistenciaNoDestino(diretorioDestinoPai, nomeDestino, caminhoNovo);

        if (moverArquivoSeExistir(diretorioOrigemPai, diretorioDestinoPai, nomeOrigem, nomeDestino))
            return;
        if (moverDiretorioSeExistir(diretorioOrigemPai, diretorioDestinoPai, nomeOrigem, nomeDestino))
            return;

        throw new CaminhoNaoEncontradoException("Caminho de origem '" + caminhoAntigo + "' não encontrado.");
    }

    @Override // Necessario permissão r
    public void ls(String caminho, String usuario, boolean recursivo)
            throws CaminhoNaoEncontradoException, PermissaoException {
        checarUsuarioV(usuario);
        verificarCaminhoComPermissao(caminho);

        checarPermissaoExecucaoRecursiva(caminho, usuario);
        validarPermissao(caminho, usuario, 'r');

        Diretorio dir = caminho.equals("/") ? fileSys.getRaiz() : navegarParaDiretorioCompleto(caminho);
        imprimirConteudoDiretorio(dir, caminho, recursivo, "");
    }

    private void verificarCaminhoComPermissao(String caminho) throws CaminhoNaoEncontradoException {
        if (caminho == null || caminho.isEmpty() || !caminho.startsWith("/")) {
            throw new CaminhoNaoEncontradoException("Caminho inválido: " + caminho);
        }
    }

    private void imprimirConteudoDiretorio(Diretorio dir, String caminho, boolean recursivo, String indent) {
        System.out.println(indent + dir.getMetaDados().getNome() + "/");
        for (Arquivo arquivo : dir.getArquivos()) {
            System.out.println(indent + "  " + arquivo.getMetaDados().getNome());
        }
        if (recursivo) {
            for (Diretorio subdir : dir.getSubdiretorios()) {
                imprimirConteudoDiretorio(subdir, caminho + "/" + subdir.getMetaDados().getNome(), true, indent + "  ");
            }
        }
    }

    @Override // Necessario permissão rw
    public void cp(String caminhoOrigem, String caminhoDestino, String usuario, boolean recursivo)
            throws CaminhoNaoEncontradoException, PermissaoException {

        checarUsuarioV(usuario);
        checarPermissaoExecucaoRecursiva(obterCaminhoPai(caminhoOrigem), usuario);
        checarPermissaoExecucaoRecursiva(obterCaminhoPai(caminhoDestino), usuario);

        Diretorio origemPai = navegarParaDiretorioPai(caminhoOrigem);
        Diretorio destinoPai = navegarParaDiretorioPai(caminhoDestino);

        String nomeOrigem = extrairNomeFinal(caminhoOrigem);
        String nomeDestino = extrairNomeFinal(caminhoDestino);

        Arquivo arquivoOrigem = origemPai.buscarArquivo(nomeOrigem);
        if (arquivoOrigem != null) {
            copiarArquivo(arquivoOrigem, destinoPai, nomeDestino, usuario);
            return;
        }

        Diretorio diretorioOrigem = origemPai.buscarSubdiretorio(nomeOrigem);
        if (diretorioOrigem != null) {
            copiarDiretorio(diretorioOrigem, destinoPai, nomeDestino, usuario, recursivo);
            return;
        }

        throw new CaminhoNaoEncontradoException("Origem não encontrada.");
    }

    public void addUser(String nome, String permissao) {
        if (!usuarios.containsKey(nome)) {
            Usuario novo = new Usuario(nome, permissao);
            usuarios.put(nome, novo);
        }
    }

    public void removeUser(String nome) {
        usuarios.remove(nome);
    }

    // Navega para o diretorio pai do caminho passado como parametro
    // Ex: "/home/user/docs" retorna "/home/user"
    private Diretorio navegarParaDiretorioPai(String caminho) throws CaminhoNaoEncontradoException {
        if (caminho.equals("/")) {
            return fileSys.getRaiz();
        }
        String[] partes = caminho.split("/");
        if (!partes[0].isEmpty()) {
            throw new IllegalArgumentException("Caminho inválido: deve começar com '/'");
        }

        Diretorio atual = fileSys.getRaiz();
        for (int i = 1; i < partes.length - 1; i++) {
            if (partes[i].isEmpty())
                continue;
            Diretorio encontrado = atual.buscarSubdiretorio(partes[i]);
            if (encontrado == null) {
                throw new CaminhoNaoEncontradoException("Diretório '" + partes[i] + "' não encontrado.");
            }
            atual = encontrado;
        }
        return atual;
    }

    // Navega para o diretorio completo do caminho passado como parametro
    // Ex: "/home/user/docs" retorna o diretorio "docs"
    private Diretorio navegarParaDiretorioCompleto(String caminho) throws CaminhoNaoEncontradoException {
        String[] partes = caminho.split("/");
        if (!partes[0].isEmpty()) {
            throw new IllegalArgumentException("Caminho inválido: deve começar com '/'");
        }

        Diretorio atual = fileSys.getRaiz();
        for (int i = 1; i < partes.length; i++) {
            if (partes[i].isEmpty())
                continue;
            Diretorio encontrado = atual.buscarSubdiretorio(partes[i]);
            if (encontrado == null) {
                throw new CaminhoNaoEncontradoException("Diretório '" + partes[i] + "' não encontrado.");
            }
            atual = encontrado;
        }
        return atual;
    }

    // Extrai o nome final do caminho, que pode ser um arquivo ou diretório
    // Ex: "/home/user/docs" retorna "docs"
    private String extrairNomeFinal(String caminho) {
        if (caminho == null || caminho.isEmpty()) {
            throw new IllegalArgumentException("Caminho inválido: não pode ser nulo ou vazio.");
        }
        if (caminho.equals("/")) {
            return "/";
        }
        String[] partes = caminho.split("/");
        return partes[partes.length - 1];
    }

    // Obtém o caminho pai do caminho fornecido
    // Ex: "/home/user/docs" retorna "/home/user"
    private String obterCaminhoPai(String caminho) {
        int ultimoSlash = caminho.lastIndexOf('/');
        if (ultimoSlash == 0)
            return "/";
        return caminho.substring(0, ultimoSlash);
    }

    private void checarUsuarioV(String usuario) throws PermissaoException {
        if (!usuarios.containsKey(usuario)) {
            throw new PermissaoException("Usuário '" + usuario + "' não encontrado.");
        }
    }

    private void checarPermissaoExecucaoRecursiva(String caminho, String usuario)
            throws PermissaoException, CaminhoNaoEncontradoException {
        if (usuario.equals(ROOT_USER))
            return;

        // Trata casos especiais de caminho vazio ou "/"
        if (caminho == null || caminho.isEmpty() || caminho.equals("/")) {
            if (!checarPermissao("/", usuario, 'x')) {
                throw new PermissaoException(
                        "Usuário '" + usuario + "' não tem permissão 'x' em '/'");
            }
            return;
        }

        String[] partes = caminho.split("/");
        StringBuilder pathBuilder = new StringBuilder();
        for (int i = 1; i < partes.length; i++) {
            if (partes[i].isEmpty())
                continue;
            pathBuilder.append("/").append(partes[i]);
            if (!checarPermissao(pathBuilder.toString(), usuario, 'x')) {
                throw new PermissaoException(
                        "Usuário '" + usuario + "' não tem permissão 'x' em '" + pathBuilder + "'");
            }
        }
    }

    private void validarPermissao(String caminho, String usuario, char tipo) throws PermissaoException {
        if (!checarPermissao(caminho, usuario, tipo)) {
            throw new PermissaoException(
                    "Usuário '" + usuario + "' não tem permissão '" + tipo + "' em '" + caminho + "'");
        }
    }

    private boolean checarPermissao(String caminho, String usuario, char tipo) {
        if (usuario.equals(ROOT_USER))
            return true; // root sempre tem permissão

        try {
            Diretorio pai = navegarParaDiretorioPai(caminho);
            String nome = extrairNomeFinal(caminho);

            Arquivo arq = pai.buscarArquivo(nome);
            if (arq != null) {
                if (arq.getMetaDados().checarPermissao(usuario, tipo)) {
                    return true;
                }
            }
            Diretorio dir = pai.buscarSubdiretorio(nome);
            if (dir != null) {
                if (dir.getMetaDados().checarPermissao(usuario, tipo)) {
                    return true;
                }
            }
            // Se for o diretório raiz
            if (caminho.equals("/")) {
                if (fileSys.getRaiz().getMetaDados().checarPermissao(usuario, tipo)) {
                    return true;
                }
            }
        } catch (Exception e) {
            // ignora, vai checar permissao global do usuario
        }

        // Checa permissao global do usuario (definida no arquivo users)
        Usuario userObj = usuarios.get(usuario);
        if (userObj != null) {
            String perm = userObj.getPermissaoParaCaminho(caminho);
            return perm.indexOf(tipo) != -1;
        }
        return false;
    }

    private void removerDiretorioRecursivamente(Diretorio dir) {
        // Remove arquivos do diretório
        for (Arquivo arquivo : new ArrayList<>(dir.getArquivos())) {
            dir.removerArquivo(arquivo.getMetaDados().getNome());
        }

        // Remove subdiretórios recursivamente
        for (Diretorio subdir : new ArrayList<>(dir.getSubdiretorios())) {
            removerDiretorioRecursivamente(subdir);
            dir.removerSubdiretorio(subdir.getMetaDados().getNome());
        }
    }

    /*
     * Escreve o conteúdo do buffer em blocos do arquivo
     * Se o buffer for maior que o tamanho do bloco,
     * ele será dividido em blocos de tamanho fixo.
     */
    private void escreverBufferEmBlocos(Arquivo arquivo, byte[] buffer) {
        int blocoTamanho = Arquivo.TAMANHO_BLOCO;
        int offset = 0;
        while (offset < buffer.length) {
            int len = Math.min(blocoTamanho, buffer.length - offset);
            byte[] dados = new byte[len];
            System.arraycopy(buffer, offset, dados, 0, len);
            Bloco bloco = new Bloco(len);
            bloco.setDados(dados);
            arquivo.adicionarBloco(bloco);
            offset += len;
        }
    }

    /*
     * Lê o conteúdo do arquivo a partir de um offset mutável.
     * O método deve atualizar o offset conforme lê bytes.
     * Se o offset for maior que o tamanho do arquivo, retorna 0.
     */
    private int lerArquivoComOffset(Arquivo arquivo, byte[] buffer, Offset offset) {
        int arquivoTamanho = arquivo.getMetaDados().getTamanho();
        int posicao = offset.getValue();

        // Zera o buffer antes de preencher
        for (int i = 0; i < buffer.length; i++)
            buffer[i] = 0;

        if (posicao >= arquivoTamanho)
            return 0;

        int bytesParaLer = Math.min(buffer.length, arquivoTamanho - posicao);

        int blocoTamanho = Arquivo.TAMANHO_BLOCO;
        int blocoInicial = posicao / blocoTamanho;
        int posNoBloco = posicao % blocoTamanho;

        int bufferPos = 0;
        int bytesRestantes = bytesParaLer;

        for (int i = blocoInicial; i < arquivo.getBlocos().size() && bytesRestantes > 0; i++) {
            Bloco bloco = arquivo.getBlocos().get(i);
            byte[] dados = bloco.getDados();
            int start = (i == blocoInicial) ? posNoBloco : 0;
            int len = Math.min(dados.length - start, bytesRestantes);
            System.arraycopy(dados, start, buffer, bufferPos, len);
            bufferPos += len;
            bytesRestantes -= len;
        }

        offset.add(bytesParaLer); // atualiza o offset para a proxima leitura
        return bytesParaLer;
    }

    private Diretorio copiarDiretorioRecursivo(Diretorio original, String usuario) throws PermissaoException {
        if (!original.getMetaDados().checarPermissao(usuario, 'r')) {
            throw new PermissaoException("Sem permissão para copiar diretório " + original.getMetaDados().getNome());
        }

        Diretorio copia = new Diretorio(original.getMetaDados().getNome(), usuario);

        for (Arquivo a : original.getArquivos()) {
            if (!a.getMetaDados().checarPermissao(usuario, 'r')) {
                throw new PermissaoException("Sem permissão para copiar arquivo " + a.getMetaDados().getNome());
            }

            Arquivo novo = new Arquivo(a.getMetaDados().getNome(), usuario);
            for (Bloco bloco : a.getBlocos()) {
                Bloco b = new Bloco(bloco.getDados().length);
                b.setDados(bloco.getDados().clone());
                novo.adicionarBloco(b);
            }
            copia.adicionarArquivo(novo);
        }

        for (Diretorio sub : original.getSubdiretorios()) {
            Diretorio subCopia = copiarDiretorioRecursivo(sub, usuario);
            copia.adicionarSubdiretorio(subCopia);
        }

        return copia;
    }

    private void verificarFormatoCaminho(String caminho) {
        if (!caminho.startsWith("/")) {
            throw new IllegalArgumentException("Caminho inválido: deve começar com '/'");
        }

    }

    private Diretorio criarSubdiretorio(Diretorio pai, String nome, String usuario) {
        Diretorio novo = new Diretorio(nome, usuario);
        pai.adicionarSubdiretorio(novo);
        return novo;
    }

    private void validarPermissoesParaRemocao(String caminho, String paiPath, String usuario)
            throws PermissaoException, CaminhoNaoEncontradoException {
        checarPermissaoExecucaoRecursiva(paiPath, usuario);
        validarPermissao(caminho, usuario, 'r');
        validarPermissao(paiPath, usuario, 'w');
    }

    private Arquivo obterArquivo(String caminho) throws CaminhoNaoEncontradoException {
        Diretorio pai = navegarParaDiretorioPai(caminho);
        String nomeArquivo = extrairNomeFinal(caminho);
        Arquivo arquivo = pai.buscarArquivo(nomeArquivo);

        if (arquivo == null) {
            throw new CaminhoNaoEncontradoException("Arquivo '" + nomeArquivo + "' não encontrado.");
        }
        return arquivo;
    }

    private void limparArquivo(Arquivo arquivo) {
        arquivo.getBlocos().clear();
        arquivo.getMetaDados().setTamanho(0);
    }

    private void checarExistenciaNoDestino(Diretorio destinoPai, String nomeDestino, String caminhoDestino)
            throws CaminhoJaExistenteException {
        if (destinoPai.buscarArquivo(nomeDestino) != null || destinoPai.buscarSubdiretorio(nomeDestino) != null) {
            throw new CaminhoJaExistenteException("O destino '" + caminhoDestino + "' já existe.");
        }
    }

    private boolean moverArquivoSeExistir(Diretorio origemPai, Diretorio destinoPai, String nomeOrigem,
            String nomeDestino) {
        Arquivo arquivo = origemPai.buscarArquivo(nomeOrigem);
        if (arquivo != null) {
            origemPai.removerArquivo(nomeOrigem);
            arquivo.getMetaDados().setNome(nomeDestino);
            destinoPai.adicionarArquivo(arquivo);
            return true;
        }
        return false;
    }

    private boolean moverDiretorioSeExistir(Diretorio origemPai, Diretorio destinoPai, String nomeOrigem,
            String nomeDestino) {
        Diretorio subdir = origemPai.buscarSubdiretorio(nomeOrigem);
        if (subdir != null) {
            origemPai.removerSubdiretorio(nomeOrigem);
            subdir.getMetaDados().setNome(nomeDestino);
            destinoPai.adicionarSubdiretorio(subdir);
            return true;
        }
        return false;
    }

    private void copiarArquivo(Arquivo origem, Diretorio destinoPai, String nomeDestino, String usuario)
            throws PermissaoException {
        if (!origem.getMetaDados().checarPermissao(usuario, 'r')) {
            throw new PermissaoException("Sem permissão de leitura para o arquivo.");
        }
        Arquivo copia = new Arquivo(nomeDestino, usuario);
        for (Bloco bloco : origem.getBlocos()) {
            Bloco novoBloco = new Bloco(bloco.getDados().length);
            novoBloco.setDados(bloco.getDados().clone());
            copia.adicionarBloco(novoBloco);
        }
        destinoPai.adicionarArquivo(copia);
    }

    private void copiarDiretorio(Diretorio origem, Diretorio destinoPai, String nomeDestino, String usuario,
            boolean recursivo) throws PermissaoException {
        if (!origem.getMetaDados().checarPermissao(usuario, 'r')) {
            throw new PermissaoException("Sem permissão de leitura para o diretório.");
        }
        if (!recursivo) {
            throw new PermissaoException("Cópia de diretório requer flag recursiva.");
        }
        Diretorio copia = copiarDiretorioRecursivo(origem, usuario);
        copia.getMetaDados().setNome(nomeDestino);
        destinoPai.adicionarSubdiretorio(copia);
    }
}
