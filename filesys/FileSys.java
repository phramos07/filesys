package filesys;

import filesys.core.Diretorio;

public class FileSys {
    private Diretorio raiz;

    public FileSys(String dono) {
        this.raiz = new Diretorio("/", dono);
    }

    public Diretorio getRaiz() {
        return raiz;
    }
    public void setRaiz(Diretorio raiz) {
        this.raiz = raiz;
    }
}
