package filesys;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import exception.BlocoVazioException;
import exception.CaminhoJaExistenteException;
import exception.CaminhoNaoEncontradoException;
import exception.PermissaoException;

// Implemente nesta classe o seu código do FileSystem.
// A classe pode ser alterada.
// O construtor, argumentos do construtor podem ser modificados 
// e atributos & métodos privados podem ser adicionados
public final class FileSystemImpl implements IFileSystem {
    private static final String ROOT_USER = "root";
    private List<Usuario> usuarios = new ArrayList<>();
    private Diretorio raiz;
    private Map<String, Arquivo> arquivos = new HashMap<>();

    public FileSystemImpl() {
        raiz = new Diretorio("/", "rwx", ROOT_USER);
        adicionarUsuario(new Usuario(ROOT_USER, "rwx", "/"));
    }

    private void adicionarUsuario(Usuario user){
        for(Usuario usuario : usuarios){
            if(usuario.getNome().trim().equals(user.getNome()))
                throw new IllegalArgumentException("Não é possível existir dois usuários com mesmo nome");
        }
        usuarios.add(user);
    }

    private MetaDados navegar(String caminho) throws CaminhoNaoEncontradoException {
        if (caminho.equals("/")) return raiz;
        String[] partes = caminho.split("/");
        Diretorio temp = raiz;
        for (String parte : java.util.Arrays.stream(partes).filter(p -> p != null && !p.isEmpty()).toArray(String[]::new)) {
            if (!temp.getFilhos().containsKey(parte)) {
                throw new CaminhoNaoEncontradoException("Não foi possível encontrar o caminho: " + caminho);
            }
            temp = temp.getFilhos().get(parte);
        }
        return temp;
    }


    @Override
    public void mkdir(String caminho, String nome) throws CaminhoJaExistenteException, PermissaoException {
        if (caminho == null || nome == null || caminho.trim().isEmpty() || nome.trim().isEmpty()) {
        throw new IllegalArgumentException("Caminho e nome não podem ser nulos ou vazios");
    }

    
    String caminhoCompleto = caminho.equals("/") ? "/" + nome : caminho + "/" + nome;
    
    
    String[] partes = caminhoCompleto.split("/");
    String caminhoAtual = "";
    Diretorio diretorioAtual = raiz;
    String usuarioAtual = Thread.currentThread().getName(); 

    
    for (String parte : java.util.Arrays.stream(partes).filter(p -> p != null && !p.isEmpty()).toArray(String[]::new)) {
        
        caminhoAtual = caminhoAtual.equals("/") ? "/" + parte : caminhoAtual + "/" + parte;

       
        if (diretorioAtual.getFilhos().containsKey(parte)) {
            diretorioAtual = diretorioAtual.getFilhos().get(parte);
            continue;
        }

        
        if (!diretorioAtual.temPermissao(usuarioAtual, 'w')) {
            throw new PermissaoException("Usuário '" + usuarioAtual + "' não tem permissão de escrita em: " + caminhoAtual);
        }

        
        Diretorio novoDir = new Diretorio(parte, "rwx", usuarioAtual);
        diretorioAtual.addFilho(novoDir);
        diretorioAtual = novoDir;
    }
    }

    /*
     * Validar a permissão de acordo com as possíveis formas de permissão 'r w x' ou 'n',
     * caso seja nulo, vazio ou não tenha nenhuma dessas letras, lança exceção.
     * Ao ser validado, chama o método navegar
     */
    @Override
    public void chmod(String caminho, String usuario, String usuarioAlvo, String permissao)
            throws CaminhoNaoEncontradoException, PermissaoException {

        validarPermissao(permissao);

        MetaDados objAlvo = navegar(caminho);
        String dono = objAlvo.getDono();

        if (!usuario.equals("root") && !usuario.equals(dono)) {
            throw new PermissaoException(
                    "Usuário '" + usuario + "' não tem permissão para alterar direitos em: " + caminho);
        }

        objAlvo.alterarPermissao(usuarioAlvo, permissao);
    }

    @Override
    public void rm(String caminho, String usuario, boolean recursivo)
            throws CaminhoNaoEncontradoException, PermissaoException {
        throw new UnsupportedOperationException("Método não implementado 'rm'");
    }

    @Override
    public void touch(String caminho, String usuario) throws CaminhoJaExistenteException, PermissaoException {
        throw new UnsupportedOperationException("Método não implementado 'touch'");
    }

