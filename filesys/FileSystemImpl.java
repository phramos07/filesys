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

/**
 * Classe que implementa um sistema de arquivos em memória.
 * Controla operações como mkdir, touch, chmod, rm, ls e read,
 * além de gerenciar permissões por usuário e metadados de arquivos e
 * diretórios.
 */
public final class FileSystemImpl implements IFileSystem {

    private static final String ROOT_USER = "root";

    private Diretorio raiz;
    private Map<String, Usuario> usuarios = new HashMap<>();

    /**
     * Construtor padrão que cria um sistema apenas com o usuário root.
     */
    public FileSystemImpl() {
        usuarios.put(ROOT_USER, new Usuario(ROOT_USER, "/", "rwx"));
        this.raiz = new Diretorio("/", ROOT_USER);
    }

    /**
     * Construtor que inicializa o sistema com uma lista de usuários.
     * 
     * @param usuarios Lista de usuários do sistema.
     */
    public FileSystemImpl(List<Usuario> usuarios) {
        if (usuarios == null || usuarios.isEmpty()) {
            throw new IllegalArgumentException("Lista de usuários não pode ser nula ou vazia.");
        }
        for (Usuario usuario : usuarios) {
            this.usuarios.put(usuario.getNome(), usuario);
        }
        this.raiz = new Diretorio("/", ROOT_USER);
    }

    /**
     * Cria um diretório no caminho especificado.
     */
    @Override
    public void mkdir(String caminho, String usuario)
            throws CaminhoJaExistenteException, PermissaoException, CaminhoNaoEncontradoException {

        String[] partes = parseCaminho(caminho);

        if (partes.length == 0) {
            throw new CaminhoJaExistenteException("Não é possível criar o diretório raiz '/'");
        }

        Diretorio atual = navegarAteDiretorioPai(partes);

        String nomeNovo = partes[partes.length - 1];

        if (atual.subdirs.containsKey(nomeNovo) || atual.arquivos.containsKey(nomeNovo)) {
            throw new CaminhoJaExistenteException("Caminho já existe: " + caminho);
        }

        if (!permiteCriarEm(usuario, atual)) {
            throw new PermissaoException("Usuário " + usuario + " não tem permissão para criar nesse diretório.");
        }

        Diretorio novo = new Diretorio(nomeNovo, usuario);
        atual.subdirs.put(nomeNovo, novo);
    }

    /**
     * Altera permissões de um arquivo ou diretório.
     */
    @Override
    public void chmod(String caminho, String usuario, String usuarioAlvo, String permissao)
            throws CaminhoNaoEncontradoException, PermissaoException {

        String[] partes = parseCaminho(caminho);

        if (partes.length == 0) {
            throw new CaminhoNaoEncontradoException("Caminho inválido.");
        }

        Diretorio atual = navegarAteDiretorioPai(partes);

        String nomeAlvo = partes[partes.length - 1];

        MetaDados meta = encontrarMetaDados(atual, nomeAlvo);

        if (!usuario.equals(ROOT_USER) && !usuario.equals(meta.getDono())) {
            throw new PermissaoException("Apenas root ou o dono pode alterar permissões.");
        }

        meta.setPermissao(usuarioAlvo, permissao);
    }

    /**
     * Remove um arquivo ou diretório. Diretórios podem ser removidos
     * recursivamente.
     */
    @Override
    public void rm(String caminho, String usuario, boolean recursivo)
            throws CaminhoNaoEncontradoException, PermissaoException {

        String[] partes = parseCaminho(caminho);

        if (partes.length == 0) {
            throw new PermissaoException("Não é possível remover a raiz.");
        }

        Diretorio atual = navegarAteDiretorioPai(partes);
        String nomeAlvo = partes[partes.length - 1];

        if (!temPermissao(usuario, atual.metaDados, 'w')) {
            throw new PermissaoException("Usuário '" + usuario + "' não tem permissão de escrita.");
        }

        if (atual.arquivos.containsKey(nomeAlvo)) {
            atual.arquivos.remove(nomeAlvo);
            return;
        }

        if (atual.subdirs.containsKey(nomeAlvo)) {
            Diretorio dirAlvo = atual.subdirs.get(nomeAlvo);
            if (!recursivo && (!dirAlvo.arquivos.isEmpty() || !dirAlvo.subdirs.isEmpty())) {
                throw new PermissaoException("Diretório não está vazio. Use o modo recursivo.");
            }
            if (recursivo) {
                removerRecursivo(dirAlvo);
            }
            atual.subdirs.remove(nomeAlvo);
            return;
        }

        throw new CaminhoNaoEncontradoException("Arquivo ou diretório '" + nomeAlvo + "' não encontrado.");
    }

