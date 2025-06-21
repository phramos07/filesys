package filesys;

import exception.CaminhoJaExistenteException;
import exception.CaminhoNaoEncontradoException;
import exception.DiretorioNaoVazioException;
import exception.PermissaoException;

import java.util.Arrays;

public final class FileSystemImpl implements IFileSystem {
    private static final String ROOT_USER = "root";
    private Diretorio raiz;
    private static final int TAMANHO_BLOCO = 1024; // 1KB por bloco

    public FileSystemImpl() {
        raiz = new Diretorio("/", ROOT_USER, null);
        // root tem permissão total na raiz
        raiz.getMetaDados().setPermissao(ROOT_USER, "rwx");
    }

    // Utilitário: Navega até um diretório específico.
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
                throw new CaminhoNaoEncontradoException("Diretório não encontrado: " + partes[i] + " em " + caminho);
            }
            atual = atual.getSubDiretorio().get(partes[i]);
        }
        return atual;
    }

    // Retorna [objetoPai, nomeFinal]. O objeto pai é sempre um Diretorio.
    private Object[] navegarParaPai(String caminho) throws CaminhoNaoEncontradoException {
        if (!caminho.startsWith("/"))
            throw new CaminhoNaoEncontradoException("Caminho inválido: " + caminho);
        if (caminho.equals("/")) {
            return new Object[] { raiz, "/" };
        }
        int idx = caminho.lastIndexOf('/');
        String pathPai = (idx == 0) ? "/" : caminho.substring(0, idx);
        String nomeFinal = caminho.substring(idx + 1);
        if (nomeFinal.isEmpty()) {
            throw new CaminhoNaoEncontradoException("Nome do arquivo/diretório não pode ser vazio.");
        }
        Diretorio pai = navegarParaDiretorio(pathPai);
        return new Object[] { pai, nomeFinal };
    }

    // Retorna o objeto (Arquivo ou Diretorio) no caminho especificado.
    private Object encontrarObjeto(String caminho) throws CaminhoNaoEncontradoException {
        Object[] res = navegarParaPai(caminho);
        Diretorio pai = (Diretorio) res[0];
        String nome = (String) res[1];

        if (pai.existeSubDiretorio(nome)) {
            return pai.getSubDiretorio().get(nome);
        } else if (pai.existeArquivo(nome)) {
            return pai.getArquivos().get(nome);
        } else {
            throw new CaminhoNaoEncontradoException("Arquivo ou diretório não encontrado: " + caminho);
        }
    }

    private void checarPermissaoEscrita(MetaDados meta, String usuario) throws PermissaoException {
        if (!meta.hasPermissao(usuario, 'w')) {
            throw new PermissaoException("Usuário " + usuario + " sem permissão de escrita em " + meta.getNome());
        }
    }

    private void checarPermissaoLeitura(MetaDados meta, String usuario) throws PermissaoException {
        if (!meta.hasPermissao(usuario, 'r')) {
            throw new PermissaoException("Usuário " + usuario + " sem permissão de leitura em " + meta.getNome());
        }
    }

    @Override
    public void mkdir(String caminho, String usuario) throws CaminhoJaExistenteException, PermissaoException {
        try {
            Object[] res = navegarParaPai(caminho);
            Diretorio pai = (Diretorio) res[0];
            String nomeNovo = (String) res[1];

            checarPermissaoEscrita(pai.getMetaDados(), usuario);

            if (pai.existeSubDiretorio(nomeNovo) || pai.existeArquivo(nomeNovo)) {
                throw new CaminhoJaExistenteException("Já existe arquivo ou diretório com esse nome: " + nomeNovo);
            }
            Diretorio novo = new Diretorio(nomeNovo, usuario, pai);
            pai.addSubDiretorio(novo);
        } catch (CaminhoNaoEncontradoException e) {
            throw new CaminhoJaExistenteException("Diretório pai não encontrado para o caminho: " + caminho);
        }
    }

    @Override
    public void touch(String caminho, String usuario) throws CaminhoJaExistenteException, PermissaoException {
        try {
            Object[] res = navegarParaPai(caminho);
            Diretorio pai = (Diretorio) res[0];
            String nomeNovo = (String) res[1];

            checarPermissaoEscrita(pai.getMetaDados(), usuario);

            if (pai.existeArquivo(nomeNovo) || pai.existeSubDiretorio(nomeNovo)) {
                throw new CaminhoJaExistenteException("Já existe arquivo ou diretório com esse nome: " + nomeNovo);
            }
            Arquivo novo = new Arquivo(nomeNovo, usuario);
            pai.addArquivo(novo);
        } catch (CaminhoNaoEncontradoException e) {
            throw new CaminhoJaExistenteException("Diretório pai não encontrado para o caminho: " + caminho);
        }
    }

    @Override
    public void rm(String caminho, String usuario, boolean recursivo)
            throws CaminhoNaoEncontradoException, PermissaoException {
        if (caminho.equals("/")) {
            throw new PermissaoException("Não é permitido remover o diretório raiz.");
        }
        Object[] res = navegarParaPai(caminho);
        Diretorio pai = (Diretorio) res[0];
        String nome = (String) res[1];

        checarPermissaoEscrita(pai.getMetaDados(), usuario);

        if (pai.existeArquivo(nome)) {
            pai.getArquivos().remove(nome);
        } else if (pai.existeSubDiretorio(nome)) {
            Diretorio dirParaRemover = pai.getSubDiretorio().get(nome);
            if (!dirParaRemover.getArquivos().isEmpty() || !dirParaRemover.getSubDiretorio().isEmpty()) {
                if (!recursivo) {
                    throw new DiretorioNaoVazioException("O diretório não está vazio: " + caminho);
                }
                removerRecursivamente(dirParaRemover, usuario);
            }
            pai.getSubDiretorio().remove(nome);
        } else {
            throw new CaminhoNaoEncontradoException("Arquivo ou diretório não encontrado: " + caminho);
        }
    }

    private void removerRecursivamente(Diretorio dir, String usuario) throws PermissaoException {
        checarPermissaoEscrita(dir.getMetaDados(), usuario);

        // Copia as chaves para evitar ConcurrentModificationException
        for (String nomeSubDir : dir.getSubDiretorio().keySet().toArray(new String[0])) {
            removerRecursivamente(dir.getSubDiretorio().get(nomeSubDir), usuario);
            dir.getSubDiretorio().remove(nomeSubDir);
        }
        dir.getArquivos().clear();
    }

    @Override
    public void write(String caminho, String usuario, boolean anexar, byte[] buffer)
            throws CaminhoNaoEncontradoException, PermissaoException {
        Object obj = encontrarObjeto(caminho);
        if (!(obj instanceof Arquivo)) {
            throw new CaminhoNaoEncontradoException("O caminho especificado não é um arquivo: " + caminho);
        }
        Arquivo arq = (Arquivo) obj;
        checarPermissaoEscrita(arq.getMetaDados(), usuario);

        if (!anexar) {
            arq.getBlocos().clear();
            arq.getMetaDados().setTamanho(0);
        }

        int offset = 0;
        while (offset < buffer.length) {
            Bloco bloco = new Bloco(TAMANHO_BLOCO);
            int tamanhoCopia = Math.min(TAMANHO_BLOCO, buffer.length - offset);
            byte[] dadosBloco = Arrays.copyOfRange(buffer, offset, offset + tamanhoCopia);
            bloco.setDados(dadosBloco);
            arq.getBlocos().add(bloco);
            offset += tamanhoCopia;
        }
        arq.getMetaDados().setTamanho(arq.getMetaDados().getTamanho() + buffer.length);
    }

    @Override
    public void read(String caminho, String usuario, byte[] buffer)
            throws CaminhoNaoEncontradoException, PermissaoException {
        Object obj = encontrarObjeto(caminho);
        if (!(obj instanceof Arquivo)) {
            throw new CaminhoNaoEncontradoException("O caminho especificado não é um arquivo: " + caminho);
        }
        Arquivo arq = (Arquivo) obj;
        checarPermissaoLeitura(arq.getMetaDados(), usuario);

        Arrays.fill(buffer, (byte) 0); // Limpa o buffer antes da leitura

        int offset = 0;
        for (Bloco bloco : arq.getBlocos()) {
            byte[] dadosBloco = bloco.getDados();
            int tamanhoCopia = Math.min(dadosBloco.length, buffer.length - offset);
            System.arraycopy(dadosBloco, 0, buffer, offset, tamanhoCopia);
            offset += tamanhoCopia;
            if (offset >= buffer.length) {
                System.out.println("Aviso: Buffer de leitura preenchido. O arquivo pode ser maior que o buffer.");
                break;
            }
        }
    }

    @Override
    public void mv(String caminhoAntigo, String caminhoNovo, String usuario)
            throws CaminhoNaoEncontradoException, PermissaoException {
        if (caminhoAntigo.equals("/") || caminhoNovo.equals("/")) {
            throw new PermissaoException("Não é permitido mover ou renomear o diretório raiz.");
        }

        Object[] resAntigo = navegarParaPai(caminhoAntigo);
        Diretorio paiAntigo = (Diretorio) resAntigo[0];
        String nomeAntigo = (String) resAntigo[1];

        Object[] resNovo = navegarParaPai(caminhoNovo);
        Diretorio paiNovo = (Diretorio) resNovo[0];
        String nomeNovo = (String) resNovo[1];

        checarPermissaoEscrita(paiAntigo.getMetaDados(), usuario);
        if (paiAntigo != paiNovo) {
            checarPermissaoEscrita(paiNovo.getMetaDados(), usuario);
        }

        if (paiNovo.existeArquivo(nomeNovo) || paiNovo.existeSubDiretorio(nomeNovo)) {
            try {
                throw new CaminhoJaExistenteException("Caminho de destino já existe: " + caminhoNovo);
            } catch (CaminhoJaExistenteException e) {
                e.printStackTrace();
            }
        }

        Object objMovido = encontrarObjeto(caminhoAntigo);
        if (objMovido instanceof Arquivo) {
            Arquivo arq = (Arquivo) paiAntigo.getArquivos().remove(nomeAntigo);
            arq.getMetaDados().setNome(nomeNovo);
            paiNovo.addArquivo(arq);
        } else if (objMovido instanceof Diretorio) {
            Diretorio dir = (Diretorio) paiAntigo.getSubDiretorio().remove(nomeAntigo);
            dir.getMetaDados().setNome(nomeNovo);
            dir.setPai(paiNovo); // Atualiza a referência do pai
            paiNovo.addSubDiretorio(dir);
        }
    }

    @Override
    public void cp(String caminhoOrigem, String caminhoDestino, String usuario, boolean recursivo)
            throws CaminhoNaoEncontradoException, PermissaoException {
        Object objOrigem = encontrarObjeto(caminhoOrigem);

        Object[] resDestino = navegarParaPai(caminhoDestino);
        Diretorio paiDestino = (Diretorio) resDestino[0];
        String nomeDestino = (String) resDestino[1];

        checarPermissaoEscrita(paiDestino.getMetaDados(), usuario);

        if (paiDestino.existeArquivo(nomeDestino) || paiDestino.existeSubDiretorio(nomeDestino)) {
            try {
                throw new CaminhoJaExistenteException("Caminho de destino já existe: " + caminhoDestino);
            } catch (CaminhoJaExistenteException e) {
                e.printStackTrace();
            }
        }

        if (objOrigem instanceof Arquivo) {
            copiarArquivo((Arquivo) objOrigem, paiDestino, nomeDestino, usuario);
        } else if (objOrigem instanceof Diretorio) {
            if (!recursivo) {
                throw new PermissaoException("É necessário usar a opção recursiva para copiar diretórios.");
            }
            copiarDiretorioRecursivamente((Diretorio) objOrigem, paiDestino, nomeDestino, usuario);
        }
    }

    private void copiarArquivo(Arquivo arqOrigem, Diretorio paiDestino, String nomeNovo, String usuario)
            throws PermissaoException {
        checarPermissaoLeitura(arqOrigem.getMetaDados(), usuario);

        Arquivo arqNovo = new Arquivo(nomeNovo, usuario);

        // Copia os blocos
        for (Bloco blocoOrigem : arqOrigem.getBlocos()) {
            Bloco blocoNovo = new Bloco(TAMANHO_BLOCO);
            byte[] dadosCopiados = Arrays.copyOf(blocoOrigem.getDados(), blocoOrigem.getDados().length);
            blocoNovo.setDados(dadosCopiados);
            arqNovo.getBlocos().add(blocoNovo);
        }

        arqNovo.getMetaDados().setTamanho(arqOrigem.getMetaDados().getTamanho());
        paiDestino.addArquivo(arqNovo);
    }

    private void copiarDiretorioRecursivamente(Diretorio dirOrigem, Diretorio paiDestino, String nomeNovo,
            String usuario) throws CaminhoNaoEncontradoException, PermissaoException {
        checarPermissaoLeitura(dirOrigem.getMetaDados(), usuario);

        Diretorio dirNovo = new Diretorio(nomeNovo, usuario, paiDestino);
        paiDestino.addSubDiretorio(dirNovo);

        // Copia arquivos
        for (Arquivo arq : dirOrigem.getArquivos().values()) {
            copiarArquivo(arq, dirNovo, arq.getMetaDados().getNome(), usuario);
        }
        // Copia subdiretórios
        for (Diretorio subDir : dirOrigem.getSubDiretorio().values()) {
            copiarDiretorioRecursivamente(subDir, dirNovo, subDir.getMetaDados().getNome(), usuario);
        }
    }

    @Override
    public void ls(String caminho, String usuario, boolean recursivo)
            throws CaminhoNaoEncontradoException, PermissaoException {
        Diretorio dir = navegarParaDiretorio(caminho);
        checarPermissaoLeitura(dir.getMetaDados(), usuario);
        System.out.println("Listando " + caminho + ":");
        listarConteudo(dir, "", recursivo);
    }

    private void listarConteudo(Diretorio dir, String prefixo, boolean recursivo) {
        for (Diretorio sub : dir.getSubDiretorio().values()) {
            System.out.println(prefixo + sub.getMetaDados().getNome() + "/");
            if (recursivo) {
                listarConteudo(sub, prefixo + "  ", true);
            }
        }
        for (Arquivo arq : dir.getArquivos().values()) {
            System.out.println(prefixo + arq.getMetaDados().getNome());
        }
    }

    @Override
    public void chmod(String caminho, String usuario, String usuarioAlvo, String permissao)
            throws CaminhoNaoEncontradoException, PermissaoException {
        Object obj = encontrarObjeto(caminho);
        MetaDados md;

        if (obj instanceof Arquivo) {
            md = ((Arquivo) obj).getMetaDados();
        } else {
            md = ((Diretorio) obj).getMetaDados();
        }

        // Apenas o dono ou o root podem alterar permissões
        if (!usuario.equals(ROOT_USER) && !usuario.equals(md.getDono())) {
            throw new PermissaoException("Apenas o dono do arquivo/diretório ou root podem alterar permissões.");
        }
        md.setPermissao(usuarioAlvo, permissao);
    }
}