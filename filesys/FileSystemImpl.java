package filesys;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import exception.CaminhoJaExistenteException;
import exception.CaminhoNaoEncontradoException;
import exception.PermissaoException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

// Implemente nesta classe o seu código do FileSystem.
// A classe pode ser alterada.
// O construtor, argumentos do construtor podem ser modificados 
// e atributos & métodos privados podem ser adicionados
public final class FileSystemImpl implements IFileSystem {
    private static final String ROOT_USER = "root";

    private List<Usuario> usuarios = new ArrayList<>();
    private Dir raiz; // Diretório raiz

    public FileSystemImpl() {
        usuarios.add(new Usuario(ROOT_USER, "/", "rwx")); // Adiciona o usuário root com permissões totais
        this.raiz = new Dir("/", ROOT_USER, "rwx");
    }

    public FileSystemImpl(List<Usuario> usuarios) {
        if (usuarios.isEmpty()) {
            throw new IllegalArgumentException("Lista de usuários não pode ser nula");
        }

        this.usuarios = usuarios;
        this.raiz = new Dir("/", ROOT_USER, "rwx");
    }

    // Método auxiliar para navegar até o diretório especificado pelo caminho
    private Dir irPara(String caminho) throws CaminhoNaoEncontradoException {
        if (caminho.equals("/") || caminho.isEmpty()) {
            return raiz;
        }

        Dir diretorioAtual = raiz;

        String[] partes = caminho.split("/");

        for (String parte : partes) {
            if (parte == null || parte.isEmpty())
                continue;
            if (!diretorioAtual.getFilhos().containsKey(parte)) {
                throw new CaminhoNaoEncontradoException("Caminho não encontrado: " + caminho);
            }
            diretorioAtual = diretorioAtual.getFilhos().get(parte);
        }

        return diretorioAtual;
    }

    // Lista o conteúdo de um diretório e, se recursivo=true, lista também os
    // subdiretórios
    private String lsRecursivo(Dir diretorio, String caminho, boolean recursivo, String usuario) {
        StringBuilder saida = new StringBuilder();
        String nomeCaminho = (caminho == null || caminho.isEmpty() || !caminho.startsWith("/")) ? "/" + caminho
                : caminho;
        saida.append(nomeCaminho).append(":\n");

        // Lista todos os filhos (arquivos e diretórios) do diretório atual
        if (diretorio.getFilhos().isEmpty()) {
            saida.append("  - Vazio\n");
        } else {
            for (Dir filho : diretorio.getFilhos().values()) {
                saida.append(filho.toString()).append("\n");
            }
        }

        // Se for para listar recursivamente, faz o mesmo para cada subdiretório
        if (recursivo) {
            for (Dir filho : diretorio.getFilhos().values()) {
                if (!filho.isArquivo()) { // Só entra em diretórios
                    String novoCaminho = caminho.equals("/") ? "/" + filho.getNome() : caminho + "/" + filho.getNome();
                    saida.append(lsRecursivo(filho, novoCaminho, true, usuario));
                }
            }
        }

        return saida.toString();
    }

