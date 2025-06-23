package filesys;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import core.Arquivo;
import core.Diretorio;
import core.ElementoFS;
import core.Usuario;
import exception.CaminhoJaExistenteException;
import exception.CaminhoNaoEncontradoException;
import exception.PermissaoException;

public final class FileSystemImpl implements IFileSystem {
    private static final String ROOT_USER = "root";
    private final Diretorio raiz;
    private final Map<String, Usuario> usuarios;

    public FileSystemImpl(List<Usuario> listaUsuarios) {
        this.raiz = new Diretorio("/", "rwx", ROOT_USER);
        this.usuarios = new HashMap<>();
        usuarios.put(ROOT_USER, new Usuario(ROOT_USER, "rwx", "/"));
        for (Usuario u : listaUsuarios) {
            if (!u.getIdentificador().equalsIgnoreCase(ROOT_USER)) {
                usuarios.put(u.getIdentificador(), u);
            }
        }
        // Aqui você pode carregar os usuários do arquivo users, se desejar
        // Exemplo: usuarios.put("root", new Usuario("root", "rwx", "/"));
    }

    private Diretorio navegarParaDiretorioPai(String caminho) throws CaminhoNaoEncontradoException {
        String[] partes = caminho.split("/");
        Diretorio atual = raiz;
        for (int i = 1; i < partes.length - 1; i++) {
            ElementoFS filho = atual.getConteudo().get(partes[i]);
            if (filho == null || filho.isArquivo()) {
                throw new CaminhoNaoEncontradoException("Diretório não encontrado: " + partes[i]);
            }
            atual = (Diretorio) filho;
        }
        return atual;
    }

    private ElementoFS buscarElemento(String caminho) throws CaminhoNaoEncontradoException {
        if ("/".equals(caminho))
            return raiz;
        String[] partes = caminho.split("/");
        Diretorio atual = raiz;
        for (int i = 1; i < partes.length - 1; i++) {
            ElementoFS filho = atual.getConteudo().get(partes[i]);
            if (filho == null || filho.isArquivo()) {
                throw new CaminhoNaoEncontradoException("Diretório não encontrado: " + partes[i]);
            }
            atual = (Diretorio) filho;
        }
        ElementoFS alvo = atual.getConteudo().get(partes[partes.length - 1]);
        if (alvo == null)
            throw new CaminhoNaoEncontradoException("Elemento não encontrado: " + caminho);
        return alvo;
    }

    public ElementoFS buscarElementoTeste(String caminho) throws CaminhoNaoEncontradoException {
        return buscarElemento(caminho);
    }

    @Override
    public void mkdir(String caminho, String usuario) throws CaminhoJaExistenteException, PermissaoException {
        Diretorio pai;
        try {
            pai = navegarParaDiretorioPai(caminho);
        } catch (CaminhoNaoEncontradoException e) {
            throw new PermissaoException("Diretório pai não encontrado: " + caminho);
        }
        String nomeNovo = caminho.substring(caminho.lastIndexOf('/') + 1);
        if (pai.getConteudo().containsKey(nomeNovo)) {
            throw new CaminhoJaExistenteException("Já existe um arquivo ou diretório com esse nome.");
        }
    if (!pai.possuiPermissao(usuario, 'w') || !pai.possuiPermissao(usuario, 'x')) {
            throw new PermissaoException("Usuário sem permissão de escrita no diretório.");
        }
        Diretorio novoDir = new Diretorio(nomeNovo, "rwx", usuario);
        pai.inserirElemento(novoDir);
    }

    @Override
    public void touch(String caminho, String usuario) throws CaminhoJaExistenteException, PermissaoException {
        Diretorio pai;
        try {
            pai = navegarParaDiretorioPai(caminho);
        } catch (CaminhoNaoEncontradoException e) {
            throw new PermissaoException("Diretório pai não encontrado: " + caminho);
        }
        String nomeArquivo = caminho.substring(caminho.lastIndexOf('/') + 1);
        if (pai.getConteudo().containsKey(nomeArquivo)) {
            throw new CaminhoJaExistenteException("Já existe um arquivo ou diretório com esse nome.");
        }
        if (!pai.possuiPermissao(usuario, 'w')) {
            throw new PermissaoException("Usuário sem permissão de escrita no diretório.");
        }
        Arquivo novoArq = new Arquivo(nomeArquivo, "rw-", usuario);
        pai.inserirElemento(novoArq);
    }

