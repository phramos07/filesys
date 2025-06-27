package filesys;

public class Block {
    private byte[] data;
    private int currentSize;
    private final int capacity;

    public Block(int capacity) {
        this.capacity = capacity;
        this.data = new byte[capacity];
        this.currentSize = 0;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getCurrentSize() {
        return currentSize;
    }

    public void setCurrentSize(int size) {
        this.currentSize = size;
    }

    public byte[] getData() {
        return data;
    }

    // Escreve até 'length' bytes de 'src' começando em 'srcOffset' para o bloco a partir de 'blockOffset'
    public int write(byte[] src, int srcOffset, int length, int blockOffset) {
        if (blockOffset >= capacity) return 0;
        int bytesToWrite = Math.min(length, capacity - blockOffset);
        System.arraycopy(src, srcOffset, data, blockOffset, bytesToWrite);
        return bytesToWrite;
    }

    // Lê até 'length' bytes do bloco a partir de 'blockOffset' para 'dest' começando em 'destOffset'
    public int read(byte[] dest, int destOffset, int length, int blockOffset) {
        if (blockOffset >= currentSize) return 0;
        int bytesToRead = Math.min(length, currentSize - blockOffset);
        System.arraycopy(data, blockOffset, dest, destOffset, bytesToRead);
        return bytesToRead;
    }
}