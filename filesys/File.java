package filesys;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;


public class File {
    private MetaData metaData; // Metadados do arquivo (nome, dono, permissões, etc.)
    private List<Block> blocks;
    private long size;
    private static final int DEFAULT_BLOCK_SIZE = 4096; 
    /**
     * Construtor para criar um novo arquivo vazio.
     * @param name O nome do arquivo.
     * @param owner O dono do arquivo.
     */
    public File(String name, String owner) {
        this.metaData = new MetaData(name, owner, false);
        this.blocks = new ArrayList<>();
        this.size = 0;
    }

    /**
     * @return O objeto MetaData associado a este arquivo.
     */
    public MetaData getMetaData() {
        return metaData;
    }

    /**
     * @return O tamanho atual do arquivo.
     */
    public long getSize() {
        return size;
    }

    /**
     * @param size O novo tamanho do arquivo.
     */
    public void setSize(long size) {
        this.size = size;
        this.metaData.updateModificationTime(); // Atualiza o timestamp de modificação
    }

    /**
     * Escreve dados no arquivo.
     * @param data 
     * @param offset
     * @param append 
     * @return 
     */
    public long write(byte[] data, long offset, boolean append) {
        if (data == null || data.length == 0) {
            return 0; // Não há dados para escrever.
        }

        long actualOffset = append ? this.size : offset; // Determina o offset de escrita real.
        long bytesWritten = 0; // Contador de bytes escritos.
        int dataPointer = 0; // Ponteiro para o array de dados a serem escritos.

        // Garante que o arquivo tenha blocos suficientes até o offset de escrita.
        // Se o offset for muito grande, blocos vazios intermediários são criados.
        // Isso simula a alocação de espaço conforme a demanda.
        while (actualOffset / DEFAULT_BLOCK_SIZE >= blocks.size()) {
            blocks.add(new Block(DEFAULT_BLOCK_SIZE));
        }

        // Itera sobre os dados a serem escritos, bloco por bloco.
        while (dataPointer < data.length) {
            long blockIndex = actualOffset / DEFAULT_BLOCK_SIZE; // Índice do bloco atual.
            int offsetInBlock = (int) (actualOffset % DEFAULT_BLOCK_SIZE); // Offset dentro do bloco.

            // Garante que o bloco exista, criando novos se necessário.
            if (blockIndex >= blocks.size()) {
                blocks.add(new Block(DEFAULT_BLOCK_SIZE));
            }

            Block currentBlock = blocks.get((int) blockIndex);

            // Calcula quantos bytes do `data` atual podem ser escritos no `currentBlock`.
            int bytesToProcessInCurrentBlock = Math.min(data.length - dataPointer, currentBlock.getCapacity() - offsetInBlock);

            // Escreve a parte dos dados no bloco atual.
            int writtenToBlock = currentBlock.write(data, dataPointer, bytesToProcessInCurrentBlock, offsetInBlock);

            bytesWritten += writtenToBlock; // Acumula bytes escritos.
            dataPointer += writtenToBlock; // Avança o ponteiro nos dados de origem.
            actualOffset += writtenToBlock; // Avança o offset total no arquivo.

            // Atualiza o currentSize do bloco se os dados se estenderem.
            currentBlock.setCurrentSize(Math.max(currentBlock.getCurrentSize(), offsetInBlock + writtenToBlock));
            
            // Se o bloco foi totalmente preenchido mas ainda há dados, o loop continuará para o próximo bloco.
            // Se writtenToBlock for 0 e dataPointer ainda < data.length, indica um problema (ex: offsetInBlock = capacity)
            // mas o loop `while` deve tratar isso avançando para o próximo blockIndex.
        }
        
        // Atualiza o tamanho total do arquivo para o maior valor entre o tamanho original e o novo offset final.
        this.size = Math.max(this.size, actualOffset);
        this.metaData.updateModificationTime(); // Atualiza o tempo de modificação do arquivo.
        return bytesWritten;
    }