    @Override
    public void chmod(String caminho, String usuario, String usuarioAlvo, String permissao)
            throws CaminhoNaoEncontradoException, PermissaoException {
        ElementoFS elemento = buscarElemento(caminho);
        if (!elemento.getDonoDiretorio().equals(usuario) && !"root".equals(usuario)) {
            throw new PermissaoException("Apenas o dono ou root pode alterar permissões.");
        }
        if (elemento instanceof Diretorio) {
            ((Diretorio) elemento).definirPermissao(usuarioAlvo, permissao);
        } else {
            elemento.setPermissoesPadrao(permissao); // Para arquivos, pode ser diferente
        }
    }

    @Override
    public void rm(String caminho, String usuario, boolean recursivo)
            throws CaminhoNaoEncontradoException, PermissaoException {
        Diretorio pai = navegarParaDiretorioPai(caminho);
        String nome = caminho.substring(caminho.lastIndexOf('/') + 1);
        ElementoFS alvo = pai.getConteudo().get(nome);
        if (alvo == null)
            throw new CaminhoNaoEncontradoException("Elemento não encontrado.");
        if (!pai.possuiPermissao(usuario, 'w')) {
            throw new PermissaoException("Sem permissão para remover.");
        }
        if (alvo instanceof Diretorio && !recursivo && !((Diretorio) alvo).getConteudo().isEmpty()) {
            throw new PermissaoException("Diretório não vazio. Use recursivo.");
        }
        pai.excluirElemento(nome);
    }

    @Override
    public void write(String caminho, String usuario, boolean anexar, byte[] buffer)
            throws CaminhoNaoEncontradoException, PermissaoException {
        ElementoFS elemento = buscarElemento(caminho);
        if (!elemento.isArquivo())
            throw new CaminhoNaoEncontradoException("Não é um arquivo.");
        if (!elemento.getDonoDiretorio().equals(usuario) && !"root".equals(usuario)) {
            throw new PermissaoException("Sem permissão para escrever.");
        }
        Arquivo arquivo = (Arquivo) elemento;
        if (!anexar)
            arquivo.limpar();
        arquivo.adicionarBloco(buffer);
    }

    @Override
    public void read(String caminho, String usuario, byte[] buffer)
            throws CaminhoNaoEncontradoException, PermissaoException {
        ElementoFS elemento = buscarElemento(caminho);
        if (!elemento.isArquivo())
            throw new CaminhoNaoEncontradoException("Não é um arquivo.");
        if (!elemento.getDonoDiretorio().equals(usuario) && !"root".equals(usuario)) {
            throw new PermissaoException("Sem permissão para leitura.");
        }
        Arquivo arquivo = (Arquivo) elemento;
        List<byte[]> blocos = arquivo.getBlocos();
        int offset = 0;
        for (byte[] bloco : blocos) {
            int len = Math.min(bloco.length, buffer.length - offset);
            System.arraycopy(bloco, 0, buffer, offset, len);
            offset += len;
            if (offset >= buffer.length)
                break;
        }
    }

