package filesys;

public class FileSys {
    private Diretorio raiz;

    public FileSys() {
        this.raiz = new Diretorio("Raiz", "root");
    }
    public Diretorio getRaiz() {
        return raiz;
    }
    public void setRaiz(Diretorio raiz) {
        this.raiz = raiz;
    }
}
