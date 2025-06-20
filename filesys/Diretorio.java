package filesys;

import java.util.ArrayList;
import java.util.List;

public class Diretorio {
    private Metadata metadata;
    private List<Diretorio> subDiretorios = new ArrayList<>();
    private List<Arquivo> arquivos = new ArrayList<>();

    public Diretorio(Metadata metadata, List<Diretorio> subDiretorios, List<Arquivo> arquivos) {
        this.metadata = metadata;
        this.subDiretorios = subDiretorios;
        this.arquivos = arquivos;
    }

    public Diretorio(Metadata metadata, List<Diretorio> subDiretorios) {
        this.metadata = metadata;
        this.subDiretorios = subDiretorios;
    }

    public Diretorio(String owner, String name) {
        this.metadata = new Metadata(name, owner);
        this.metadata.getPermissions().put(owner, "rwx");
        this.subDiretorios = new ArrayList<>();
        this.arquivos = new ArrayList<>();
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public List<Diretorio> getSubDiretorios() {
        return subDiretorios;
    }

    public List<Arquivo> getArquivos() {
        return arquivos;
    }

    public void addSubDiretorio(Diretorio dir) {
        subDiretorios.add(dir);
    }

    public void addFile(Arquivo file) {
        arquivos.add(file);
    }

}