    @Override
    public void mkdir(String caminho, String usuario) throws CaminhoJaExistenteException, PermissaoException {
        if (caminho == null || usuario == null) {
            throw new IllegalArgumentException("Caminho e usuário não podem ser nulos");
        }

        // Normaliza o caminho para formato UNIX
        caminho = caminho.replace("\\", "/"); // Converte barras invertidas para barras normais

        // Remove barras finais
        if (caminho.length() > 1 && caminho.endsWith("/")) {
            caminho = caminho.substring(0, caminho.length() - 1);
        }

        if (caminho.equals("/")) {
            throw new UnsupportedOperationException("Não é possível criar diretório raiz pois ele já existe.");
        }

        if (raiz == null) {
            System.err.println("Diretório raiz: " + raiz);
            throw new IllegalStateException("Diretório raiz não foi inicializado.");
        }

        // Separar caminho em partes
        String[] partes = caminho.split("/");
        Dir diretorioAtual = raiz; // Começa no diretório raiz
        StringBuilder caminhoAtual = new StringBuilder("/");

        for (int i = 0; i < partes.length; i++) {
            String parte = partes[i];

            if (parte == null || parte.isEmpty()) {
                continue; // Ignora partes vazias
            }

            // Impede nomes inválidos de diretório
            if (parte.contains("/") || parte.contains("\\")) {
                throw new IllegalArgumentException("Nome de diretório inválido: " + parte);
            }

            caminhoAtual.append(parte).append("/");

            // Verifica se é o último diretório a ser criado
            boolean isUltimoDiretorio = (i == partes.length - 1);

            // Verifica se o diretório já existe
            if (diretorioAtual.getFilhos().containsKey(parte)) {
                if (diretorioAtual.getFilhos().get(parte).isArquivo()) {
                    throw new CaminhoJaExistenteException("Caminho já existe como arquivo: " + caminhoAtual);
                }

                diretorioAtual = diretorioAtual.getFilhos().get(parte); // Ir para o diretório existente

                if (isUltimoDiretorio) {
                    throw new CaminhoJaExistenteException("Diretório já existe: " + caminhoAtual);
                }

                continue; // Se já existe, não cria novamente
            }

            // Verifica se o usuário existe
            usuarios.stream()
                    .filter(u -> u.getNome().equals(usuario))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + usuario));

            // Verifica se o usuário tem permissão para criar o diretório
            if (!diretorioAtual.temPerm(usuario, "w")) {
                throw new PermissaoException("Usuário não tem permissão para criar diretório: " + caminhoAtual);
            }

            // Cria o novo diretório
            Dir novoDiretorio = new Dir(parte, usuario, "rwx");
            diretorioAtual.addFilho(novoDiretorio);
            diretorioAtual = novoDiretorio; // Move para o novo diretório criado