    @Override
    public void mv(String caminhoAntigo, String caminhoNovo, String usuario)
            throws CaminhoNaoEncontradoException, PermissaoException {
        Diretorio paiAntigo, paiNovo;
        try {
            paiAntigo = navegarParaDiretorioPai(caminhoAntigo);
            paiNovo = navegarParaDiretorioPai(caminhoNovo);
        } catch (CaminhoNaoEncontradoException e) {
            throw new PermissaoException("Diretório pai não encontrado.");
        }
        String nomeAntigo = caminhoAntigo.substring(caminhoAntigo.lastIndexOf('/') + 1);
        String nomeNovo = caminhoNovo.substring(caminhoNovo.lastIndexOf('/') + 1);
        ElementoFS elemento = paiAntigo.getConteudo().get(nomeAntigo);
        if (elemento == null)
            throw new CaminhoNaoEncontradoException("Elemento não encontrado.");
        if (!paiAntigo.possuiPermissao(usuario, 'w')) {
            throw new PermissaoException("Sem permissão para mover.");
        }
        if (paiNovo.getConteudo().containsKey(nomeNovo)) {
            throw new PermissaoException("Já existe um elemento com esse nome no destino.");
        }
        paiAntigo.excluirElemento(nomeAntigo);
        elemento.setNomeDiretorio(nomeNovo);
        paiNovo.inserirElemento(elemento);
    }

    @Override
    public void ls(String caminho, String usuario, boolean recursivo)
            throws CaminhoNaoEncontradoException, PermissaoException {
        ElementoFS elemento = buscarElemento(caminho);
        if (!(elemento instanceof Diretorio))
            throw new CaminhoNaoEncontradoException("Não é um diretório.");
        Diretorio dir = (Diretorio) elemento;
        if (!dir.possuiPermissao(usuario, 'r')) {
            throw new PermissaoException("Sem permissão para leitura.");
        }
        listarConteudo(dir, recursivo, "");
    }

    private void listarConteudo(Diretorio dir, boolean recursivo, String prefixo) {
        for (ElementoFS filho : dir.getConteudo().values()) {
            System.out.println(prefixo + filho.getNomeDiretorio());
            if (recursivo && filho instanceof Diretorio) {
                listarConteudo((Diretorio) filho, true, prefixo + "  ");
            }
        }
    }

    @Override
    public void cp(String caminhoOrigem, String caminhoDestino, String usuario, boolean recursivo)
            throws CaminhoNaoEncontradoException, PermissaoException {
        ElementoFS origem = buscarElemento(caminhoOrigem);
        Diretorio destinoPai;
        try {
            destinoPai = navegarParaDiretorioPai(caminhoDestino);
        } catch (CaminhoNaoEncontradoException e) {
            throw new PermissaoException("Diretório pai não encontrado: " + caminhoDestino);
        }
        String nomeDestino = caminhoDestino.substring(caminhoDestino.lastIndexOf('/') + 1);
        if (destinoPai.getConteudo().containsKey(nomeDestino)) {
            throw new PermissaoException("Já existe um elemento com esse nome no destino.");
        }
        if (!destinoPai.possuiPermissao(usuario, 'w')) {
            throw new PermissaoException("Sem permissão para copiar.");
        }
        ElementoFS copia = copiarElemento(origem, recursivo);
        copia.setNomeDiretorio(nomeDestino);
        destinoPai.inserirElemento(copia);
    }

    private ElementoFS copiarElemento(ElementoFS elemento, boolean recursivo) {
        if (elemento instanceof Arquivo) {
            Arquivo arq = (Arquivo) elemento;
            Arquivo novo = new Arquivo(arq.getNomeDiretorio(), arq.getPermissoesPadrao(), arq.getDonoDiretorio());
            for (byte[] bloco : arq.getBlocos()) {
                novo.adicionarBloco(Arrays.copyOf(bloco, bloco.length));
            }
            return novo;
        } else if (elemento instanceof Diretorio) {
            Diretorio dir = (Diretorio) elemento;
            Diretorio novoDir = new Diretorio(dir.getNomeDiretorio(), dir.getPermissoesPadrao(),
                    dir.getDonoDiretorio());
            if (recursivo) {
                for (ElementoFS filho : dir.getConteudo().values()) {
                    novoDir.inserirElemento(copiarElemento(filho, true));
                }
            }
            return novoDir;
        }
        throw new IllegalArgumentException("Tipo desconhecido.");
    }

    public void addUser(String user) {
        // Implemente se desejar gerenciar usuários dinamicamente
    }
}