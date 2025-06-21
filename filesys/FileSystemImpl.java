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
        if (caminho == null || caminho.isEmpty() || usuario == null || usuario.isEmpty()) {
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

            // Verifica se o usuário tem permissão para criar o diretório
            Usuario usuarioObj = null;

            for (Usuario user : usuarios) {
                if (user.getNome().equals(usuario)) {
                    usuarioObj = user;
                }
            }

            if (usuarioObj == null) {
                new IllegalArgumentException("Usuário não encontrado: " + usuario);
            }

            if (!usuarioObj.getPermissoes().contains("w")) {
                try {
                    diretorioAtual.temPerm(usuario, "w");
                } catch (IllegalArgumentException e) {
                    throw new PermissaoException("Usuário não tem permissão para criar diretório: " + caminhoAtual);
                }
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
        if (caminho == null || caminho.isEmpty() || usuario == null || usuario.isEmpty() || usuarioAlvo == null || usuarioAlvo.isEmpty() || permissao == null || permissao.isEmpty()) {
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

        // Verifica se o usuário tem permissão para criar o diretório
        Usuario usuarioObj = null;

        for (Usuario user : usuarios) {
            if (user.getNome().equals(usuario)) {
                usuarioObj = user;
            }
        }

        if (usuarioObj == null) {
            new IllegalArgumentException("Usuário não encontrado: " + usuario);
        }

        if (usuarioObj.getPermissoes().contains("w")) {
            try {
                dir.temPerm(usuario, "w");
            } catch (IllegalArgumentException e) {
                throw new PermissaoException("Usuário não tem permissão para criar diretório: " + caminho);
            }
        } else {
            throw new PermissaoException("Usuário não tem permissão para criar diretório: " + caminho);
        }

        dir.setPermissoesUsuario(usuarioAlvo, permissao);
        System.out.println("Permissão alterada para " + usuarioAlvo + " em " + caminho + ": " + permissao);
    }

    @Override
    public void rm(String caminho, String usuario, boolean recursivo)
            throws CaminhoNaoEncontradoException, PermissaoException {

        if (caminho == null || caminho.isEmpty() || usuario == null || usuario.isEmpty()) {
            throw new IllegalArgumentException("Caminho e usuário não podem ser nulos");
        }
        

        caminho = caminho.replace("\\", "/");
        if (caminho.endsWith("/"))
            caminho = caminho.substring(0, caminho.length() - 1);

        Dir dir;

        try {
            dir = irPara(caminho);
        } catch (CaminhoNaoEncontradoException e) {
            // Tentativa de recuperar o arquivo pelo pai
            int ultimoBarra = caminho.lastIndexOf('/');
            if (ultimoBarra == -1)
                throw e;

            String caminhoPai = caminho.substring(0, ultimoBarra);
            String nomeArquivo = caminho.substring(ultimoBarra + 1);

            Dir pai = irPara(caminhoPai);
            Dir possivelArquivo = pai.getFilhos().get(nomeArquivo);

            if (possivelArquivo != null && possivelArquivo.isArquivo()) {
                dir = possivelArquivo;
            } else {
                throw new CaminhoNaoEncontradoException("Arquivo ou diretório não encontrado: " + caminho);
            }
        }

        // Busca o objeto usuário
        Usuario usuarioObj = null;
        for (Usuario user : usuarios) {
            if (user.getNome().equals(usuario)) {
                usuarioObj = user;
                break;
            }
        }

        if (usuarioObj == null) {
            throw new IllegalArgumentException("Usuário não encontrado: " + usuario);
        }

        // Verifica permissão de escrita
        if (usuarioObj.getPermissoes().contains("w")) {
            try {
                dir.temPerm(usuario, "w");
            } catch (IllegalArgumentException e) {
                throw new PermissaoException("Usuário não tem permissão para remover: " + dir.getNome());
            }
        } else {
            throw new PermissaoException("Usuário não tem permissão para remover: " + dir.getNome());
        }

        // Verifica se pode remover diretório com conteúdo
        if (!dir.isArquivo() && dir.temSubdiretorios() && !recursivo) {
            throw new PermissaoException(
                    "Esse diretório contém subdiretórios. Use o parâmetro recursivo para removê-lo.");
        }

        // Remoção
        if (dir.isArquivo()) {
            dir.getPai().removeFilho(dir.getNome());
            System.out.println("Arquivo removido: " + caminho);
        } else {
            if (recursivo) {
                for (Dir filho : new ArrayList<>(dir.getFilhos().values())) {
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
        if (caminho == null || caminho.isEmpty() || usuario == null || usuario.isEmpty()) {
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

        Usuario usuarioObj = null;

        for (Usuario user : usuarios) {
            if (user.getNome().equals(usuario)) {
                usuarioObj = user;
            }
        }

        if (usuarioObj == null) {
            new IllegalArgumentException("Usuário não encontrado: " + usuario);
        }

        if (usuarioObj.getPermissoes().contains("w")) {
            try {
                dirPai.temPerm(usuario, "w");
            } catch (IllegalArgumentException e) {
                throw new PermissaoException("Usuário não tem permissão para criar arquivo: " + caminho);
            }
        } else {
            throw new PermissaoException("Usuário não tem permissão para criar arquivo: " + caminho);
        }

        File novoArquivo = new File(nomeArquivo, usuario, "rwx");
        dirPai.addFilho(novoArquivo);
        System.out.println("Arquivo criado: " + caminho);
    }

    @Override
    public void write(String caminho, String usuario, boolean anexar, byte[] buffer)
            throws CaminhoNaoEncontradoException, PermissaoException {
        if (caminho == null || usuario == null || buffer == null || caminho.isEmpty() || usuario.isEmpty() || buffer.length == 0) {
            throw new IllegalArgumentException("Caminho, usuário e buffer não podem ser nulos");
        }

        caminho = caminho.replace("\\", "/");
        if (caminho.endsWith("/")) {
            caminho = caminho.substring(0, caminho.length() - 1);
        }

        Dir diretorio = irPara(caminho);

        if (diretorio == null) {
            throw new CaminhoNaoEncontradoException("Caminho não encontrado: " + caminho);
        }

        if (!diretorio.isArquivo()) {
            throw new IllegalArgumentException("O caminho especificado não é um arquivo: " + caminho);
        }

        Usuario usuarioObj = null;

        for (Usuario user : usuarios) {
            if (user.getNome().equals(usuario)) {
                usuarioObj = user;
            }
        }

        if (usuarioObj == null) {
            new IllegalArgumentException("Usuário não encontrado: " + usuario);
        }

        if (usuarioObj.getPermissoes().contains("w")) {
            try {
                diretorio.temPerm(usuario, "w");
            } catch (IllegalArgumentException e) {
                throw new PermissaoException("Usuário não tem permissão para criar diretório: " + caminho);
            }
        } else {
            throw new PermissaoException("Usuário não tem permissão para criar diretório: " + caminho);
        }

        File arquivo = (File) diretorio;
        if (!anexar)
            arquivo.limparBlocos();

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
        if (caminho == null || usuario == null || buffer == null || caminho.isEmpty() || usuario.isEmpty() || buffer.length == 0) {
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

        Usuario usuarioObj = null;

        for (Usuario user : usuarios) {
            if (user.getNome().equals(usuario)) {
                usuarioObj = user;
            }
        }

        if (usuarioObj == null) {
            new IllegalArgumentException("Usuário não encontrado: " + usuario);
        }

        if (usuarioObj.getPermissoes().contains("r")) {
            try {
                diretorio.temPerm(usuario, "r");
            } catch (IllegalArgumentException e) {
                throw new PermissaoException("Usuário não tem permissão para criar diretório: " + caminho);
            }
        } else {
            throw new PermissaoException("Usuário não tem permissão para criar diretório: " + caminho);
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
        if (caminhoAntigo == null || caminhoNovo == null || usuario == null || caminhoAntigo.isEmpty() || caminhoNovo.isEmpty() || usuario.isEmpty()) {
            throw new IllegalArgumentException("Caminho antigo, caminho novo e usuário não podem ser nulos");
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
            throw new IllegalArgumentException("Caminho antigo e caminho novo não podem ser iguais");
        }

        Dir dirantigo = irPara(caminhoAntigo);

        Usuario usuarioObj = usuarios.stream()
                .filter(u -> u.getNome().equals(usuario))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + usuario));

        if (usuarioObj.getPermissoes().contains("r")) {
            try {
                dirantigo.temPerm(usuario, "r");
            } catch (IllegalArgumentException e) {
                throw new PermissaoException("Usuário não tem permissão para mover: " + caminhoAntigo);
            }
        } else {
            throw new PermissaoException("Usuário não tem permissão para mover: " + caminhoAntigo);
        }

        try {
            Dir destino = irPara(caminhoNovo);

            if (usuarioObj.getPermissoes().contains("w")) {
                try {
                    destino.temPerm(usuario, "w");
                } catch (IllegalArgumentException e) {
                    throw new PermissaoException("Sem permissão de escrita no destino");
                }
            } else {
                throw new PermissaoException("Sem permissão de escrita no destino");
            }

            if (destino.getFilhos().containsKey(dirantigo.getNome())) {
                throw new IllegalArgumentException("Já existe um diretório ou arquivo com esse nome no destino");
            }

            dirantigo.getPai().removeFilho(dirantigo.getNome());
            destino.addFilho(dirantigo);
        } catch (CaminhoNaoEncontradoException e) {
            int idx = caminhoNovo.lastIndexOf('/');
            String novoNome = caminhoNovo.substring(idx + 1);
            String caminhoPaiNovo = (idx <= 0) ? "/" : caminhoNovo.substring(0, idx);
            Dir novoPai = irPara(caminhoPaiNovo);

            if (usuarioObj.getPermissoes().contains("w")) {
                try {
                    novoPai.temPerm(usuario, "w");
                } catch (IllegalArgumentException ex) {
                    throw new PermissaoException("Sem permissão de escrita no novo caminho");
                }
            } else {
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

        // Verifica se o usuário tem permissão para criar o diretório
        Usuario usuarioObj = null;

        for (Usuario user : usuarios) {
            if (user.getNome().equals(usuario)) {
                usuarioObj = user;
            }
        }

        if (usuarioObj == null) {
            new IllegalArgumentException("Usuário não encontrado: " + usuario);
        }

        if (usuarioObj.getPermissoes().contains("r")) {
            try {
                diretorio.temPerm(usuario, "r");
            } catch (IllegalArgumentException e) {
                throw new PermissaoException("Você não tem permissão para listar este diretório!");
            }
        } else {
            throw new PermissaoException("Você não tem permissão para listar este diretório!");
        }

        String output = lsRecursivo(diretorio, caminho, recursivo, usuario);
        System.out.print(output);
    }

    @Override
    public void cp(String caminhoOrigem, String caminhoDestino, String usuario, boolean recursivo)
            throws CaminhoNaoEncontradoException, PermissaoException {
        if (caminhoOrigem == null || caminhoDestino == null || usuario == null || caminhoOrigem.isEmpty() || caminhoDestino.isEmpty() || usuario.isEmpty()) {
            throw new IllegalArgumentException("Caminho de origem, destino e usuário não podem ser nulos");
        }

        caminhoOrigem = caminhoOrigem.replace("\\", "/").replaceAll("/$", "");
        caminhoDestino = caminhoDestino.replace("\\", "/").replaceAll("/$", "");

        Dir origem = irPara(caminhoOrigem);
        Dir destino = irPara(caminhoDestino);

        Usuario usuarioObj = usuarios.stream()
                .filter(u -> u.getNome().equals(usuario))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + usuario));

        if (usuarioObj.getPermissoes().contains("rw")) {
            try {
                origem.temPerm(usuario, "r");
            } catch (IllegalArgumentException e) {
                throw new PermissaoException("Sem permissão de leitura em: " + caminhoOrigem);
            }
            try {
                origem.temPerm(usuario, "w");
            } catch (IllegalArgumentException e) {
                throw new PermissaoException("Sem permissão de escrita em: " + caminhoDestino);
            }
        } else {
            throw new PermissaoException("Usuário não tem permissão para copiar: " + caminhoOrigem);
        }

        String nomeOrigem = origem.getNome();

        // Se destino for um arquivo, substituí-lo se possível
        if (destino instanceof File) {
            if (!origem.isArquivo()) {
                throw new IllegalArgumentException("Não é possível copiar um diretório para um arquivo");
            }
            copiarConteudoArquivo((File) origem, (File) destino, usuario);
            System.out.println("Arquivo sobrescrito: " + destino.getCaminhoCompleto());
            return;
        }

        // Se for diretório, verificar se já existe um filho com o mesmo nome
        Dir existente = destino.getFilhos().get(nomeOrigem);

        if (existente != null) {
            // Se for arquivo, sobrescreve
            if (existente.isArquivo() && origem.isArquivo()) {
                copiarConteudoArquivo((File) origem, (File) existente, usuario);
                System.out.println("Arquivo sobrescrito: " + existente.getCaminhoCompleto());
                return;
            } else {
                throw new PermissaoException(
                        "Já existe um diretório/arquivo com esse nome: " + existente.getCaminhoCompleto());
            }
        }

        if (origem.isArquivo()) {
            File arquivoOrigem = (File) origem;
            File arquivoNovo = new File(arquivoOrigem.getNome(), usuario, "rwx");
            for (BlocoDeDados bloco : arquivoOrigem.getBlocos()) {
                // Copia física dos blocos
                byte[] dadosCopiados = bloco.getDados().clone(); // copia conteúdo
                arquivoNovo.addBloco(new BlocoDeDados(dadosCopiados));
            }
            destino.addFilho(arquivoNovo);
            System.out.println("Arquivo copiado: " + arquivoNovo.getCaminhoCompleto());
        } else {
            if (!recursivo) {
                throw new IllegalArgumentException("Cópia de diretório requer o modo recursivo");
            }

            Dir novoDir = new Dir(origem.getNome(), usuario, "rwx");
            destino.addFilho(novoDir);
            for (Dir filho : origem.getFilhos().values()) {
                cp(filho.getCaminhoCompleto(), novoDir.getCaminhoCompleto(), usuario, true);
            }
            System.out.println("Diretório copiado: " + novoDir.getCaminhoCompleto());
        }
    }

    // Método auxiliar
    private void copiarConteudoArquivo(File origem, File destino, String usuario) {
        destino.limparBlocos();
        for (BlocoDeDados bloco : origem.getBlocos()) {
            byte[] dadosCopiados = bloco.getDados().clone();
            destino.addBloco(new BlocoDeDados(dadosCopiados));
        }
    }

    @Override
    public void addUser(Usuario usuario) {
        if (usuario == null || usuario.getNome() == null || usuario.getNome().isEmpty()
                || usuario.getPermissoes() == null || usuario.getPermissoes().isEmpty()) {
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