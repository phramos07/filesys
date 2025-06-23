package core;

public class Usuario {
    private String identificador;
    private String pastaBase;
    private String nivelAcesso;

    public Usuario(String identificador, String nivelAcesso, String pastaBase) {
        this.setIdentificador(identificador);
        this.setNivelAcesso(nivelAcesso);
        this.setPastaBase(pastaBase);
    }

    public String getIdentificador() {
        return identificador;
    }

    public String getNivelAcesso() {
        return nivelAcesso;
    }

    public void setNivelAcesso(String nivelAcesso) {
        this.nivelAcesso = nivelAcesso;
    }

    public void setIdentificador(String identificador) {
        this.identificador = identificador;
    }

    public void setPastaBase(String pastaBase) {
        this.pastaBase = pastaBase;
    }

    @Override
    public String toString() {
        return "Usuario{" +
                "identificador='" + identificador + '\'' +
                ", nivelAcesso='" + nivelAcesso + '\'' +
                ", pastaBase='" + pastaBase + '\'' +
                '}';
    }
}