package filesys;

import exception.CaminhoJaExistenteException;
import exception.CaminhoNaoEncontradoException;
import exception.PermissaoException;

public final class FileSystemImpl implements IFileSystem {
    private static final String ROOT_USER = "root";
    private Diretorio raiz;

    public FileSystemImpl() {
        raiz = new Diretorio("/", ROOT_USER, null);
        // root tem permissão total na raiz
        raiz.getMetaDados().setPermissao(ROOT_USER, "rwx");
    }

    // Utilitário: Navega até o diretório/arquivo alvo
    private Diretorio navegarParaDiretorio(String caminho) throws CaminhoNaoEncontradoException {
        if (!caminho.startsWith("/"))
            throw new CaminhoNaoEncontradoException("Caminho inválido: " + caminho);
        if (caminho.equals("/"))
            return raiz;

        String[] partes = caminho.split("/");
        Diretorio atual = raiz;
        for (int i = 1; i < partes.length; i++) {
            if (partes[i].isEmpty())
                continue;
            if (!atual.getSubDiretorio().containsKey(partes[i])) {
                throw new CaminhoNaoEncontradoException("Diretório não encontrado: " + partes[i]);
            }
            atual = atual.getSubDiretorio().get(partes[i]);
        }
        return atual;
    }

    // Retorna [diretorioPai, nomeFinal]
    private Object[] navegarParaPai(String caminho) throws CaminhoNaoEncontradoException {
        if (!caminho.startsWith("/"))
            throw new CaminhoNaoEncontradoException("Caminho inválido: " + caminho);
        int idx = caminho.lastIndexOf('/');
        String pathPai = (idx == 0) ? "/" : caminho.substring(0, idx);
        String nomeFinal = caminho.substring(idx + 1);
        Diretorio pai = navegarParaDiretorio(pathPai);
        return new Object[] { pai, nomeFinal };
    }

    private void checarPermissaoEscrita(Diretorio dir, String usuario) throws PermissaoException {
        if (!dir.getMetaDados().hasPermissao(usuario, 'w')) {
            throw new PermissaoException(
                    "Usuário sem permissão de escrita no diretório " + dir.getMetaDados().getNome());
        }
    }

    private void checarPermissaoLeitura(Diretorio dir, String usuario) throws PermissaoException {
        if (!dir.getMetaDados().hasPermissao(usuario, 'r')) {
            throw new PermissaoException(
                    "Usuário sem permissão de leitura no diretório " + dir.getMetaDados().getNome());
        }
    }

    @Override
    public void mkdir(String caminho, String usuario) throws CaminhoJaExistenteException, PermissaoException {
        try {
            Object[] res = navegarParaPai(caminho);
            Diretorio pai = (Diretorio) res[0];
            String nomeNovo = (String) res[1];

            checarPermissaoEscrita(pai, usuario);

            if (pai.existeSubDiretorio(nomeNovo) || pai.existeArquivo(nomeNovo)) {
                throw new CaminhoJaExistenteException("Já existe arquivo ou diretório com esse nome.");
            }
            Diretorio novo = new Diretorio(nomeNovo, usuario, pai);
            pai.addSubDiretorio(novo);
        } catch (CaminhoNaoEncontradoException e) {
            throw new CaminhoJaExistenteException("Diretório pai não encontrado.");
        }
    }

    @Override
    public void touch(String caminho, String usuario) throws CaminhoJaExistenteException, PermissaoException {
        try {
            Object[] res = navegarParaPai(caminho);
            Diretorio pai = (Diretorio) res[0];
            String nomeNovo = (String) res[1];

            checarPermissaoEscrita(pai, usuario);

            if (pai.existeArquivo(nomeNovo) || pai.existeSubDiretorio(nomeNovo)) {
                throw new CaminhoJaExistenteException("Já existe arquivo ou diretório com esse nome.");
            }
            Arquivo novo = new Arquivo(nomeNovo, usuario);
            pai.addArquivo(novo);
        } catch (CaminhoNaoEncontradoException e) {
            throw new CaminhoJaExistenteException("Diretório pai não encontrado.");
        }
    }

    @Override
    public void ls(String caminho, String usuario, boolean recursivo)
            throws CaminhoNaoEncontradoException, PermissaoException {
        Diretorio dir = navegarParaDiretorio(caminho);
        checarPermissaoLeitura(dir, usuario);
        listarConteudo(dir, "", recursivo);
    }

    private void listarConteudo(Diretorio dir, String prefixo, boolean recursivo) {
        System.out.println(prefixo + dir.getMetaDados().getNome() + "/");
        for (Arquivo arq : dir.getArquivos().values()) {
            System.out.println(prefixo + "  " + arq.getMetaDados().getNome());
        }
        if (recursivo) {
            for (Diretorio sub : dir.getSubDiretorio().values()) {
                listarConteudo(sub, prefixo + "  ", true);
            }
        } else {
            for (Diretorio sub : dir.getSubDiretorio().values()) {
                System.out.println(prefixo + "  " + sub.getMetaDados().getNome() + "/");
            }
        }
    }

    @Override
    public void chmod(String caminho, String usuario, String usuarioAlvo, String permissao)
            throws CaminhoNaoEncontradoException, PermissaoException {
        // Pode ser arquivo ou diretório
        Object[] res = navegarParaPai(caminho);
        Diretorio pai = (Diretorio) res[0];
        String nome = (String) res[1];

        if (pai.existeArquivo(nome)) {
            Arquivo arq = pai.getArquivos().get(nome);
            MetaDados md = arq.getMetaDados();
            if (!usuario.equals(ROOT_USER) && !md.hasPermissao(usuario, 'w')) {
                throw new PermissaoException("Sem permissão para alterar permissões desse arquivo.");
            }
            md.setPermissao(usuarioAlvo, permissao);
        } else if (pai.existeSubDiretorio(nome)) {
            Diretorio dir = pai.getSubDiretorio().get(nome);
            MetaDados md = dir.getMetaDados();
            if (!usuario.equals(ROOT_USER) && !md.hasPermissao(usuario, 'w')) {
                throw new PermissaoException("Sem permissão para alterar permissões desse diretório.");
            }
            md.setPermissao(usuarioAlvo, permissao);
        } else {
            throw new CaminhoNaoEncontradoException("Arquivo/diretório não encontrado para chmod.");
        }
    }

    // Métodos ainda não implementados:
    @Override
    public void rm(String caminho, String usuario, boolean recursivo) {
        throw new UnsupportedOperationException("Método não implementado 'rm'");
    }

    @Override
    public void write(String caminho, String usuario, boolean anexar, byte[] buffer) {
        throw new UnsupportedOperationException("Método não implementado 'write'");
    }

    @Override
    public void read(String caminho, String usuario, byte[] buffer) {
        throw new UnsupportedOperationException("Método não implementado 'read'");
    }

    @Override
    public void mv(String caminhoAntigo, String caminhoNovo, String usuario) {
        throw new UnsupportedOperationException("Método não implementado 'mv'");
    }

    @Override
    public void cp(String caminhoOrigem, String caminhoDestino, String usuario, boolean recursivo) {
        throw new UnsupportedOperationException("Método não implementado 'cp'");
    }
}