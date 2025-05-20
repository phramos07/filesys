package model;

public abstract class ElementoFS {
    protected String nomeDiretorio;
    protected String permissoesPadrao;
    protected String donoDiretorio;

    public ElementoFS(String nomeDiretorio, String permissoesPadrao, String donoDiretorio) {
        this.nomeDiretorio = nomeDiretorio;
        this.permissoesPadrao = permissoesPadrao;
        this.donoDiretorio = donoDiretorio;
    }

    public String getNomeDiretorio() {
        return nomeDiretorio;
    }

    public void setNomeDiretorio(String nomeDiretorio) {
        this.nomeDiretorio = nomeDiretorio;
    }

    public String getPermissoesPadrao() {
        return permissoesPadrao;
    }

    public void setPermissoesPadrao(String permissoesPadrao) {
        this.permissoesPadrao = permissoesPadrao;
    }

    public String getDonoDiretorio() {
        return donoDiretorio;
    }

    public void setDonoDiretorio(String donoDiretorio) {
        this.donoDiretorio = donoDiretorio;
    }

    public abstract boolean isArquivo();
}