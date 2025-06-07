package filesys;

import java.util.Map;

import exception.PermissaoException;

public class Diretorio {
    protected String nome;
    protected String dono;
    protected String permissoesA; //permissões rank A

    protected Diretorio pai;

    protected Map<String, Diretorio> filhos;
    protected Map<String, String> permissoesS; //permissões rank S

    /*
     * verifica se o usuário é root, caso sim, retorna todas as permissões em formato de string (rwx)
     * verifica se o usuário é dono, caso sim, retorna as respectivas permissoes
     * se nenhuma das condições for satisfeita, retorna permissoes de valor default: "nnn" (todas null)
     */
    public String getPermissoesDeUsuario(String usuario){
        if(usuario.equals("root"))
            return "rwx";
        else if(usuario.equals(dono))
            return permissoesA;
        return permissoesS.getOrDefault(usuario, "nnn");
    }
    
    /*
     * seta permissões para o usuário utilizando 3 tipos de caracteres: "w", "r" e "x"
     * caso as permissões forem diferentes de 3 lança exceção
     * para sempre ter 3 letras, caso não houver o usuário não tiver uma permissão, é utilizado o n (null)
     */
    public void setPermissoesUsuario(String usuario, String permissoes){
        if(permissoes == null || permissoes.length() != 3)
            throw new PermissaoException("Ocorreu um erro quanto as exceções");

        permissoesS.put(usuario, permissoes);
    }
    /*
     * verifica se o usuário é root ou dono, caso sim retorna true porque os dois tem as permissões
     * se não, ele continua e verifica as permissões do usuário no diretório
     * o código continua se nenhuma das condições tiver sido satisfeita e procura as permissões herdadas do pai
     * retorna falso se o usuário não tem a permissão
     */
    public boolean temPermissao(String usuario, char permissaoRequerida){

        if(usuario.equals("root") || usuario.equals(dono))
            return true;

        String permissoesDoUsuario = permissoesS.get(usuario);
        
        if(permissoesDoUsuario != null && permissoesDoUsuario.indexOf(permissaoRequerida) != -1)
            return true;
        
        Diretorio pai = getPai();
        if(pai != null){
            return pai.temPermissao(usuario, permissaoRequerida);
        }
        return false;
    }

    public void addFilho(Diretorio filho){
        filho.setPai(this);
        filhos.put(filho.getNome(), filho);
    }

    public Diretorio removerFilho(String nome){
        return filhos.remove(nome);
    }

    public String getNome() {
        return nome;
    }
    public void setNome(String nome) {
        this.nome = nome;
    }
    public String getDono() {
        return dono;
    }
    public void setDono(String dono) {
        this.dono = dono;
    }
    public String getPermissoesA() {
        return permissoesA;
    }
    public void setPermissoesA(String permissoesA) {
        this.permissoesA = permissoesA;
    }
    public Diretorio getPai() {
        return pai;
    }
    public void setPai(Diretorio pai) {
        this.pai = pai;
    }
    public Map<String, Diretorio> getFilhos() {
        return filhos;
    }
    public void setFilhos(Map<String, Diretorio> filhos) {
        this.filhos = filhos;
    }
    public Map<String, String> getPermissoesS() {
        return permissoesS;
    }
    public void setPermissoesS(Map<String, String> permissoesS) {
        this.permissoesS = permissoesS;
    }

    public boolean isFile(){
        return false;
    }


    public Diretorio(String nome, String dono, String permissoesA, Diretorio pai, Map<String, Diretorio> filhos,
            Map<String, String> permissoesS) {
        setNome(nome);
        setDono(dono);
        setPermissoesA(permissoesA);
        setPai(pai);
        setFilhos(filhos);
        setPermissoesS(permissoesS);
    }

    
    
}