    /**
     * Cria um arquivo no sistema.
     */
    @Override
    public void touch(String caminho, String usuario)
            throws CaminhoJaExistenteException, PermissaoException, CaminhoNaoEncontradoException {

        String[] partes = parseCaminho(caminho);

        if (partes.length == 0) {
            throw new CaminhoJaExistenteException("Não é possível criar o arquivo na raiz sem nome");
        }

        Diretorio atual = navegarAteDiretorioPai(partes);

        String nomeArquivo = partes[partes.length - 1];

        if (atual.arquivos.containsKey(nomeArquivo) || atual.subdirs.containsKey(nomeArquivo)) {
            throw new CaminhoJaExistenteException("Já existe um item com esse nome: " + caminho);
        }

        if (!permiteCriarEm(usuario, atual)) {
            throw new PermissaoException("Usuário " + usuario + " não tem permissão para criar nesse diretório.");
        }

        Arquivo novoArquivo = new Arquivo(nomeArquivo, usuario);
        atual.arquivos.put(nomeArquivo, novoArquivo);
    }

    /**
     * Lê o conteúdo de um arquivo dentro do tamanho do buffer.
     */
    @Override
    public void read(String caminho, String usuario, byte[] buffer)
            throws CaminhoNaoEncontradoException, PermissaoException {

        String[] partes = parseCaminho(caminho);
        Diretorio atual = navegarAteDiretorioPai(partes);
        String nomeArquivo = partes[partes.length - 1];

        if (!atual.arquivos.containsKey(nomeArquivo)) {
            throw new CaminhoNaoEncontradoException("Arquivo '" + nomeArquivo + "' não encontrado.");
        }

        Arquivo arquivo = atual.arquivos.get(nomeArquivo);

        if (!temPermissao(usuario, arquivo.getMetaDados(), 'r')) {
            throw new PermissaoException("Sem permissão de leitura.");
        }

        List<Byte> conteudo = arquivo.getConteudo();
        int i = 0;

        while (i < buffer.length && i < conteudo.size()) {
            buffer[i] = conteudo.get(i);
            i++;
        }

        System.out.println("Leitura (" + i + " bytes):");
        System.out.println(new String(buffer, 0, i));
    }

    /**
     * Lista os arquivos e diretórios em um caminho, com opção recursiva.
     */
    @Override
    public void ls(String caminho, String usuario, boolean recursivo)
            throws CaminhoNaoEncontradoException, PermissaoException {

        String[] partes = parseCaminho(caminho);
        Diretorio atual = navegarAteDiretorio(partes);

        if (!temPermissao(usuario, atual.metaDados, 'r')) {
            throw new PermissaoException("Sem permissão de leitura.");
        }

        System.out.println("Listando conteúdo de: " + caminho);
        listar(atual, caminho, recursivo, "");
    }

    private void listar(Diretorio dir, String caminho, boolean recursivo, String indent) {
        for (String nomeArq : dir.arquivos.keySet()) {
            System.out.println(indent + "- " + nomeArq + " (arquivo)");
        }
        for (String nomeDir : dir.subdirs.keySet()) {
            System.out.println(indent + "+ " + nomeDir + " (diretório)");
            if (recursivo) {
                listar(dir.subdirs.get(nomeDir), caminho + "/" + nomeDir, true, indent + "  ");
            }
        }
    }

    @Override
    public void write(String caminho, String usuario, boolean anexar, Offset offset, byte[] buffer)
            throws CaminhoNaoEncontradoException, PermissaoException {

        String[] partes = Arrays.stream(caminho.split("/"))
                .filter(p -> !p.isEmpty())
                .toArray(String[]::new);

        if (partes.length == 0) {
            throw new CaminhoNaoEncontradoException("Caminho inválido.");
        }

        Diretorio atual = raiz;

        for (int i = 0; i < partes.length - 1; i++) {
            String parte = partes[i];
            if (!atual.subdirs.containsKey(parte)) {
                throw new CaminhoNaoEncontradoException("Diretório pai '" + parte + "' não encontrado.");
            }
            atual = atual.subdirs.get(parte);
        }

        String nomeArquivo = partes[partes.length - 1];

        if (!atual.arquivos.containsKey(nomeArquivo)) {
            throw new CaminhoNaoEncontradoException("Arquivo '" + nomeArquivo + "' não encontrado.");
        }

        Arquivo arquivo = atual.arquivos.get(nomeArquivo);
        if (!arquivo.getMetaDados().podeEscrever(usuario) && !usuario.equals(ROOT_USER)) {
            throw new PermissaoException("Usuário '" + usuario + "' não tem permissão de escrita.");
        }

        List<Byte> conteudo = arquivo.getConteudo();
        int posEscrita = anexar ? conteudo.size() : offset.getValue();
        if (!anexar && offset.getValue() == 0) {
            arquivo.limparConteudo();
            conteudo = arquivo.getConteudo();
        }

        for (int i = 0; i < buffer.length; i++) {
            if (posEscrita < conteudo.size()) {
                conteudo.set(posEscrita, buffer[i]);
            } else {
                conteudo.add(buffer[i]);
            }
            posEscrita++;
        }
    }