            System.out.println("Diretório criado: " + caminhoAtual);
        }
    }

    @Override
    public void chmod(String caminho, String usuario, String usuarioAlvo, String permissao)
            throws CaminhoNaoEncontradoException, PermissaoException {
        if (caminho == null || usuario == null || usuarioAlvo == null || permissao == null) {
            throw new IllegalArgumentException("Caminho, usuário, usuário alvo e permissão não podem ser nulos");
        }

        // Verifica se o usuário existe
        usuarios.stream()
                .filter(u -> u.getNome().equals(usuarioAlvo))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + usuarioAlvo));

        if (!permissao.matches("^[rwx-]{3}$")) {
            throw new IllegalArgumentException("Permissão inválida: " + permissao);
        }

        caminho = caminho.replace("\\", "/");
        if (caminho.endsWith("/"))
            caminho = caminho.substring(0, caminho.length() - 1);

        Dir dir = irPara(caminho);

        if (!dir.temPerm(usuario, "w")) {
            throw new PermissaoException("Usuário não tem permissão para alterar permissões: " + caminho);
        }

        dir.setPermissoesUsuario(usuarioAlvo, permissao);
        System.out.println("Permissão alterada para " + usuarioAlvo + " em " + caminho + ": " + permissao);
    }

    @Override
    public void rm(String caminho, String usuario, boolean recursivo)
            throws CaminhoNaoEncontradoException, PermissaoException {
        if (caminho == null || usuario == null) {
            throw new IllegalArgumentException("Caminho e usuário não podem ser nulos");
        }

        caminho = caminho.replace("\\", "/");
        if (caminho.endsWith("/"))
            caminho = caminho.substring(0, caminho.length() - 1);

        Dir dir = irPara(caminho);

        if (!dir.temPerm(usuario, "w")) {
            throw new PermissaoException("Usuário não tem permissão para remover: " + caminho);
        }
        if (dir.temSubdiretorios() && !recursivo) {
            throw new PermissaoException(
                    "Esse diretório contém subdiretórios. Use o parâmetro recursivo para removê-lo.");
        }

        if (dir.isArquivo()) {
            dir.getPai().removeFilho(dir.getNome());
            System.out.println("Arquivo removido: " + caminho);
        } else {
            if (recursivo) {
                for (Dir filho : dir.getFilhos().values()) {
                    rm(filho.getCaminhoCompleto(), usuario, true);
                }
            }
            dir.getPai().removeFilho(dir.getNome());
            System.out.println("Diretório removido: " + caminho);
        }
    }

    @Override
    public void touch(String caminho, String usuario)
            throws CaminhoJaExistenteException, PermissaoException, CaminhoNaoEncontradoException {
        if (caminho == null || usuario == null) {
            throw new IllegalArgumentException("Caminho e usuário não podem ser nulos");
        }

        caminho = caminho.replace("\\", "/");
        if (caminho.endsWith("/"))
            caminho = caminho.substring(0, caminho.length() - 1);

        int idx = caminho.lastIndexOf('/');
        String nomeArquivo = caminho.substring(idx + 1);
        String caminhoPai = (idx <= 0) ? "/" : caminho.substring(0, idx);

        Dir dirPai = irPara(caminhoPai);

        if (dirPai.getFilhos().containsKey(nomeArquivo)) {
            throw new CaminhoJaExistenteException("Arquivo ou diretório já existe: " + caminho);
        }

        if (!dirPai.temPerm(usuario, "w")) {
            throw new PermissaoException("Usuário não tem permissão para criar arquivo: " + caminho);
        }

        File novoArquivo = new File(nomeArquivo, usuario, "rwx");
        dirPai.addFilho(novoArquivo);
        System.out.println("Arquivo criado: " + caminho);
    }

    @Override
    public void write(String caminho, String usuario, boolean anexar, byte[] buffer)
            throws CaminhoNaoEncontradoException, PermissaoException {
        if (caminho == null || usuario == null || buffer == null) {
            throw new IllegalArgumentException("Caminho, usuário e buffer não podem ser nulos");
        }

        caminho = caminho.replace("\\", "/");
        if (caminho.endsWith("/")) {
            caminho = caminho.substring(0, caminho.length() - 1);
        }

        Dir diretorio = irPara(caminho);

        if (!diretorio.isArquivo()) {
            throw new IllegalArgumentException("O caminho especificado não é um arquivo: " + caminho);
        }

        if (!diretorio.temPerm(usuario, "w")) {
            throw new PermissaoException("Usuário não tem permissão para escrever: " + caminho);
        }

        File arquivo = (File) diretorio;
        if (!anexar) arquivo.limparBlocos();

        int offset = 0;

        while (offset < buffer.length) {
            // Tamanho do bloco a ser escrito
            int tamanhoBloco = Math.min(buffer.length - offset, File.TAMANHO_BYTES_BLOCO);
            // Bloco de dados a ser escrito
            byte[] blocoDados = new byte[tamanhoBloco];
            // Copiar dados para o bloco
            System.arraycopy(buffer, offset, blocoDados, 0, tamanhoBloco);

            // Criar novo bloco de dados
            BlocoDeDados novoBloco = new BlocoDeDados(blocoDados);
            // Adicionar bloco ao arquivo
            arquivo.addBloco(novoBloco);
            // Atualizar offset para o próximo bloco
            offset += tamanhoBloco;
        }
    }

    @Override
    public int read(String caminho, String usuario, byte[] buffer, int offset)
            throws CaminhoNaoEncontradoException, PermissaoException {
        if (caminho == null || usuario == null || buffer == null) {
            throw new IllegalArgumentException("Caminho, usuário e buffer não podem ser nulos");
        }

        caminho = caminho.replace("\\", "/");
        if (caminho.endsWith("/")) {
            caminho = caminho.substring(0, caminho.length() - 1);
        }

        Dir diretorio = irPara(caminho);

        if (!diretorio.isArquivo()) {
            throw new IllegalArgumentException("O caminho especificado não é um arquivo: " + caminho);
        }

        if (!diretorio.temPerm(usuario, "r")) {
            throw new PermissaoException("Usuário não tem permissão para ler: " + caminho);
        }

        File arquivo = (File) diretorio;
        int bufferOffset = 0;
        int arquivoOffset = 0;

        // Lê os blocos do arquivo e copia para o buffer
        for (BlocoDeDados bloco : arquivo.getBlocos()) {
            byte[] dadosBloco = bloco.getDados();
            int tamanhoBloco = dadosBloco.length;

            // Se o offset ainda não foi alcançado, pula bytes
            if (arquivoOffset + tamanhoBloco <= offset) {
                arquivoOffset += tamanhoBloco;
                continue;
            }

            // Calcula o início da leitura dentro do bloco
            int inicioLeitura = Math.max(0, offset - arquivoOffset);
            int bytesDisponiveis = tamanhoBloco - inicioLeitura;
            int bytesRestantes = Math.min(buffer.length - bufferOffset, bytesDisponiveis);

            // Copia os dados do bloco para o buffer
            System.arraycopy(dadosBloco, inicioLeitura, buffer, bufferOffset, bytesRestantes);
            bufferOffset += bytesRestantes;

            // Se o buffer estiver cheio, sai do loop
            if (bufferOffset >= buffer.length) {
                break;
            }
        }

        return bufferOffset;
    }

    @Override
    public void mv(String caminhoAntigo, String caminhoNovo, String usuario)
            throws CaminhoNaoEncontradoException, PermissaoException {
        if (caminhoAntigo == null || caminhoNovo == null || usuario == null) {
            throw new PermissaoException("Caminho antigo, caminho novo e usuário não podem ser nulos");
        }

        caminhoAntigo = caminhoAntigo.replace("\\", "/");
        caminhoNovo = caminhoNovo.replace("\\", "/");

        if (caminhoAntigo.endsWith("/")) {
            caminhoAntigo = caminhoAntigo.substring(0, caminhoAntigo.length() - 1);
        }
        if (caminhoNovo.endsWith("/")) {
            caminhoNovo = caminhoNovo.substring(0, caminhoNovo.length() - 1);
        }

        if (caminhoAntigo.equals(caminhoNovo)) {
            throw new PermissaoException("Caminho antigo e caminho novo não podem ser iguais");
        }

        Dir dirantigo = irPara(caminhoAntigo);

        if (!dirantigo.temPerm(usuario, "w")) {
            throw new PermissaoException("Usuário não tem permissão para mover: " + caminhoAntigo);
        }

        try {
            Dir destino = irPara(caminhoNovo);
            if (!destino.temPerm(usuario, "w")) {
                throw new PermissaoException("Sem permissão de escrita no destino");
            }
            if (destino.getFilhos().containsKey(dirantigo.getNome())) {
                throw new PermissaoException("Já existe um diretório ou arquivo com esse nome no destino");
            }

            dirantigo.getPai().removeFilho(dirantigo.getNome());
            destino.addFilho(dirantigo);

        } catch (CaminhoNaoEncontradoException e) {

            int idx = caminhoNovo.lastIndexOf('/');
            String novoNome = caminhoNovo.substring(idx + 1);
            String caminhoPaiNovo = (idx <= 0) ? "/" : caminhoNovo.substring(0, idx);
            Dir novoPai = irPara(caminhoPaiNovo);

            if (!novoPai.temPerm(usuario, "w")) {
                throw new PermissaoException("Sem permissão de escrita no novo caminho");
            }
            if (novoPai.getFilhos().containsKey(novoNome)) {
                throw new IllegalArgumentException("Já existe um objeto com esse nome no destino");
            }

            dirantigo.getPai().removeFilho(dirantigo.getNome());
            dirantigo.setNome(novoNome);
            novoPai.addFilho(dirantigo);
        }
    }

    @Override
    public void ls(String caminho, String usuario, boolean recursivo)
            throws CaminhoNaoEncontradoException, PermissaoException {
        Dir diretorio = irPara(caminho);

        if (!diretorio.temPerm(usuario, "r")) {
            throw new PermissaoException("Você não tem permissão para listar este diretório!");
        }

        String output = lsRecursivo(diretorio, caminho, recursivo, usuario);
        System.out.print(output);
    }

    @Override
    public void cp(String caminhoOrigem, String caminhoDestino, String usuario, boolean recursivo)
            throws CaminhoNaoEncontradoException, PermissaoException {
        if (caminhoOrigem == null || caminhoDestino == null || usuario == null) {
            throw new IllegalArgumentException("Caminho de origem, destino e usuário não podem ser nulos");
        }
        caminhoOrigem = caminhoOrigem.replace("\\", "/");
        caminhoDestino = caminhoDestino.replace("\\", "/");
        if (caminhoOrigem.endsWith("/")) {
            caminhoOrigem = caminhoOrigem.substring(0, caminhoOrigem.length() - 1);
        }
        if (caminhoDestino.endsWith("/")) {
            caminhoDestino = caminhoDestino.substring(0, caminhoDestino.length() - 1);
        }
        Dir dirOrigem = irPara(caminhoOrigem);
        Dir dirDestino = irPara(caminhoDestino);
        if (!dirOrigem.temPerm(usuario, "r")) {
            throw new PermissaoException("Usuário não tem permissão para ler o diretório de origem: " + caminhoOrigem);
        }
        if (!dirDestino.temPerm(usuario, "w")) {
            throw new PermissaoException(
                    "Usuário não tem permissão para escrever no diretório de destino: " + caminhoDestino);
        }

        String nomeArquivo = dirOrigem.getNome();
        String caminhoCompletoDestino = dirDestino.getCaminhoCompleto() + "/" + nomeArquivo;
        if (dirDestino.getFilhos().containsKey(nomeArquivo)) {
            throw new PermissaoException("Arquivo ou diretório já existe no destino: " + caminhoCompletoDestino);
        }
        if (dirOrigem.isArquivo()) {
            File novoArquivo = new File(nomeArquivo, usuario, "rwx");
            dirDestino.addFilho(novoArquivo);
            System.out.println("Arquivo copiado de " + caminhoOrigem + " para " + caminhoCompletoDestino);
        } else {
            Dir novoDiretorio = new Dir(nomeArquivo, usuario, "rwx");
            dirDestino.addFilho(novoDiretorio);
            for (Dir filho : dirOrigem.getFilhos().values()) {
                String caminhoFilhoOrigem = filho.getCaminhoCompleto();
                String caminhoFilhoDestino = novoDiretorio.getCaminhoCompleto() + "/" + filho.getNome();
                cp(caminhoFilhoOrigem, caminhoFilhoDestino, usuario, recursivo);
            }
            System.out.println("Diretório copiado de " + caminhoOrigem + " para " + caminhoCompletoDestino);
        }

    }

    @Override
    public void addUser(Usuario usuario) {
        if (usuario == null || usuario.getNome() == null || usuario.getPermissoes() == null) {
            throw new IllegalArgumentException("Usuário, nome e permissões não podem ser nulos");
        }

        // Verifica se o usuário já existe
        for (Usuario u : usuarios) {
            if (u.getNome().equals(usuario.getNome())) {
                throw new IllegalArgumentException("Usuário já existe: " + usuario.getNome());
            }
        }

        // Adiciona o novo usuário
        usuarios.add(usuario);
        System.out.println("Usuário adicionado: " + usuario.getNome());
    }
}
