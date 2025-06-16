package filesys;
public class Block {
    private byte[] data;
    private int currentSize;

    /**
     * @param blockSize O tamanho maximo que este bloco pode armazenar em bytes.
     */
    public Block(int blockSize) {
        this.data = new byte[blockSize];
        this.currentSize = 0;
    }

    /**
     * @return Os dados do bloco.
     */
    public byte[] getData() {
        return data;
    }

    /**
     * @return O numero de bytes atualmente utilizados no bloco.
     */
    public int getCurrentSize() {
        return currentSize;
    }

    /**
     * @param currentSize O novo tamanho atual.
     */
    public void setCurrentSize(int currentSize) {
        this.currentSize = currentSize;
    }

    /**
     * @return
     */
    public int getCapacity() {
        return data.length;
    }

    /**
     * Escreve dados no bloco a partir de um determinado offset.
     * @param source 
     * @param sourceOffset
     * @param length 
     * @param destinationOffset 
     * @return O número de bytes realmente escritos no bloco.
     */
    public int write(byte[] source, int sourceOffset, int length, int destinationOffset) {
        int bytesToWrite = Math.min(length, getCapacity() - destinationOffset);
        bytesToWrite = Math.min(bytesToWrite, source.length - sourceOffset);

        // Copia os dados do array de origem para o array de dados do bloco.
        System.arraycopy(source, sourceOffset, this.data, destinationOffset, bytesToWrite);

        // Atualiza o currentSize se os novos dados se estenderem além do currentSize anterior.
        this.currentSize = Math.max(this.currentSize, destinationOffset + bytesToWrite);
        return bytesToWrite;
    }

    /**
     * Lê dados do bloco para um buffer de destino a partir de um determinado offset.
     * @param destination 
     * @param destinationOffset 
     * @param length 
     * @param sourceOffset
     * @return O número de bytes realmente lidos do bloco.
     */
    public int read(byte[] destination, int destinationOffset, int length, int sourceOffset) {
        int bytesToRead = Math.min(length, currentSize - sourceOffset);
        bytesToRead = Math.min(bytesToRead, destination.length - destinationOffset);

        if (bytesToRead <= 0) {
            return 0; // Não há nada para ler ou o offset está fora dos limites
        }

        System.arraycopy(this.data, sourceOffset, destination, destinationOffset, bytesToRead);
        return bytesToRead;
    }
}