    private String[] extrairDiretorioENome(String caminho) {
        String[] partes = Arrays.stream(caminho.split("/")).filter(p -> !p.isEmpty()).toArray(String[]::new);
        if (partes.length == 0)
            return new String[] { "/", "" };
        String nome = partes[partes.length - 1];
        String dirPai = "/" + String.join("/", Arrays.copyOf(partes, partes.length - 1));
        return new String[] { dirPai.isEmpty() ? "/" : dirPai, nome };
    }

    private Diretorio navegarPara(String caminho) throws CaminhoNaoEncontradoException {
        String[] partes = Arrays.stream(caminho.split("/")).filter(p -> !p.isEmpty()).toArray(String[]::new);
        Diretorio atual = raiz;
        for (String parte : partes) {
            if (!atual.subdirs.containsKey(parte)) {
                throw new CaminhoNaoEncontradoException("Diretório '" + parte + "' não encontrado.");
            }
            atual = atual.subdirs.get(parte);
        }
        return atual;
    }

    private Diretorio copiarDiretorio(Diretorio original, String novoNome, String dono) {
        Diretorio copia = new Diretorio(novoNome, dono);
        copia.metaDados.setPermissao(dono, "rwx");

        for (String nomeArq : original.arquivos.keySet()) {
            Arquivo arq = original.arquivos.get(nomeArq);
            Arquivo novo = new Arquivo(nomeArq, dono);
            novo.getConteudo().addAll(arq.getConteudo());
            copia.arquivos.put(nomeArq, novo);
        }

        for (String nomeDir : original.subdirs.keySet()) {
            Diretorio subdir = original.subdirs.get(nomeDir);
            Diretorio novoSub = copiarDiretorio(subdir, nomeDir, dono);
            copia.subdirs.put(nomeDir, novoSub);
        }

        return copia;
    }

    @Override
    public void mv(String caminhoAntigo, String caminhoNovo, String usuario)
            throws CaminhoNaoEncontradoException, PermissaoException {

        String[] origem = extrairDiretorioENome(caminhoAntigo);
        String[] destino = extrairDiretorioENome(caminhoNovo);

        Diretorio dirOrigem = navegarPara(origem[0]);
        Diretorio dirDestino = navegarPara(destino[0]);

        String nomeOrigem = origem[1];
        String nomeDestino = destino[1];

        if (!dirOrigem.metaDados.podeEscrever(usuario) || !dirDestino.metaDados.podeEscrever(usuario)) {
            throw new PermissaoException("Sem permissão para mover.");
        }

        if (dirOrigem.arquivos.containsKey(nomeOrigem)) {
            Arquivo arq = dirOrigem.arquivos.remove(nomeOrigem);
            arq.getMetaDados().setNome(nomeDestino);

            dirDestino.arquivos.put(nomeDestino, arq);
            return;
        }

        if (dirOrigem.subdirs.containsKey(nomeOrigem)) {
            Diretorio dir = dirOrigem.subdirs.remove(nomeOrigem);
            dir.metaDados.setNome(nomeDestino);
            dirDestino.subdirs.put(nomeDestino, dir);
            return;
        }

        throw new CaminhoNaoEncontradoException("Origem não encontrada.");

    }

    @Override
    public void cp(String caminhoOrigem, String caminhoDestino, String usuario, boolean recursivo)
            throws CaminhoNaoEncontradoException, PermissaoException {

        String[] origem = extrairDiretorioENome(caminhoOrigem);
        String[] destino = extrairDiretorioENome(caminhoDestino);

        Diretorio dirOrigem = navegarPara(origem[0]);
        Diretorio dirDestino = navegarPara(destino[0]);

        String nomeOrigem = origem[1];
        String nomeDestino = destino[1];

        if (!dirDestino.metaDados.podeEscrever(usuario)) {
            throw new PermissaoException("Sem permissão no destino.");
        }

        if (dirOrigem.arquivos.containsKey(nomeOrigem)) {
            Arquivo arq = dirOrigem.arquivos.get(nomeOrigem);
            Arquivo copia = new Arquivo(nomeDestino, usuario);
            copia.getConteudo().addAll(arq.getConteudo());
            dirDestino.arquivos.put(nomeDestino, copia);
            return;
        }

        if (dirOrigem.subdirs.containsKey(nomeOrigem)) {
            if (!recursivo) {
                throw new PermissaoException("Cópia de diretório requer modo recursivo.");
            }

            Diretorio origemDir = dirOrigem.subdirs.get(nomeOrigem);
            Diretorio copiaDir = copiarDiretorio(origemDir, nomeDestino, usuario);
            dirDestino.subdirs.put(nomeDestino, copiaDir);
            return;
        }

        throw new CaminhoNaoEncontradoException("Origem não encontrada.");
    }

