package filesys;
import exception.BlocoVazioException;
import java.util.List;

public class Arquivo extends MetaDados {

    List<Bloco> blocos;
    private long tamanho;

    //adiciona bloco na lista e incrementa o tamanho atual
    public void adicionarBloco(Bloco bloco){
        blocos.add(bloco);
        tamanho++;
    }
    //remove blocos da lista por posição passada e decrementa o tamanho atual
    public Bloco removerBlocoPos(int pos){
        if(blocos.isEmpty()){
            throw new BlocoVazioException("Não é possível remover pois o bloco já está vazio");
        }
        tamanho--;
        return blocos.remove(pos);
    }
    //limpa o arquivo deletando tudo que está lá e iguala tamanho atual a 0
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
    public Arquivo(String nome, String permissoesBasicas, String dono, List<Bloco> blocos, long tamanho) {
        super(nome, permissoesBasicas, dono);
        this.blocos = blocos;
        this.tamanho = tamanho;
    }

    

}
