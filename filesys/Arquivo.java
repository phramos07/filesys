package filesys;

class Arquivo {
    private MetaDados metaDados;
    private Bloco[] bloco;

    public Arquivo(String nome, String dono) {
        this.metaDados = new MetaDados(nome, dono, "rwx");
    }

    public MetaDados getMetaDados() {
        return metaDados;
    }
}
