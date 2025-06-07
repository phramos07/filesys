package filesys;
import exception.BlocoVazioException;
import java.util.List;

public class Arquivo extends Diretorio {
    List<Bloco> blocos;
    private long tamanho;

    public void adicionarBloco(Bloco bloco){
        blocos.add(bloco);
        tamanho++;
    }

    public Bloco removerBlocoPos(int pos){
        if(blocos.isEmpty()){
            throw new BlocoVazioException("Não é possível remover pois o bloco já está vazio");
        }
        tamanho--;
        return blocos.remove(pos);
    }

    public void limparArquivo(){
        if(blocos.isEmpty())
            throw new BlocoVazioException("Não é possível limpar pois o bloco já está vazio");
        blocos.clear();
        tamanho = 0;
    }

    public List<Bloco> getAllBlocos(){
        return blocos;
    }

      public void setBlocos(List<Bloco> blocos){
        this.blocos = blocos;
    }

    public long getTamanhoAtual(){
        return tamanho;
    }


}
