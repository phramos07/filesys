package filesys;

import java.util.ArrayList;
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
    private final FileSys fileSys;
    private final Map<String, Usuario> usuarios;

    public FileSystemImpl(Map<String, Usuario> usuarios) {
        this.fileSys = new FileSys(ROOT_USER);
        this.usuarios = usuarios;
    }

    @Override
    public void mkdir(String caminho, String usuario)
            throws CaminhoJaExistenteException, PermissaoException, CaminhoNaoEncontradoException {
        verificaUsuarioValido(usuario);

        if (caminho.equals("/"))
            return;

        verificarPermissaoExecucaoNoCaminho(obterCaminhoPai(caminho), usuario);

        Diretorio pai = navegarParaDiretorioPai(caminho);
        String nomeNovo = extrairNomeFinal(caminho);

        if (pai.buscarSubdiretorio(nomeNovo) != null) {
            throw new CaminhoJaExistenteException("O diretório '" + nomeNovo + "' já existe em '" + caminho + "'");
        }

        verificarPermissao(obterCaminhoPai(caminho), usuario, 'w');

        Diretorio novo = new Diretorio(nomeNovo, usuario);
        pai.adicionarSubdiretorio(novo);

        // Debug pra testes: Imprime os subdiretorios do diretorio atual
        System.out.print("Subdiretorios de '" + pai.getMetaDados().getNome() + "': ");
        for (Diretorio d : pai.getSubdiretorios()) {
            System.out.print(d.getMetaDados().getNome() + " ");
        }
        System.out.println();
    }

    @Override
    public void chmod(String caminho, String usuario, String usuarioAlvo, String permissao)
            throws CaminhoNaoEncontradoException, PermissaoException {
        verificaUsuarioValido(usuario);
        verificaUsuarioValido(usuarioAlvo);

        // Só root ou quem tem permissão rw pode alterar permissoes
        if (!usuario.equals(ROOT_USER)
                && !(temPermissao(caminho, usuario, 'r') && temPermissao(caminho, usuario, 'w'))) {
            throw new PermissaoException(
                    "Usuário '" + usuario + "' não tem permissão para alterar permissões em '" + caminho + "'");
        }

        Diretorio pai = navegarParaDiretorioPai(caminho);
        String nome = extrairNomeFinal(caminho);

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
        throw new CaminhoNaoEncontradoException("Caminho '" + caminho + "' não encontrado.");
    }

    @Override
    public void rm(String caminho, String usuario, boolean recursivo)
            throws CaminhoNaoEncontradoException, PermissaoException {
        verificaUsuarioValido(usuario);

        if (caminho.equals("/")) {
            throw new PermissaoException("Não é permitido remover o diretório raiz.");
        }

        verificarPermissaoExecucaoNoCaminho(obterCaminhoPai(caminho), usuario);
        verificarPermissao(caminho, usuario, 'r');
        verificarPermissao(obterCaminhoPai(caminho), usuario, 'w');

        Diretorio pai = navegarParaDiretorioPai(caminho);
        String nome = extrairNomeFinal(caminho);

        Arquivo arq = pai.buscarArquivo(nome);
        if (arq != null) {
            pai.removerArquivo(nome);
            return;
        }

        Diretorio dir = pai.buscarSubdiretorio(nome);
        if (dir == null) {
            throw new CaminhoNaoEncontradoException("Caminho '" + caminho + "' não encontrado.");
        }

        if (!recursivo && (!dir.getArquivos().isEmpty() || !dir.getSubdiretorios().isEmpty())) {
            throw new PermissaoException("Diretório não vazio. Use o modo recursivo.");
        }

        if (recursivo) {
            removerDiretorioRecursivamente(dir);
        }

        pai.removerSubdiretorio(nome);
    }

    @Override
    public void touch(String caminho, String usuario)
            throws CaminhoJaExistenteException, CaminhoNaoEncontradoException, PermissaoException {
        verificaUsuarioValido(usuario);

        if (caminho.equals("/") || caminho.endsWith("/")) {
            throw new CaminhoNaoEncontradoException("Caminho inválido para arquivo: " + caminho);
        }

        verificarPermissaoExecucaoNoCaminho(obterCaminhoPai(caminho), usuario);

        Diretorio pai = navegarParaDiretorioPai(caminho);
        String nomeArquivo = extrairNomeFinal(caminho);

        if (pai.buscarArquivo(nomeArquivo) != null) {
            throw new CaminhoJaExistenteException("O arquivo '" + nomeArquivo + "' já existe.");
        }

        verificarPermissao(obterCaminhoPai(caminho), usuario, 'w');

        Arquivo novoArquivo = new Arquivo(nomeArquivo, usuario);
        pai.adicionarArquivo(novoArquivo);
    }

    @Override
    public void write(String caminho, String usuario, boolean anexar, byte[] buffer)
            throws CaminhoNaoEncontradoException, PermissaoException {
        verificaUsuarioValido(usuario);
        verificarPermissao(caminho, usuario, 'w');
        verificarPermissaoExecucaoNoCaminho(obterCaminhoPai(caminho), usuario);

        Diretorio pai = navegarParaDiretorioPai(caminho);
        String nomeArquivo = extrairNomeFinal(caminho);
        Arquivo arquivo = pai.buscarArquivo(nomeArquivo);

        if (arquivo == null) {
            if (anexar) {
                throw new CaminhoNaoEncontradoException("Arquivo '" + nomeArquivo + "' não encontrado para anexar.");
            }
            arquivo = new Arquivo(nomeArquivo, usuario);
            pai.adicionarArquivo(arquivo);
        }

        if (!anexar) {
            arquivo.getBlocos().clear();
            arquivo.getMetaDados().setTamanho(0);
        }

        escreverBufferEmBlocos(arquivo, buffer);
    }

    @Override
    public void read(String caminho, String usuario, byte[] buffer, Offset offset)
            throws CaminhoNaoEncontradoException, PermissaoException {
        verificaUsuarioValido(usuario);
        verificarPermissao(caminho, usuario, 'r');
        verificarPermissaoExecucaoNoCaminho(obterCaminhoPai(caminho), usuario);

        Diretorio pai = navegarParaDiretorioPai(caminho);
        String nomeArquivo = extrairNomeFinal(caminho);
        Arquivo arquivo = pai.buscarArquivo(nomeArquivo);

        if (arquivo == null) {
            throw new CaminhoNaoEncontradoException("Arquivo '" + nomeArquivo + "' não encontrado.");
        }

        lerArquivoComOffset(arquivo, buffer, offset);
    }

    @Override
    public void mv(String caminhoAntigo, String caminhoNovo, String usuario)
            throws CaminhoNaoEncontradoException, PermissaoException, CaminhoJaExistenteException {
        verificaUsuarioValido(usuario);

        if (caminhoAntigo.equals("/") || caminhoNovo.equals("/")) {
            throw new CaminhoNaoEncontradoException("Não é possível mover o diretório raiz.");
        }

        verificarPermissaoExecucaoNoCaminho(caminhoAntigo, usuario);
        verificarPermissaoExecucaoNoCaminho(caminhoNovo, usuario);

        verificarPermissao(caminhoAntigo, usuario, 'r');
        verificarPermissao(obterCaminhoPai(caminhoNovo), usuario, 'w');
        verificarPermissao(obterCaminhoPai(caminhoAntigo), usuario, 'w');

        // Extrai nomes e diretórios pai
        Diretorio dirOrigemPai = navegarParaDiretorioPai(caminhoAntigo);
        Diretorio dirDestinoPai = navegarParaDiretorioPai(caminhoNovo);
        String nomeOrigem = extrairNomeFinal(caminhoAntigo);
        String nomeDestino = extrairNomeFinal(caminhoNovo);

        // Verifica se já existe algo no destino
        if (dirDestinoPai.buscarArquivo(nomeDestino) != null || dirDestinoPai.buscarSubdiretorio(nomeDestino) != null) {
            throw new CaminhoJaExistenteException("O destino '" + caminhoNovo + "' já existe.");
        }

        // Verifica se é arquivo
        Arquivo arquivo = dirOrigemPai.buscarArquivo(nomeOrigem);
        if (arquivo != null) {
            dirOrigemPai.removerArquivo(nomeOrigem);
            arquivo.getMetaDados().setNome(nomeDestino);
            dirDestinoPai.adicionarArquivo(arquivo);
            return;
        }

        // Verifica se é diretório
        Diretorio subdir = dirOrigemPai.buscarSubdiretorio(nomeOrigem);
        if (subdir != null) {
            dirOrigemPai.removerSubdiretorio(nomeOrigem);
            subdir.getMetaDados().setNome(nomeDestino);
            dirDestinoPai.adicionarSubdiretorio(subdir);
            return;
        }

        // Se não encontrou nada
        throw new CaminhoNaoEncontradoException("Caminho de origem '" + caminhoAntigo + "' não encontrado.");
    }

    @Override
    public void ls(String caminho, String usuario, boolean recursivo)
            throws CaminhoNaoEncontradoException, PermissaoException {
        verificaUsuarioValido(usuario);

        // Verifica se o caminho é válido
        if (caminho == null || caminho.isEmpty() || !caminho.startsWith("/")) {
            throw new CaminhoNaoEncontradoException("Caminho inválido: " + caminho);
        }

        verificarPermissaoExecucaoNoCaminho(caminho, usuario);

        Diretorio dir = caminho.equals("/") ? fileSys.getRaiz() : navegarParaDiretorioCompleto(caminho);

        verificarPermissao(caminho, usuario, 'r');
        lsDiretorio(dir, caminho.equals("/") ? "/" : dir.getMetaDados().getNome(), recursivo, "");
    }

    private void lsDiretorio(Diretorio dir, String nome, boolean recursivo, String prefixo) {
        System.out.println(prefixo + nome + ":");

        for (Arquivo arq : dir.getArquivos()) {
            System.out.println(prefixo + "  " + arq.getMetaDados().getNome());
        }

        for (Diretorio sub : dir.getSubdiretorios()) {
            System.out.println(prefixo + "  " + sub.getMetaDados().getNome() + "/");
        }
        // Se recursivo, entra nos subdiretorios
        if (recursivo) {
            for (Diretorio sub : dir.getSubdiretorios()) {
                lsDiretorio(sub, sub.getMetaDados().getNome(), true, prefixo + "  ");
            }
        }
    }

    @Override
    public void cp(String caminhoOrigem, String caminhoDestino, String usuario, boolean recursivo)
            throws CaminhoNaoEncontradoException, PermissaoException {
        throw new UnsupportedOperationException("Método não implementado 'cp'");
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

    private void verificaUsuarioValido(String usuario) throws PermissaoException {
        if (!usuarios.containsKey(usuario)) {
            throw new PermissaoException("Usuário '" + usuario + "' não encontrado.");
        }
    }

    private void verificarPermissaoExecucaoNoCaminho(String caminho, String usuario)
            throws PermissaoException, CaminhoNaoEncontradoException {
        if (usuario.equals(ROOT_USER))
            return;

        String[] partes = caminho.split("/");
        Diretorio atual = fileSys.getRaiz();
        StringBuilder pathBuilder = new StringBuilder();
        for (int i = 1; i < partes.length; i++) {
            if (partes[i].isEmpty())
                continue;
            pathBuilder.append("/").append(partes[i]);
            if (!temPermissao(pathBuilder.toString(), usuario, 'x')) {
                throw new PermissaoException(
                        "Usuário '" + usuario + "' não tem permissão 'x' em '" + pathBuilder + "'");
            }
            Diretorio encontrado = atual.buscarSubdiretorio(partes[i]);
            if (encontrado == null)
                break;
            atual = encontrado;
        }
        // Sempre checa permissão 'x' em '/' se o caminho for exatamente "/"
        if (caminho.equals("/") && !temPermissao("/", usuario, 'x')) {
            throw new PermissaoException(
                    "Usuário '" + usuario + "' não tem permissão 'x' em '/'");
        }
    }

    private void verificarPermissao(String caminho, String usuario, char tipo) throws PermissaoException {
        if (!temPermissao(caminho, usuario, tipo)) {
            throw new PermissaoException(
                    "Usuário '" + usuario + "' não tem permissão '" + tipo + "' em '" + caminho + "'");
        }
    }

    private boolean temPermissao(String caminho, String usuario, char tipo) {
        if (usuario.equals(ROOT_USER))
            return true; // root sempre tem permissão

        try {
            Diretorio pai = navegarParaDiretorioPai(caminho);
            String nome = extrairNomeFinal(caminho);

            Arquivo arq = pai.buscarArquivo(nome);
            if (arq != null) {
                if (arq.getMetaDados().temPermissao(usuario, tipo)) {
                    return true;
                }
            }
            Diretorio dir = pai.buscarSubdiretorio(nome);
            if (dir != null) {
                if (dir.getMetaDados().temPermissao(usuario, tipo)) {
                    return true;
                }
            }
            // Se for o diretório raiz
            if (caminho.equals("/")) {
                if (fileSys.getRaiz().getMetaDados().temPermissao(usuario, tipo)) {
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
        // Remove subdiretórios recursivamente
        for (Diretorio sub : new ArrayList<>(dir.getSubdiretorios())) {
            removerDiretorioRecursivamente(sub);
            dir.removerSubdiretorio(sub.getMetaDados().getNome());
        }

        // Remove todos os arquivos
        for (Arquivo arq : new ArrayList<>(dir.getArquivos())) {
            dir.removerArquivo(arq.getMetaDados().getNome());
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

}
