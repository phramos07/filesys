package filesys;

import java.util.HashMap;
import java.util.Map;

public class Dir {
    // Atributos
    private String nome;
    private String dono;
    private String permissoes;

    // Relacionamentos
    protected Dir pai;
    protected Map<String, Dir> filhos;
    private Map<String, String> permissoesUsuarios;

    // Construtor
    public Dir(String nome, String dono, String permissoes) {
        this.nome = nome;
        this.dono = dono;
        this.permissoes = permissoes;

        this.pai = null; // Inicialmente, o diretório não tem pai
        this.filhos = new HashMap<>();
        this.permissoesUsuarios = new HashMap<>();

        // Define as permissões iniciais para o dono do diretório
        this.permissoesUsuarios.put(dono, "rwx"); // O dono tem permissão total
    }

    // Nome
    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }


    // Dono
    public String getDono() {
        return dono;
    }

    public void setDono(String dono) {
        this.dono = dono;
    }


    // Permissões
    public String getPermissoes() {
        return permissoes;
    }

    public void setPermissoes(String permissoes) {
        this.permissoes = permissoes;
    }


    // Pai
    public Dir getPai() {
        return pai;
    }

    public void setPai(Dir pai) {
        this.pai = pai;
    }


    // Filhos
    public Map<String, Dir> getFilhos() {
        return filhos;
    }

    public Dir getFilho(String nome) {
        return filhos.get(nome);
    }

    public void setFilhos(Map<String, Dir> filhos) {
        this.filhos = filhos;
        for (Dir filho : filhos.values()) {
            filho.setPai(this); // Define o pai de cada filho
        }
    }

    public void addFilho(Dir filho) {
        filhos.put(filho.getNome(), filho);
        filho.setPai(this); // Define o pai do filho
    }

    public void removeFilho(String nome) {
        filhos.remove(nome);
    }


    // Permissões dos usuários
    public Map<String, String> getPermissoesUsuarios() {
        return permissoesUsuarios;
    }

    public String getPermissoesUsuario(String usuario) {
        if ("root".equals(usuario)) return "rwx"; // O usuário root sempre tem permissão total

        // Verifica se o usuário é nulo ou vazio
        if (usuario == null || usuario.isEmpty()) {
            throw new IllegalArgumentException("Usuário não pode ser nulo ou vazio");
        }

        // Verifica se o usuário tem permissões definidas
        if (!permissoesUsuarios.containsKey(usuario)) {
            throw new IllegalArgumentException("Usuário não encontrado: " + usuario);
        }

        if (dono.equals(usuario)) return permissoes; // O dono do diretório tem as permissões do diretório

        return permissoesUsuarios.getOrDefault(usuario, "---"); // Retorna as permissões do usuário ou um padrão se não tiver
    }

    public void setPermissoesUsuario(String usuario, String permissoes) {
        if (permissoes == null || permissoes.length() != 3) {
            throw new IllegalArgumentException("As permissões precisam ter 3 caracteres");
        }

        this.permissoesUsuarios.put(usuario, permissoes);
    }


    // Verifica se o usuário tem permissão para acessar o diretório
    public boolean temPerm(String usuario, String permissao) {
        // Verifica se o usuário é nulo ou vazio
        if (usuario == null || usuario.isEmpty()) {
            throw new IllegalArgumentException("Usuário não pode ser nulo ou vazio");
        }

        if ("root".equals(usuario)) {
            return true; // O dono do diretório sempre tem permissão total
        }

        if (usuario.equals(dono)) {
            return true; // O dono do diretório sempre tem permissão total
        }

        // Verifica se a permissão é nula ou vazia
        if (permissao == null || permissao.isEmpty()) {
            throw new IllegalArgumentException("Permissão não pode ser nula ou vazia");
        }

        // Verifica se o usuário tem permissões definidas
        if (!permissoesUsuarios.containsKey(usuario)) {
            throw new IllegalArgumentException("Usuário não encontrado nas permissões: " + usuario);
        }

        String permissoesUsuario = getPermissoesUsuario(usuario);

        // Verifica se o usuário tem a permissão solicitada
        if (permissoesUsuario.contains(permissao)) {
            return true; // O usuário tem a permissão solicitada
        }

        // Se não tiver a permissão solicitada, verifica permissões do diretório pai
        if (pai != null) {
            return pai.temPerm(usuario, permissao); // Verifica no diretório pai
        }

        // Se não, o usuário não tem permissão
        return false;
    }


    // É um arquivo?
    public boolean isArquivo() {
        return false; // Esta classe representa um diretório, não um arquivo
    }


    // Transformando em string
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("  Diretório: ").append(nome).append("\n")
        .append("    - Dono: ").append(dono).append("\n")
        .append("    - Permissões: ").append(permissoes).append("\n")
        .append("    - Pai: ").append(pai != null ? pai.getNome() : "Nenhum").append("\n")
        .append("    - Filhos: ").append(getFilhos().isEmpty() ? "Nenhum" : getFilhos().keySet()).append("\n")
        .append("    - Permissões dos usuários: ").append(permissoesUsuarios).append("\n");
        return sb.toString();
    }
}
