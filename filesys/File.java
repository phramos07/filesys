package filesys;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

public class File {
    private MetaData metaData;
    private List<Block> blocks;
    private long size;
    private static final int DEFAULT_BLOCK_SIZE = 4096;

    public File(String name, String owner) {
        this.metaData = new MetaData(name, owner, false);
        this.blocks = new ArrayList<>();
        this.size = 0;
    }

    public MetaData getMetaData() {
        return metaData;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
        this.metaData.updateModificationTime();
    }

    // Escreve dados no arquivo a partir de um offset ou anexa ao final
    public long write(byte[] data, long offset, boolean append) {
        if (data == null || data.length == 0) {
            return 0;
        }

        long actualOffset = append ? this.size : offset;
        long bytesWritten = 0;
        int dataPointer = 0;

        while (actualOffset / DEFAULT_BLOCK_SIZE >= blocks.size()) {
            blocks.add(new Block(DEFAULT_BLOCK_SIZE));
        }

        while (dataPointer < data.length) {
            long blockIndex = actualOffset / DEFAULT_BLOCK_SIZE;
            int offsetInBlock = (int) (actualOffset % DEFAULT_BLOCK_SIZE);

            if (blockIndex >= blocks.size()) {
                blocks.add(new Block(DEFAULT_BLOCK_SIZE));
            }

            Block currentBlock = blocks.get((int) blockIndex);

            int bytesToProcessInCurrentBlock = Math.min(data.length - dataPointer, currentBlock.getCapacity() - offsetInBlock);

            int writtenToBlock = currentBlock.write(data, dataPointer, bytesToProcessInCurrentBlock, offsetInBlock);

            bytesWritten += writtenToBlock;
            dataPointer += writtenToBlock;
            actualOffset += writtenToBlock;

            currentBlock.setCurrentSize(Math.max(currentBlock.getCurrentSize(), offsetInBlock + writtenToBlock));
        }

        this.size = Math.max(this.size, actualOffset);
        this.metaData.updateModificationTime();
        return bytesWritten;
    }

    // Lê dados do arquivo a partir de um offset para um buffer de destino
    public long read(byte[] destination, long offset, int length) {
        if (destination == null || destination.length == 0 || length <= 0) {
            return 0;
        }
        if (offset >= this.size) {
            return 0;
        }

        long bytesRead = 0;
        int destinationPointer = 0;

        while (bytesRead < length && offset < this.size) {
            long blockIndex = offset / DEFAULT_BLOCK_SIZE;
            int offsetInBlock = (int) (offset % DEFAULT_BLOCK_SIZE);

            if (blockIndex >= blocks.size()) {
                break;
            }

            Block currentBlock = blocks.get((int) blockIndex);

            int bytesToProcessInCurrentBlock = Math.min(length - destinationPointer, currentBlock.getCurrentSize() - offsetInBlock);

            if (bytesToProcessInCurrentBlock <= 0) {
                offset = (blockIndex + 1) * DEFAULT_BLOCK_SIZE;
                continue;
            }

            int readFromBlock = currentBlock.read(destination, destinationPointer, bytesToProcessInCurrentBlock, offsetInBlock);

            bytesRead += readFromBlock;
            destinationPointer += readFromBlock;
            offset += readFromBlock;
        }

        return bytesRead;
    }

    public void clearContent() {
        this.blocks.clear();
        this.size = 0;
        this.metaData.updateModificationTime();
    }

    // Cria uma cópia profunda deste arquivo
    public File deepCopy(String newName, String newOwner) {
        File newFile = new File(newName, newOwner);

        newFile.getMetaData().setPermission(newOwner, this.metaData.getPermissions().get(this.metaData.getOwner()));
        if(this.metaData.getPermissions().containsKey("other")) {
            newFile.getMetaData().setPermission("other", this.metaData.getPermissions().get("other"));
        }

        for (Block originalBlock : this.blocks) {
            Block newBlock = new Block(originalBlock.getCapacity());
            System.arraycopy(originalBlock.getData(), 0, newBlock.getData(), 0, originalBlock.getCurrentSize());
            newBlock.setCurrentSize(originalBlock.getCurrentSize());
            newFile.blocks.add(newBlock);
        }
        newFile.size = this.size;
        newFile.metaData.updateModificationTime();
        return newFile;
    }
}