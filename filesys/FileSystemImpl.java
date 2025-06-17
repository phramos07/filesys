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
                throw new CaminhoNaoEncontradoException("Diretório '" + parte + "' não encontrado.");
            }
            atual = atual.subdirs.get(parte);
        }

        String nomeAlvo = partes[partes.length - 1];

        // Pode ser arquivo ou diretório
        MetaDados meta = null;
        if (atual.arquivos.containsKey(nomeAlvo)) {
            meta = atual.arquivos.get(nomeAlvo).getMetaDados();
        } else if (atual.subdirs.containsKey(nomeAlvo)) {
            meta = atual.subdirs.get(nomeAlvo).metaDados;
        } else {
            throw new CaminhoNaoEncontradoException("Arquivo ou diretório '" + nomeAlvo + "' não encontrado.");
        }

        // Só root ou dono pode alterar permissões
        if (!usuario.equals(ROOT_USER) && !usuario.equals(meta.getDono())) {
            throw new PermissaoException("Apenas root ou o dono pode alterar permissões.");
        }

        // Atualiza permissões do usuário alvo
        meta.setPermissao(usuarioAlvo, permissao);
    }

    @Override
    public void rm(String caminho, String usuario, boolean recursivo)
            throws CaminhoNaoEncontradoException, PermissaoException {
        String[] partes = Arrays.stream(caminho.split("/"))
                .filter(p -> !p.isEmpty())
                .toArray(String[]::new);

        if (partes.length == 0) {
            throw new PermissaoException("Não é possível remover a raiz.");
        }

        Diretorio atual = raiz;
        for (int i = 0; i < partes.length - 1; i++) {
            String parte = partes[i];
            if (!atual.subdirs.containsKey(parte)) {
                throw new CaminhoNaoEncontradoException("Diretório '" + parte + "' não encontrado.");
            }
            atual = atual.subdirs.get(parte);
        }

        String nomeAlvo = partes[partes.length - 1];

        // Verifica permissão de escrita no diretório pai
        if (!atual.metaDados.podeEscrever(usuario) && !usuario.equals(ROOT_USER)) {
            throw new PermissaoException("Usuário '" + usuario + "' não tem permissão de escrita.");
        }

        // Remover arquivo
        if (atual.arquivos.containsKey(nomeAlvo)) {
            atual.arquivos.remove(nomeAlvo);
            return;
        }

        // Remover diretório
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

    // Função auxiliar para remoção recursiva
    private void removerRecursivo(Diretorio dir) {
        for (String nome : dir.arquivos.keySet().toArray(new String[0])) {
            dir.arquivos.remove(nome);
        }
        for (String nome : dir.subdirs.keySet().toArray(new String[0])) {
            removerRecursivo(dir.subdirs.get(nome));
            dir.subdirs.remove(nome);
        }
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

    @Override
    public void mv(String caminhoAntigo, String caminhoNovo, String usuario)
            throws CaminhoNaoEncontradoException, PermissaoException {
        throw new UnsupportedOperationException("Método não implementado 'mv'");
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
        throw new UnsupportedOperationException("Método não implementado 'cp'");
    }

    public void addUser(String user) {
        throw new UnsupportedOperationException("Método não implementado 'addUser'");
    }
}
