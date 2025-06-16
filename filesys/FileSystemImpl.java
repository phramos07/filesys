package filesys;

import java.util.Arrays;

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

    public FileSystemImpl() {
        this.raiz = new Diretorio("/", ROOT_USER);
    }

    @Override
    public void mkdir(String caminho, String usuario) throws CaminhoJaExistenteException, PermissaoException {
        String[] partes = Arrays.stream(caminho.split("/"))
                .filter(p -> !p.isEmpty())
                .toArray(String[]::new);

        if (partes.length == 0) {
            throw new CaminhoJaExistenteException("Não é possível criar o diretório raiz '/'");
        }

        Diretorio atual = raiz;

        for (int i = 0; i < partes.length - 1; i++) {
            String parte = partes[i];
            if (!atual.subdirs.containsKey(parte)) {
                throw new RuntimeException("Diretório pai '" + parte + "' não encontrado.");
            }
            atual = atual.subdirs.get(parte);
        }

        String nomeNovo = partes[partes.length - 1];

        if (atual.subdirs.containsKey(nomeNovo) || atual.arquivos.containsKey(nomeNovo)) {
            throw new CaminhoJaExistenteException("Caminho já existe: " + caminho);
        }

        if (!atual.metaDados.podeEscrever(usuario) && !usuario.equals(ROOT_USER)) {
            throw new PermissaoException("Usuário " + usuario + " não tem permissão de escrita.");
        }

        Diretorio novo = new Diretorio(nomeNovo, usuario);
        atual.subdirs.put(nomeNovo, novo);
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
        String[] partes = Arrays.stream(caminho.split("/"))
                .filter(p -> !p.isEmpty())
                .toArray(String[]::new);

        if (partes.length == 0) {
            throw new CaminhoJaExistenteException("Não é possível criar o arquivo na raiz sem nome");
        }

        Diretorio atual = raiz;

        for (int i = 0; i < partes.length - 1; i++) {
            String parte = partes[i];
            if (!atual.subdirs.containsKey(parte)) {
                throw new RuntimeException("Diretório pai '" + parte + "' não encontrado.");
            }
            atual = atual.subdirs.get(parte);
        }

        String nomeArquivo = partes[partes.length - 1];

        // Verifica se já existe
        if (atual.arquivos.containsKey(nomeArquivo) || atual.subdirs.containsKey(nomeArquivo)) {
            throw new CaminhoJaExistenteException("Já existe um item com esse nome: " + caminho);
        }

        // Verifica permissão de escrita
        if (!atual.metaDados.podeEscrever(usuario) && !usuario.equals(ROOT_USER)) {
            throw new PermissaoException(
                    "Usuário '" + usuario + "' não tem permissão para criar arquivos nesse diretório.");
        }

        Arquivo novoArquivo = new Arquivo(nomeArquivo, usuario);
        atual.arquivos.put(nomeArquivo, novoArquivo);
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
        copia.metaDados.permissoes.put(dono, "rwx");

        for (String nomeArq : original.arquivos.keySet()) {
            Arquivo arq = original.arquivos.get(nomeArq);
            Arquivo novo = new Arquivo(nomeArq, dono);
            novo.conteudo.addAll(arq.conteudo);
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
    public void ls(String caminho, String usuario, boolean recursivo)
            throws CaminhoNaoEncontradoException, PermissaoException {
        String[] partes = Arrays.stream(caminho.split("/"))
                .filter(p -> !p.isEmpty())
                .toArray(String[]::new);

        Diretorio atual = raiz;

        for (String parte : partes) {
            if (!atual.subdirs.containsKey(parte)) {
                throw new CaminhoNaoEncontradoException("Diretório '" + parte + "' não encontrado.");
            }
            atual = atual.subdirs.get(parte);
        }

        // Verifica permissão de leitura
        if (!atual.metaDados.podeLer(usuario) && !usuario.equals(ROOT_USER)) {
            throw new PermissaoException("Usuário '" + usuario + "' não tem permissão de leitura.");
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
            copia.conteudo.addAll(arq.conteudo);
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

    public void addUser(String user) {
        throw new UnsupportedOperationException("Método não implementado 'addUser'");
    }
}
