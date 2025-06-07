package filesys;

public class Bloco {
    byte[] bytes;

    public Bloco(){
        bytes = new byte[4096];
    }

    public byte[] getDados(){
        return bytes;
    }

    public void setBytes(byte[] bytes){
        this.bytes = bytes;
    }
}
