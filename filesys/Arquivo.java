package filesys;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import exception.PermissaoException;

public class Arquivo {
    private Metadata metadata;
    private static final int BLOCK_SIZE = 4096;
    private List<Bloco> blocos = new ArrayList<>();

    
    public Arquivo(String nomeArquivo, String usuario) {
        this.metadata = new Metadata(nomeArquivo, usuario);
    }

    public void write(byte[] buffer, boolean append) throws PermissaoException {
        if (metadata.getPermissions().values().stream().noneMatch(p -> p.contains("w"))) {
            throw new PermissaoException("User " + metadata.getOwner() + " doesn't have permission to write!");
        }

        if (!append) {
            blocos.clear();
        }

        int offset = 0;
        while (offset < buffer.length) {
            int remaining = buffer.length - offset;
            int chunkSize = Math.min(BLOCK_SIZE, remaining);
            byte[] chunk = new byte[chunkSize];
            System.arraycopy(buffer, offset, chunk, 0, chunkSize);
            blocos.add(new Bloco(chunk));
            offset += chunkSize;
        }
    }

    public byte[] read() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        for (Bloco block : blocos) {
            output.writeBytes(block.getDados());
        }
        return output.toByteArray();
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    public static int getBlockSize() {
        return BLOCK_SIZE;
    }

    public List<Bloco> getBlocos() {
        return blocos;
    }

    public void setBlocos(List<Bloco> blocos) {
        this.blocos = blocos;
    }

    public void addBloco(Bloco bloco) {
        this.blocos.add(bloco);
        this.metadata.setSize(this.metadata.getSize() + bloco.getSize());
    }

    public void removeBloco(Bloco bloco) {
        this.blocos.remove(bloco);
        this.metadata.setSize(this.metadata.getSize() - bloco.getSize());
    }

    public void clearBloco() {
        this.blocos.clear();
        this.metadata.setSize(0);
    }


}
