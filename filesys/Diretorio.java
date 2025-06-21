package filesys;

import java.util.*;

public class Diretorio {
  private MetaDados metaDados;
  private Map<String, Diretorio> subDiretorio;
  private Map<String, Arquivo> arquivos;
  private Diretorio pai;

  public Diretorio(String nome, String dono, Diretorio pai) {
    this.metaDados = new MetaDados(nome, dono);
    this.subDiretorio = new HashMap<>();
    this.arquivos = new HashMap<>();
    this.pai = pai;
  }

  public MetaDados getMetaDados() {
    return metaDados;
  }

  public Map<String, Diretorio> getSubDiretorio() {
    return subDiretorio;
  }

  public Map<String, Arquivo> getArquivos() {
    return arquivos;
  }

  public Diretorio getPai() {
    return pai;
  }

  // Adicionado para a funcionalidade 'mv'
  public void setPai(Diretorio pai) {
      this.pai = pai;
  }

  public void addSubDiretorio(Diretorio dir) {
    subDiretorio.put(dir.getMetaDados().getNome(), dir);
  }

  public void addArquivo(Arquivo arq) {
    arquivos.put(arq.getMetaDados().getNome(), arq);
  }

  public boolean existeSubDiretorio(String nome) {
    return subDiretorio.containsKey(nome);
  }

  public boolean existeArquivo(String nome) {
    return arquivos.containsKey(nome);
  }
}