    @Override
    public void write(String caminho, String usuario, boolean anexar, byte[] buffer)
            throws CaminhoNaoEncontradoException, PermissaoException {
        if (buffer == null) {
            throw new IllegalArgumentException("Buffer não pode ser nulo");
        }

        // Obter o diretório pai e nome do arquivo
        String diretorioPai = obterDiretorioPai(caminho);
        String nomeArquivo = obterNomeArquivo(caminho);
        
        // Verificar se o arquivo já existe no nosso mapa de arquivos
        if (arquivos.containsKey(caminho)) {
            Arquivo arquivo = arquivos.get(caminho);
            
           if (!usuario.equals(ROOT_USER) && !arquivo.getDono().equals(usuario)) {
    char permissaoRequerida = 'w';
    String permissoes = arquivo.getPermissoesBasicas();
    if (permissoes.indexOf(permissaoRequerida) == -1) {
        throw new PermissaoException("Usuário: " + usuario + " não tem permissão de escrita em: " + caminho);
    }
}
            
            // Se não estiver anexando, limpa o arquivo
            if (!anexar) {
                try {
                    arquivo.limparArquivo();
                } catch (BlocoVazioException e) {
                    // O arquivo já está vazio, pode prosseguir
                }
            }
            
            // Divide o buffer em blocos e escreve
            final int TAMANHO_BLOCO = 4096;
            
            for (int i = 0; i < buffer.length; i += TAMANHO_BLOCO) {
                int tamanhoAtual = Math.min(TAMANHO_BLOCO, buffer.length - i);
                byte[] dadosBloco = new byte[tamanhoAtual];
                System.arraycopy(buffer, i, dadosBloco, 0, tamanhoAtual);
                
                Bloco bloco = new Bloco();
                bloco.setBytes(dadosBloco);
                arquivo.adicionarBloco(bloco);
            }
        } else {
            // O arquivo não existe, precisa criar
            try {
                // Verifica se o diretório pai existe
                MetaDados diretorio = navegar(diretorioPai);
                
                if (!(diretorio instanceof Diretorio)) {
                    throw new IllegalArgumentException("O caminho pai não é um diretório: " + diretorioPai);
                }
                
                Diretorio dir = (Diretorio) diretorio;
                
                // Verifica permissão para criar no diretório
                if (!dir.getDono().equals(usuario) && !usuario.equals(ROOT_USER)) {
                    if (!dir.temPermissao(usuario, 'w')) {
                        throw new PermissaoException("Usuário: " + usuario + " não tem permissão de escrita em: " + caminho);
                    }
                }
                
                // Cria o arquivo e adiciona ao nosso mapa
                List<Bloco> blocos = new ArrayList<>();
                Arquivo novoArquivo = new Arquivo(nomeArquivo, "rw-", usuario, blocos, 0);
                
                // Adiciona ao mapa de arquivos por caminho completo
                arquivos.put(caminho, novoArquivo);
                
                // Divide o buffer em blocos e escreve
                final int TAMANHO_BLOCO = 4096;
                
                for (int i = 0; i < buffer.length; i += TAMANHO_BLOCO) {
                    int tamanhoAtual = Math.min(TAMANHO_BLOCO, buffer.length - i);
                    byte[] dadosBloco = new byte[tamanhoAtual];
                    System.arraycopy(buffer, i, dadosBloco, 0, tamanhoAtual);
                    
                    Bloco bloco = new Bloco();
                    bloco.setBytes(dadosBloco);
                    novoArquivo.adicionarBloco(bloco);
                }
                
            } catch (CaminhoNaoEncontradoException ex) {
                throw new CaminhoNaoEncontradoException("Diretório pai não encontrado: " + diretorioPai);
            }
        }
    }

      @Override
    public void read(String caminho, String usuario, byte[] buffer)
            throws CaminhoNaoEncontradoException, PermissaoException {
        if (buffer == null) {
            throw new IllegalArgumentException("Buffer não pode ser nulo");
        }
        
        if (!arquivos.containsKey(caminho)) {
            throw new CaminhoNaoEncontradoException("Arquivo não encontrado: " + caminho);
        }
        
        Arquivo arquivo = arquivos.get(caminho);
        
        if (!usuario.equals(ROOT_USER) && !arquivo.getDono().equals(usuario)) {
    char permissaoRequerida = 'w';
    String permissoes = arquivo.getPermissoesBasicas();
    if (permissoes.indexOf(permissaoRequerida) == -1) {
        throw new PermissaoException("Usuário: " + usuario + " não tem permissão de escrita em: " + caminho);
    }
}
        
        // Lê todos os blocos do arquivo
        List<Bloco> blocos = arquivo.getAllBlocos();
        int offset = 0;
        
        for (Bloco bloco : blocos) {
            byte[] dados = bloco.getDados();
            int tamanhoACopiar = Math.min(dados.length, buffer.length - offset);
            
            if (tamanhoACopiar <= 0) break; // Buffer cheio
            
            // Copia os dados do bloco para o buffer
            System.arraycopy(dados, 0, buffer, offset, tamanhoACopiar);
            offset += tamanhoACopiar;
        }
    }

    @Override
    public void mv(String caminhoAntigo, String caminhoNovo, String usuario)
            throws CaminhoNaoEncontradoException, PermissaoException {
        throw new UnsupportedOperationException("Método não implementado 'mv'");
    }

    @Override
    public void ls(String caminho, String usuario, boolean recursivo) throws CaminhoNaoEncontradoException, PermissaoException {
        throw new UnsupportedOperationException("Método não implementado 'ls'");
    }

    @Override
    public void cp(String caminhoOrigem, String caminhoDestino, String usuario, boolean recursivo)
            throws CaminhoNaoEncontradoException, PermissaoException {
        throw new UnsupportedOperationException("Método não implementado 'cp'");
    }

    public void addUser(String user) {
        throw new UnsupportedOperationException("Método não implementado 'addUser'");
    }

    private void validarPermissao(String permissao) {
        if (permissao == null || permissao.length() != 3)
            throw new IllegalArgumentException("Permissão inválida");
        for (char c : permissao.toCharArray()) {
            if ("rwxn".indexOf(c) == -1)
                throw new IllegalArgumentException("Permissão inválida");
        }
    }

      private String obterDiretorioPai(String caminho) {
        if (caminho.equals("/")) return "/";
        
        int ultimaBarra = caminho.lastIndexOf('/');
        if (ultimaBarra == 0) return "/";
        return caminho.substring(0, ultimaBarra);
    }

    private String obterNomeArquivo(String caminho) {
        if (caminho.equals("/")) return "";
        
        int ultimaBarra = caminho.lastIndexOf('/');
        return caminho.substring(ultimaBarra + 1);
    }

}
