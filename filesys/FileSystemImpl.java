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
            if (parte == null || parte.isEmpty()) continue;
            if (!diretorioAtual.getFilhos().containsKey(parte)) {
                throw new CaminhoNaoEncontradoException("Caminho não encontrado: " + caminho);
            }
            diretorioAtual = diretorioAtual.getFilhos().get(parte);
        }

        return diretorioAtual;
    }

    // Lista o conteúdo de um diretório e, se recursivo=true, lista também os subdiretórios
    private String lsRecursivo(Dir diretorio, String caminho, boolean recursivo, String usuario) {
        StringBuilder saida = new StringBuilder();
        String nomeCaminho = (caminho == null || caminho.isEmpty() || !caminho.startsWith("/")) ? "/" + caminho : caminho;
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
    public void chmod(String caminho, String usuario, String usuarioAlvo, String permissao) throws CaminhoNaoEncontradoException, PermissaoException {
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
    public void rm(String caminho, String usuario, boolean recursivo) throws CaminhoNaoEncontradoException, PermissaoException {
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
        if(dir.temSubdiretorios() && !recursivo) {
            throw new PermissaoException("Esse diretório contém subdiretórios. Use o parâmetro recursivo para removê-lo.");
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
    public void touch(String caminho, String usuario) throws CaminhoJaExistenteException, PermissaoException, CaminhoNaoEncontradoException {
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
        if (caminhoAntigo == null || caminhoNovo == null || usuario == null) {
            throw new IllegalArgumentException("Caminho antigo, caminho novo e usuário não podem ser nulos");
        }
        Dir dirAntigo = irPara(caminhoAntigo);
        Dir dirNovo = irPara(caminhoNovo);
        if (!dirAntigo.temPerm(usuario, "w")) {
            throw new PermissaoException("Usuário não tem permissão para mover: " + caminhoAntigo);
        }
        if (!dirNovo.temPerm(usuario, "w")) {
            throw new PermissaoException("Usuário não tem permissão para mover para: " + caminhoNovo);
        }
        
        /*
         if( dirNovo.getFilhos().containsKey(dirAntigo.getNome())) {
             throw new CaminhoJaExistenteException("Já existe um arquivo ou diretório com o mesmo nome em: " + caminhoNovo);
         }
         */
        
        dirNovo.addFilho(dirAntigo);
        dirAntigo.getPai().removeFilho(dirAntigo.getNome());
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
        throw new UnsupportedOperationException("Método não implementado 'cp'");
    }

    public void addUser(String user) {
        throw new UnsupportedOperationException("Método não implementado 'addUser'");
    }
}