    /**
     * Lê dados do arquivo a partir de um determinado offset para um buffer de destino.
     * @param destination O array de bytes onde os dados lidos serão armazenados.
     * @param offset O offset no arquivo onde a leitura deve começar.
     * @param length O número máximo de bytes a serem lidos.
     * @return O número de bytes realmente lidos.
     */
    public long read(byte[] destination, long offset, int length) {
        if (destination == null || destination.length == 0 || length <= 0) {
            return 0; // Buffer de destino inválido ou nada para ler.
        }
        if (offset >= this.size) {
            return 0; // Offset fora dos limites do arquivo (nada para ler).
        }

        long bytesRead = 0; // Contador de bytes lidos.
        int destinationPointer = 0; // Ponteiro para o array de destino.

        // Itera enquanto há bytes para ler (`bytesRead < length`) e ainda há dados no arquivo (`offset < this.size`).
        while (bytesRead < length && offset < this.size) {
            long blockIndex = offset / DEFAULT_BLOCK_SIZE; // Índice do bloco atual.
            int offsetInBlock = (int) (offset % DEFAULT_BLOCK_SIZE); // Offset dentro do bloco.

            // Se o índice do bloco é maior ou igual ao número de blocos existentes, não há mais dados.
            if (blockIndex >= blocks.size()) {
                break;
            }

            Block currentBlock = blocks.get((int) blockIndex);

            // Calcula quantos bytes podem ser lidos do bloco atual para o buffer de destino.
            int bytesToProcessInCurrentBlock = Math.min(length - destinationPointer, currentBlock.getCurrentSize() - offsetInBlock);

            if (bytesToProcessInCurrentBlock <= 0) {
                // Se não há mais dados válidos no bloco atual a partir do offset,
                // avança para o início do próximo bloco e continua o loop.
                offset = (blockIndex + 1) * DEFAULT_BLOCK_SIZE;
                continue;
            }

            // Lê a parte dos dados do bloco atual para o buffer de destino.
            int readFromBlock = currentBlock.read(destination, destinationPointer, bytesToProcessInCurrentBlock, offsetInBlock);

            bytesRead += readFromBlock; // Acumula bytes lidos.
            destinationPointer += readFromBlock; // Avança o ponteiro no buffer de destino.
            offset += readFromBlock; // Avança o offset total no arquivo.
        }

        return bytesRead;
    }

    /**
     * Define o conteúdo do arquivo como vazio, limpando todos os blocos e resetando o tamanho.
     * Útil para operações de truncamento ou sobrescrita completa.
     */
    public void clearContent() {
        this.blocks.clear(); // Remove todos os blocos.
        this.size = 0; // Redefine o tamanho do arquivo para zero.
        this.metaData.updateModificationTime(); // Atualiza o timestamp de modificação.
    }
    
    /**
     * Cria uma cópia profunda deste arquivo.
     * Isso significa que tanto os metadados quanto o conteúdo dos blocos são copiados
     * para uma nova instância de File.
     * @param newName O nome do novo arquivo copiado.
     * @param newOwner O dono do novo arquivo copiado.
     * @return Uma nova instância de File com os mesmos dados, mas um novo nome e dono.
     */
    public File deepCopy(String newName, String newOwner) {
        File newFile = new File(newName, newOwner); // Cria um novo arquivo.

        // Copia as permissões do arquivo original para o novo arquivo.
        // Primeiro, as permissões do dono.
        newFile.getMetaData().setPermission(newOwner, this.metaData.getPermissions().get(this.metaData.getOwner()));
        // Em seguida, se houver, as permissões de "other".
        if(this.metaData.getPermissions().containsKey("other")) {
            newFile.getMetaData().setPermission("other", this.metaData.getPermissions().get("other"));
        }

        // Copia os dados bloco por bloco.
        for (Block originalBlock : this.blocks) {
            Block newBlock = new Block(originalBlock.getCapacity()); // Cria um novo bloco com a mesma capacidade.
            // Copia o array de bytes do bloco original para o novo bloco, até o currentSize.
            System.arraycopy(originalBlock.getData(), 0, newBlock.getData(), 0, originalBlock.getCurrentSize());
            newBlock.setCurrentSize(originalBlock.getCurrentSize()); // Define o currentSize do novo bloco.
            newFile.blocks.add(newBlock); // Adiciona o novo bloco à lista de blocos do novo arquivo.
        }
        newFile.size = this.size; // Copia o tamanho total do arquivo.
        newFile.metaData.updateModificationTime(); // Garante que o tempo de modificação seja atualizado.
        return newFile;
    }
}
