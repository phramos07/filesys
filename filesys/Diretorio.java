package filesys;

public class Diretorio {
    
    private MetaDados metaDados;
    private Diretorio[] subDiretorios;
    private Arquivo[] arquivos;

    public Diretorio(String nome, String dono) {
        this.metaDados = new MetaDados(nome, 0, dono);
        this.subDiretorios = new Diretorio[0];
        this.arquivos = new Arquivo[0];
    }

    public MetaDados getMetaDados() {
        return metaDados;
    }

    public void setMetaDados(MetaDados metaDados) {
        this.metaDados = metaDados;
    }

    public Diretorio[] getSubDiretorios() {
        return subDiretorios;
    }

    public Arquivo[] getArquivos() {
        return arquivos;
    }

    public void addSubDiretorio(Diretorio subDiretorio) {
        Diretorio[] novoSubDiretorios = new Diretorio[this.subDiretorios.length + 1];
        System.arraycopy(this.subDiretorios, 0, novoSubDiretorios, 0, this.subDiretorios.length);
        novoSubDiretorios[this.subDiretorios.length] = subDiretorio;
        this.subDiretorios = novoSubDiretorios;
    }

    public void addArquivo(Arquivo arquivo) {
        Arquivo[] novoArquivos = new Arquivo[this.arquivos.length + 1];
        System.arraycopy(this.arquivos, 0, novoArquivos, 0, this.arquivos.length);
        novoArquivos[this.arquivos.length] = arquivo;
        this.arquivos = novoArquivos;
        this.metaDados.setTamanho(this.metaDados.getTamanho() + arquivo.getMetaDados().getTamanho());
    }
}