    /**
     * Adiciona um usuário no sistema.
     */
    @Override
    public void addUser(String nome, String diretorio, String permissoes) throws CaminhoNaoEncontradoException {
        if (usuarios.containsKey(nome)) {
            throw new IllegalArgumentException("Usuário já existe: " + nome);
        }

        if (diretorio == null || diretorio.isEmpty()) {
            throw new IllegalArgumentException("Diretório inicial não pode ser nulo ou vazio.");
        }

        // Verifica se o diretório já existe
        Diretorio dir = navegarPara(diretorio);
        if (dir == null) {
            throw new CaminhoNaoEncontradoException("Diretório '" + diretorio + "' não encontrado.");
        }

        // Cria o novo usuário
        Usuario novoUsuario = new Usuario(nome, diretorio, permissoes);
        usuarios.put(nome, novoUsuario);
    }

    /**
     * Remove um usuário do sistema.
     */
    @Override
    public void removeUser(String nome) throws CaminhoNaoEncontradoException, PermissaoException{
        Usuario usuario = usuarios.get(nome);
        if (usuario == null) {
            throw new CaminhoNaoEncontradoException("Usuário '" + nome + "' não encontrado.");
        }

        // Verifica se o usuário é o dono do diretório raiz
        if (usuario.getDiretorio().equals("/") && !nome.equals(ROOT_USER)) {
            throw new PermissaoException("Usuário não pode remover o diretório raiz.");
        }

        // Remove o usuário
        usuarios.remove(nome);

        // Remove o diretório do usuário, se existir
        Diretorio dir = navegarPara(usuario.getDiretorio());
        removerRecursivo(dir);
    }

    private boolean temPermissao(String usuario, MetaDados meta, char tipo) {
        if (ROOT_USER.equals(usuario))
            return true;

        String permissao = meta.getPermissao(usuario);
        return permissao != null && permissao.indexOf(tipo) != -1;
    }

    private boolean permiteCriarEm(String usuario, Diretorio dirPai) {
        if (ROOT_USER.equals(usuario))
            return true;

        String permissaoLocal = dirPai.metaDados.getPermissao(usuario);
        if (permissaoLocal != null && permissaoLocal.contains("w")) {
            return true;
        }

        Usuario u = usuarios.get(usuario);
        return u != null && u.getDiretorio().equals("/**") && u.getPermissoes().contains("w");
    }

    private String[] parseCaminho(String caminho) {
        return Arrays.stream(caminho.split("/"))
                .filter(p -> !p.isEmpty())
                .toArray(String[]::new);
    }

    private Diretorio navegarAteDiretorio(String[] partes) throws CaminhoNaoEncontradoException {
        Diretorio atual = raiz;
        for (String parte : partes) {
            if (!atual.subdirs.containsKey(parte)) {
                throw new CaminhoNaoEncontradoException("Diretório '" + parte + "' não encontrado.");
            }
            atual = atual.subdirs.get(parte);
        }
        return atual;
    }

    private Diretorio navegarAteDiretorioPai(String[] partes) throws CaminhoNaoEncontradoException {
        if (partes.length == 1)
            return raiz;
        return navegarAteDiretorio(Arrays.copyOfRange(partes, 0, partes.length - 1));
    }

    private MetaDados encontrarMetaDados(Diretorio dir, String nome)
            throws CaminhoNaoEncontradoException {
        if (dir.arquivos.containsKey(nome)) {
            return dir.arquivos.get(nome).getMetaDados();
        }
        if (dir.subdirs.containsKey(nome)) {
            return dir.subdirs.get(nome).metaDados;
        }
        throw new CaminhoNaoEncontradoException("Arquivo ou diretório '" + nome + "' não encontrado.");
    }

    private void removerRecursivo(Diretorio dir) {
        for (String nome : dir.arquivos.keySet().toArray(new String[0])) {
            dir.arquivos.remove(nome);
        }
        for (String nome : dir.subdirs.keySet().toArray(new String[0])) {
            removerRecursivo(dir.subdirs.get(nome));
            dir.subdirs.remove(nome);
        }
    }
}
