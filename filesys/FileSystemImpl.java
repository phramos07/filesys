package filesys;

import java.util.Set;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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
    private static final String ROOT_DIR = "/"; // diretório raiz

    public Map<String, Map<String, String>> permissoes;

    // Função para adicionar meu usuário
    private final Set<String> usuarios = new HashSet<>();

    // Guarda se é arquivo ou diretório
    private Map<String, Boolean> isArquivo;

    // Guarda conteúdo dos arquivos
    private Map<String, byte[]> conteudoArquivos;

    //public FileSystemImpl(Map<String, Map<String, String>> permissoes) {
        //this.permissoes = permissoes;
        // Inicialização dos diretórios, arquivos, etc.
    //}

    public FileSystemImpl(Map<String, Map<String, String>> permissoes) {
    this.permissoes = permissoes;
    this.isArquivo = new HashMap<>();
    this.conteudoArquivos = new HashMap<>();

    // Preenche usuários a partir das chaves do mapa permissoes
    if (permissoes != null) {
        for (Map<String, String> usuariosPerms : permissoes.values()) {
            for (String usuario : usuariosPerms.keySet()) {
                if (usuario != null && !usuario.trim().isEmpty()) {
                    this.usuarios.add(usuario);
                }
            }
        }
    }

    // Garante que root existe e tem permissão total para raiz
    if (!usuarios.contains(ROOT_USER)) {
        usuarios.add(ROOT_USER);
    }

    permissoes.putIfAbsent(ROOT_DIR, new HashMap<>());
    permissoes.get(ROOT_DIR).put(ROOT_USER, "rwx");
    isArquivo.put(ROOT_DIR, false); // raiz é diretório
}

    public FileSystemImpl() {

        this.permissoes = new HashMap<>();
        this.isArquivo = new HashMap<>();
        this.conteudoArquivos = new HashMap<>();

        // Inicialização de diretórios, etc.

        // Adicionador root como usuário
        addUser(ROOT_USER);

        // Inicializar raiz com permissão total para root
        Map<String, String> rootPerms = new HashMap<>();
        rootPerms.put(ROOT_USER, "rwx");
        permissoes.put("/", rootPerms);
        isArquivo.put("/", false); // raiz é diretório

    }

    // Função para adicionar meu usuário

    public void addUser(String usuario) {
        if (usuario != null && !usuario.trim().isEmpty()) {
            usuarios.add(usuario);
        }
    }


    private boolean usuarioExiste(String usuario) {
        return usuarios.contains(usuario);
    }

    // Getter às permissões
    public Map<String, Map<String, String>> getPermissoes() {
        return permissoes;
    }

    // Listar os usuários
    public Set<String> getUsuarios() {
        return Collections.unmodifiableSet(usuarios);
    }

    private boolean temPermissao(String caminho, String usuario, char tipoPermissao) {
        if (ROOT_USER.equals(usuario)) return true;

        // Caminho atual (parte mais específica)
        String caminhoAtual = caminho;

        while (caminhoAtual != null && !caminhoAtual.isEmpty()) {
            Map<String, String> mapUsuarioPerm = permissoes.get(caminhoAtual);
            if (mapUsuarioPerm != null && mapUsuarioPerm.containsKey(usuario)) {
                String perm = mapUsuarioPerm.get(usuario);
                return perm.indexOf(tipoPermissao) != -1;
            }

            // Se chegou na raiz, para
            if (caminhoAtual.equals("/")) break;

            // Sobe um nível no caminho
            int lastSlash = caminhoAtual.lastIndexOf('/');
            caminhoAtual = lastSlash > 0 ? caminhoAtual.substring(0, lastSlash) : "/";
        }

        // Se não encontrou permissão exata, procura se tem permissão de /** no "/"
        Map<String, String> raizPerms = permissoes.get("/");
        if (raizPerms != null && raizPerms.containsKey(usuario)) {
            String perm = raizPerms.get(usuario);
            return perm.indexOf(tipoPermissao) != -1;
        }

        return false;
    }

    @Override
    public void mkdir(String caminho, String usuario) throws CaminhoJaExistenteException, PermissaoException {

        if (!usuarioExiste(usuario)) {
            throw new PermissaoException("Usuário não fornecido ou inexistente.");
        }

        if (caminho == null || caminho.isEmpty() || !caminho.startsWith("/")) {
            throw new IllegalArgumentException("Caminho inválido.");
        }

        if (permissoes.containsKey(caminho)) {
            throw new CaminhoJaExistenteException("O diretório já existe: " + caminho);
        }

        int lastSlashIndex = caminho.lastIndexOf('/');
        String caminhoPai = (lastSlashIndex == 0) ? "/" : caminho.substring(0, lastSlashIndex);

        if (!permissoes.containsKey(caminhoPai)) {
            throw new PermissaoException("Diretório pai inexistente: " + caminhoPai);
        }

        Map<String, String> permsPai = permissoes.get(caminhoPai);
        String permUsuario = ROOT_USER.equals(usuario) ? "rwx" : permsPai.get(usuario);
        if (permUsuario == null || !permUsuario.contains("w")) {
            throw new PermissaoException("Usuário não tem permissão de escrita no diretório pai.");
        }

        Map<String, String> novasPerms = new HashMap<>();
        novasPerms.put(usuario, "rwx");
        novasPerms.put(ROOT_USER, "rwx");

        permissoes.put(caminho, novasPerms);
        isArquivo.put(caminho, false); // <-- ESSENCIAL PARA O MÉTODO rm FUNCIONAR

        System.out.println("Diretório criado com sucesso: " + caminho);
}

    @Override
    public void chmod(String caminho, String usuario, String usuarioAlvo, String permissao)
            throws CaminhoNaoEncontradoException, PermissaoException {
        // throw new UnsupportedOperationException("Método não implementado 'chmod'");

        // Verifica se o caminho existe (permissoes contem o caminho)
        if (!permissoes.containsKey(caminho)) {
            throw new CaminhoNaoEncontradoException("Caminho não encontrado: " + caminho);
        }

        // Verfica se o usuário que está tentando alterar é root ou tem permissão rwx
        if (!ROOT_USER.equals(usuario)) {

            Map<String, String> permissoesDoCaminho = permissoes.get(caminho);
            String permUsuario = permissoesDoCaminho.get(usuario);

            if (permUsuario == null || ! permUsuario.startsWith("rw")) {
                throw new PermissaoException("Usuário " + usuario + " não tem permissão para alterar permissões em " + caminho);
            }
        }

        // Verificar se o usuarioAlvo existe
        if (!usuarioExiste(usuarioAlvo)) {
            throw new PermissaoException("Usuário alvo não existe: " + usuarioAlvo);
        }

        // Atualiza a permissão para o usuarioAlvo no caminho
        Map<String, String> permissoesDoCaminho = permissoes.get(caminho);
        permissoesDoCaminho.put(usuarioAlvo, permissao);

        // imprimir a mudança de permissão
        System.out.println("Permissão para " + usuarioAlvo + " no caminho " + caminho + " alterada para " + permissao);

    }

    @Override
    public void rm(String caminho, String usuario, boolean recursivo) throws CaminhoNaoEncontradoException, PermissaoException {
        if (!usuarioExiste(usuario)) {
            throw new PermissaoException("Usuário não fornecido ou inexistente.");
        }

        if (!permissoes.containsKey(caminho)) {
            throw new CaminhoNaoEncontradoException("Caminho não encontrado: " + caminho);
        }

        Map<String, String> perms = permissoes.get(caminho);
        String permUsuario = ROOT_USER.equals(usuario) ? "rwx" : perms.get(usuario);
        if (permUsuario == null || !permUsuario.contains("w")) {
            throw new PermissaoException("Usuário não tem permissão de escrita para remover: " + caminho);
        }

        // Se for diretório e não recursivo, deve falhar se tiver conteúdo
        if (!isArquivo.get(caminho) && !recursivo) {
            // Verifica se existe algum caminho que começa com esse caminho + "/"
            for (String c : permissoes.keySet()) {
                if (!c.equals(caminho) && c.startsWith(caminho.endsWith("/") ? caminho : caminho + "/")) {
                    throw new PermissaoException("Diretório não está vazio e recursivo não foi especificado.");
                }
            }
        }

        // Remove recursivamente todos os caminhos filhos
        permissoes.keySet().removeIf(c -> c.equals(caminho) || c.startsWith(caminho.endsWith("/") ? caminho : caminho + "/"));
        isArquivo.keySet().removeIf(c -> c.equals(caminho) || c.startsWith(caminho.endsWith("/") ? caminho : caminho + "/"));
        conteudoArquivos.keySet().removeIf(c -> c.equals(caminho) || c.startsWith(caminho.endsWith("/") ? caminho : caminho + "/"));

        System.out.println("Removido: " + caminho);
    }

    @Override
    public void touch(String caminho, String usuario) throws CaminhoJaExistenteException, PermissaoException {
        if (!usuarioExiste(usuario)) {
            throw new PermissaoException("Usuário não fornecido ou inexistente.");
        }

        if (caminho == null || caminho.isEmpty() || !caminho.startsWith("/")) {
            throw new IllegalArgumentException("Caminho inválido.");
        }

        if (permissoes.containsKey(caminho)) {
            throw new CaminhoJaExistenteException("O arquivo já existe: " + caminho);
        }

        int lastSlashIndex = caminho.lastIndexOf('/');
        String caminhoPai = (lastSlashIndex == 0) ? "/" : caminho.substring(0, lastSlashIndex);

        if (!permissoes.containsKey(caminhoPai)) {
            throw new PermissaoException("Diretório pai inexistente: " + caminhoPai);
        }

        Map<String, String> permsPai = permissoes.get(caminhoPai);
        String permUsuario = ROOT_USER.equals(usuario) ? "rwx" : permsPai.get(usuario);
        if (permUsuario == null || !permUsuario.contains("w")) {
            throw new PermissaoException("Usuário não tem permissão de escrita no diretório pai.");
        }

        Map<String, String> novasPerms = new HashMap<>();
        novasPerms.put(usuario, "rw");  // Para arquivos, geralmente "rw"
        novasPerms.put(ROOT_USER, "rw");

        permissoes.put(caminho, novasPerms);
        isArquivo.put(caminho, true);
        conteudoArquivos.put(caminho, new byte[0]); // Arquivo vazio

        System.out.println("Arquivo criado com sucesso: " + caminho);
    }


    @Override
    public void write(String caminho, String usuario, boolean anexar, byte[] buffer)
            throws CaminhoNaoEncontradoException, PermissaoException {
        if (!usuarioExiste(usuario)) {
            throw new PermissaoException("Usuário não fornecido ou inexistente.");
        }
        if (!permissoes.containsKey(caminho)) {
            throw new CaminhoNaoEncontradoException("Caminho não encontrado: " + caminho);
        }
        if (!Boolean.TRUE.equals(isArquivo.get(caminho))) {
            throw new PermissaoException("O caminho não é um arquivo: " + caminho);
        }
        Map<String, String> perms = permissoes.get(caminho);
        String permUsuario = ROOT_USER.equals(usuario) ? "rw" : perms.get(usuario);
        if (permUsuario == null || !permUsuario.contains("w")) {
            throw new PermissaoException("Usuário não tem permissão para escrever no arquivo: " + caminho);
        }

        // Bloco de escrita
        final int BLOCK_SIZE = 256;
        byte[] conteudoAtual = conteudoArquivos.get(caminho);
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        try {
            if (anexar) {
                output.write(conteudoAtual);
            }

            int offset = 0;
            while (offset < buffer.length) {
                int blockLen = Math.min(BLOCK_SIZE, buffer.length - offset);
                output.write(buffer, offset, blockLen);
                offset += blockLen;
            }

            conteudoArquivos.put(caminho, output.toByteArray());
            System.out.println("Escrita por blocos concluída: " + caminho);
        } catch (IOException e) {
            e.printStackTrace(); // Não deve ocorrer em ByteArrayOutputStream
        }
    }

    @Override
    public void read(String caminho, String usuario, byte[] buffer)
            throws CaminhoNaoEncontradoException, PermissaoException {
        if (!usuarioExiste(usuario)) {
            throw new PermissaoException("Usuário não fornecido ou inexistente.");
        }
        if (!permissoes.containsKey(caminho)) {
            throw new CaminhoNaoEncontradoException("Caminho não encontrado: " + caminho);
        }
        if (!Boolean.TRUE.equals(isArquivo.get(caminho))) {
            throw new PermissaoException("O caminho não é um arquivo: " + caminho);
        }

        Map<String, String> perms = permissoes.get(caminho);
        String permUsuario = ROOT_USER.equals(usuario) ? "rw" : perms.get(usuario);
        if (permUsuario == null || !permUsuario.contains("r")) {
            throw new PermissaoException("Usuário não tem permissão de leitura no arquivo: " + caminho);
        }

        byte[] conteudo = conteudoArquivos.get(caminho);

        // Leitura em blocos
        final int BLOCK_SIZE = 256;
        int offset = 0;
        while (offset < conteudo.length && offset < buffer.length) {
            int blockLen = Math.min(BLOCK_SIZE, conteudo.length - offset);
            System.arraycopy(conteudo, offset, buffer, offset, blockLen);
            offset += blockLen;
        }

        String conteudoStr = new String(buffer).trim();
        System.out.println("Conteúdo lido por blocos:");
        System.out.println(conteudoStr);
    }

    @Override
    public void mv(String caminhoAntigo, String caminhoNovo, String usuario)
            throws CaminhoNaoEncontradoException, PermissaoException {

        if (!usuarioExiste(usuario)) {
            throw new PermissaoException("Usuário não fornecido ou inexistente.");
        }
        if (!permissoes.containsKey(caminhoAntigo)) {
            throw new CaminhoNaoEncontradoException("Caminho antigo não encontrado: " + caminhoAntigo);
        }

        // Verificar permissão de escrita no diretório pai do caminhoAntigo
        int lastSlashOld = caminhoAntigo.lastIndexOf('/');
        String paiAntigo = (lastSlashOld == 0) ? "/" : caminhoAntigo.substring(0, lastSlashOld);
        Map<String, String> permsPaiAntigo = permissoes.get(paiAntigo);
        String permUsuarioAntigo = ROOT_USER.equals(usuario) ? "rwx" : permsPaiAntigo.get(usuario);
        if (permUsuarioAntigo == null || !permUsuarioAntigo.contains("w")) {
            throw new PermissaoException("Usuário não tem permissão de escrita no diretório pai do caminho antigo.");
        }

        // Se já existe como destino, não precisa verificar o pai
        if (!permissoes.containsKey(caminhoNovo)) {
            // Se não existe, verificar o pai
            int lastSlashNew = caminhoNovo.lastIndexOf('/');
            String paiNovo = (lastSlashNew == 0) ? "/" : caminhoNovo.substring(0, lastSlashNew);
            if (!permissoes.containsKey(paiNovo)) {
                throw new PermissaoException("Diretório pai do caminho novo não existe: " + paiNovo);
            }
            Map<String, String> permsPaiNovo = permissoes.get(paiNovo);
            String permUsuarioNovo = ROOT_USER.equals(usuario) ? "rwx" : permsPaiNovo.get(usuario);
            if (permUsuarioNovo == null || !permUsuarioNovo.contains("w")) {
                throw new PermissaoException("Usuário não tem permissão de escrita no diretório pai do caminho novo.");
            }
        }

        // Lista de caminhos que serão movidos (inclui caminhoAntigo e seus filhos)
        Set<String> caminhosAMover = new HashSet<>();
        String prefixoAntigo = caminhoAntigo.endsWith("/") ? caminhoAntigo : caminhoAntigo + "/";
        for (String c : permissoes.keySet()) {
            if (c.equals(caminhoAntigo) || c.startsWith(prefixoAntigo)) {
                caminhosAMover.add(c);
            }
        }

        // Mover: remover entradas antigas e criar novas com o prefixo atualizado
        for (String c : caminhosAMover) {
            String novoCaminho = caminhoNovo + c.substring(caminhoAntigo.length());

            // Permissões
            Map<String, String> p = permissoes.remove(c);
            permissoes.put(novoCaminho, p);

            // isArquivo
            Boolean isA = isArquivo.remove(c);
            isArquivo.put(novoCaminho, isA);

            // Conteúdo arquivos
            if (isA) {
                byte[] conteudo = conteudoArquivos.remove(c);
                conteudoArquivos.put(novoCaminho, conteudo);
            }
        }
    }

    @Override
    public void ls(String caminho, String usuario, boolean recursivo) throws CaminhoNaoEncontradoException, PermissaoException {
        if (!usuarioExiste(usuario)) {
            throw new PermissaoException("Usuário não fornecido ou inexistente.");
        }
        if (!permissoes.containsKey(caminho)) {
            throw new CaminhoNaoEncontradoException("Caminho não encontrado: " + caminho);
        }

        // Verifica permissão de leitura
        Map<String, String> perms = permissoes.get(caminho);
        String permUsuario = ROOT_USER.equals(usuario) ? "r" : perms.get(usuario);
        if (permUsuario == null || !permUsuario.contains("r")) {
            throw new PermissaoException("Usuário não tem permissão para listar: " + caminho);
        }

        for (String c : permissoes.keySet()) {
            if (c.equals(caminho)) continue;
            if (c.startsWith(caminho.endsWith("/") ? caminho : caminho + "/")) {
                if (!recursivo) {
                    // Lista só filhos diretos
                    String restante = c.substring(caminho.length());
                    if (restante.startsWith("/")) {
                        restante = restante.substring(1);
                    }
                    if (!restante.contains("/")) {
                        System.out.println(c);
                    }
                } else {
                    System.out.println(c);
                }
            }
        }
    }


    @Override
    public void cp(String caminhoOrigem, String caminhoDestino, String usuario, boolean recursivo)
            throws CaminhoNaoEncontradoException, PermissaoException {

        if (!usuarioExiste(usuario)) {
            throw new PermissaoException("Usuário não fornecido ou inexistente.");
        }
        if (!permissoes.containsKey(caminhoOrigem)) {
            throw new CaminhoNaoEncontradoException("Caminho origem não encontrado: " + caminhoOrigem);
        }

        // Verifica permissão leitura no caminhoOrigem
        Map<String, String> permsOrigem = permissoes.get(caminhoOrigem);
        String permUsuarioOrigem = ROOT_USER.equals(usuario) ? "rwx" : permsOrigem.get(usuario);
        if (permUsuarioOrigem == null || !permUsuarioOrigem.contains("r")) {
            throw new PermissaoException("Usuário não tem permissão para ler o caminho origem.");
        }

        // Verifica permissão escrita no diretório pai do destino
        if (!permissoes.containsKey(caminhoDestino)) {
            throw new PermissaoException("Diretório de destino não existe: " + caminhoDestino);
        }
        Map<String, String> permsDestino = permissoes.get(caminhoDestino);
        String permUsuarioDestino = ROOT_USER.equals(usuario) ? "rwx" : permsDestino.get(usuario);
        if (permUsuarioDestino == null || !permUsuarioDestino.contains("w")) {
            throw new PermissaoException("Usuário não tem permissão para escrever no destino.");
        }
        if (permUsuarioDestino == null || !permUsuarioDestino.contains("w")) {
            throw new PermissaoException("Usuário não tem permissão para escrever no diretório pai do destino.");
        }

        boolean origemArquivo = Boolean.TRUE.equals(isArquivo.get(caminhoOrigem));

        if (!origemArquivo && !recursivo) {
            throw new PermissaoException("Caminho origem e diretório recursivo não foi especificado.");
        }

        // Caminhos a copiar (origem + filhos se diretório)
        Set<String> caminhosACopiar = new HashSet<>();
        if (origemArquivo) {
            caminhosACopiar.add(caminhoOrigem);
        } else {
            String prefixoOrigem = caminhoOrigem.endsWith("/") ? caminhoOrigem : caminhoOrigem + "/";
            for (String c : permissoes.keySet()) {
                if (c.equals(caminhoOrigem) || c.startsWith(prefixoOrigem)) {
                    caminhosACopiar.add(c);
                }
            }
        }

        for (String c : caminhosACopiar) {
            String novoCaminho = caminhoDestino + c.substring(caminhoOrigem.length());

            // Clonar permissões
            Map<String, String> permsOrig = permissoes.get(c);
            Map<String, String> novaPerms = new HashMap<>(permsOrig);
            permissoes.put(novoCaminho, novaPerms);

            // Clonar isArquivo
            Boolean isA = isArquivo.get(c);
            isArquivo.put(novoCaminho, isA);

            // Clonar conteúdo se arquivo
            if (isA) {
                byte[] conteudo = conteudoArquivos.get(c);
                conteudoArquivos.put(novoCaminho, conteudo.clone());
            }
        }
    }

}


