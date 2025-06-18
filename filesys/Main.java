package filesys; 

import java.util.Scanner;
import java.nio.charset.StandardCharsets; 
import exception.PermissaoException;
import exception.CaminhoJaExistenteException;
import exception.CaminhoNaoEncontradoException;

public class Main {

    // Constantes úteis para a versão interativa.
    private static final String ROOT_USER = "root";
    private static final String ROOT_DIR = "/";
    private static final int READ_BUFFER_SIZE = 4096; // Tamanho do buffer de leitura (4KB)

    // Sistema de arquivos (usando a classe proxy)
    private static FileSystem fileSystemProxy; // Instância do proxy
    private static FileSystemImpl fileSystemImpl; // Referência direta ao impl para o getCurrentUser e outros

    // Scanner para leitura de entrada do usuário
    private static Scanner scanner = new Scanner(System.in);

    // Usuário que está executando o programa (definido pelos argumentos da linha de comando)
    private static String user;

    // O sistema de arquivos é inteiramente virtual, ou seja, será reiniciado a cada execução do programa.
    // Logo, não é necessário salvar os arquivos em disco. O sistema será uma simulação em memória.
    public static void main(String[] args) {
        // O usuário que está executando o programa é obtido dos argumentos da linha de comando.
        // Isso simula o usuário que "logou" no sistema.
        if (args.length < 1) { // Verifica se pelo menos o nome de usuário foi fornecido
            System.out.println("Uso: java Main <nome_do_usuario>");
            System.out.println("Exemplo: java Main joao");
            return;
        }
        user = args[0]; // Assume que o primeiro argumento é o nome do usuário

        // Inicializa o sistema de arquivos.
        fileSystemProxy = new FileSystem();
        // Acessa a implementação real para poder usar métodos como getCurrentUser()
        // Isso é um cast, pois fileSystemImpl é um atributo public final em FileSystem.java
        fileSystemImpl = (FileSystemImpl) fileSystemProxy.fileSystemImpl;
        
        // Define o usuário inicial do sistema de arquivos para o usuário fornecido nos args.
        // Se o usuário inicial for diferente de "root", as permissões serão aplicadas.
        fileSystemImpl.changeUser(user);

        // Não é mais necessário criar o diretório raiz, pois ele é criado no construtor de FileSystemImpl.
        System.out.println("Bem-vindo ao Sistema de Arquivos Virtual!");
        System.out.println("Usuário logado: " + fileSystemImpl.getCurrentUser());
        System.out.println("Digite 'help' para ver os comandos disponíveis.");

        // Menu interativo principal.
        menu();
    }

    // Menu interativo para fins de teste e demonstração.
    // Os testes junit não são feitos com esse menu,
    // mas diretamente na interface IFileSystem.
    public static void menu() {
        while (true) {
            System.out.println("\nComandos disponíveis:");
            System.out.println("1. chmod - Alterar permissões");
            System.out.println("2. mkdir - Criar diretório");
            System.out.println("3. rm - Remover arquivo/diretório");
            System.out.println("4. touch - Criar arquivo");
            System.out.println("5. write - Escrever em arquivo");
            System.out.println("6. read - Ler arquivo");
            System.out.println("7. mv - Mover/renomear arquivo");
            System.out.println("8. ls - Listar diretório");
            System.out.println("9. cp - Copiar arquivo");
            System.out.println("0. exit - Sair");
            System.out.print("\nDigite o comando desejado: ");

            String opcao = scanner.nextLine();
            try {
                switch (opcao) {
                    case "1":
                        chmod();
                        break;
                    case "2":
                        mkdir();
                        break;
                    case "3":
                        rm();
                        break;
                    case "4":
                        touch();
                        break;
                    case "5":
                        write();
                        break;
                    case "6":
                        read();
                        break;
                    case "7":
                        mv();
                        break;
                    case "8":
                        ls();
                        break;
                    case "9":
                        cp();
                        break;
                    case "0":
                        System.out.println("Encerrando...");
                        return;
                    default:
                        System.out.println("Comando inválido!");
                }
            } catch (CaminhoNaoEncontradoException | CaminhoJaExistenteException | PermissaoException | IllegalArgumentException e) {
                // Captura exceções e exibe mensagens de erro amigáveis.
                System.err.println("Erro: " + e.getMessage());
            }

            System.out.println("Pressione Enter para continuar...");
            scanner.nextLine();
            // Limpa a tela (funciona em alguns terminais)
            // System.out.print("\033[H\033[2J");
            // System.out.flush();
        }
    }

    /**
     * Implementa o comando chmod para o menu interativo.
     * Solicita o caminho, usuário alvo e novas permissões, e chama o método chmod do FileSystem.
     * @throws CaminhoNaoEncontradoException Se o caminho não for encontrado.
     * @throws PermissaoException Se o usuário atual não tiver permissão.
     */
    public static void chmod() throws CaminhoNaoEncontradoException, PermissaoException {
        System.out.println("Insira o caminho do arquivo ou diretório:");
        String caminho = scanner.nextLine();
        System.out.println("Insira o usuário para o qual deseja alterar as permissões (e.g., 'root', 'user1', 'other'):");
        String usuarioAlvo = scanner.nextLine();
        System.out.println("Insira a permissão (formato: 3 caracteres 'rwx', 'rw-', 'r-x', '---'):");
        String permissoes = scanner.nextLine();
        
        fileSystemProxy.chmod(caminho, user, usuarioAlvo, permissoes);
    }

    /**
     * Implementa o comando mkdir para o menu interativo.
     * Solicita o caminho para o novo diretório e chama o método mkdir do FileSystem.
     * @throws CaminhoJaExistenteException Se o caminho já existir.
     * @throws PermissaoException Se o usuário não tiver permissão.
     */
    public static void mkdir() throws CaminhoJaExistenteException, PermissaoException {
        System.out.println("Insira o caminho do diretório a ser criado (ex: /home/user/newdir):");
        String caminho = scanner.nextLine();
        
        fileSystemProxy.mkdir(caminho, user);
    }

    /**
     * Implementa o comando rm para o menu interativo.
     * Solicita o caminho e se a remoção deve ser recursiva, e chama o método rm do FileSystem.
     * @throws CaminhoNaoEncontradoException Se o caminho não for encontrado.
     * @throws PermissaoException Se o usuário não tiver permissão.
     */
    public static void rm() throws CaminhoNaoEncontradoException, PermissaoException {
        System.out.println("Insira o caminho do item a ser removido (ex: /home/user/file.txt ou /home/user/mydir):");
        String caminho = scanner.nextLine();
        System.out.println("Remover recursivamente para diretórios? (true/false):");
        boolean recursivo = Boolean.parseBoolean(scanner.nextLine());
        
        fileSystemProxy.rm(caminho, user, recursivo);
    }

    /**
     * Implementa o comando touch para o menu interativo.
     * Solicita o caminho para o novo arquivo e chama o método touch do FileSystem.
     * @throws CaminhoJaExistenteException Se o arquivo já existir como diretório.
     * @throws PermissaoException Se o usuário não tiver permissão.
     * @throws CaminhoNaoEncontradoException Se o diretório pai não existir.
     */
    public static void touch() throws CaminhoJaExistenteException, PermissaoException, CaminhoNaoEncontradoException {
        System.out.println("Insira o caminho do arquivo a ser criado ou atualizado (ex: /home/user/newfile.txt):");
        String caminho = scanner.nextLine();
        
        fileSystemProxy.touch(caminho, user);
    }

    /**
     * Implementa o comando write para o menu interativo.
     * Solicita o caminho, se deve anexar, e o conteúdo a ser escrito.
     * @throws CaminhoNaoEncontradoException Se o arquivo não for encontrado.
     * @throws PermissaoException Se o usuário não tiver permissão.
     */
    public static void write() throws CaminhoNaoEncontradoException, PermissaoException {
        System.out.println("Insira o caminho do arquivo a ser escrito (ex: /home/user/data.txt):");
        String caminho = scanner.nextLine();
        System.out.println("Anexar ao final do arquivo? (true/false):");
        boolean anexar = Boolean.parseBoolean(scanner.nextLine());
        System.out.println("Insira o conteúdo a ser escrito:");
        String content = scanner.nextLine();
        byte[] buffer = content.getBytes(StandardCharsets.UTF_8); // Converte a string para bytes
        
        fileSystemProxy.write(caminho, user, anexar, buffer);
    }

    /**
     * Implementa o comando read para o menu interativo.
     * Solicita o caminho do arquivo e tenta ler seu conteúdo para um buffer padrão.
     * @throws CaminhoNaoEncontradoException Se o arquivo não for encontrado.
     * @throws PermissaoException Se o usuário não tiver permissão.
     */
    public static void read() throws CaminhoNaoEncontradoException, PermissaoException {
        System.out.println("Insira o caminho do arquivo a ser lido (ex: /home/user/data.txt):");
        String caminho = scanner.nextLine();
        byte[] buffer = new byte[READ_BUFFER_SIZE]; // Cria um buffer de tamanho fixo para a leitura.
                                                   // O Main lerá o quanto couber no buffer ou o arquivo inteiro.
        
        fileSystemProxy.read(caminho, user, buffer);
        
        // Para exibir o conteúdo, é preciso saber quantos bytes foram efetivamente lidos
        // O método read em FileSystemImpl apenas imprime.
        // Para ter o retorno aqui, teríamos que mudar a assinatura da interface IFileSystem.read
        // A solução abaixo tenta mostrar o conteúdo lido, baseando-se no tamanho do arquivo.
        try {
            // Acessa o método getNodeAtPath da instância fileSystemImpl
            Object node = fileSystemImpl.getNodeAtPath(caminho);
            if (node instanceof filesys.File) { // Garante que é um objeto File do pacote filesys
                filesys.File file = (filesys.File) node;
                int actualLength = (int) Math.min(file.getSize(), READ_BUFFER_SIZE);
                String readContent = new String(buffer, 0, actualLength, StandardCharsets.UTF_8);
                System.out.println("Conteúdo lido: \"" + readContent + "\"");
            } else {
                System.out.println("Não foi possível exibir o conteúdo. O caminho não é um arquivo ou está vazio.");
            }
        } catch (CaminhoNaoEncontradoException e) {
             System.out.println("Erro ao obter o arquivo para exibição do conteúdo: " + e.getMessage());
        }
    }

    /**
     * Implementa o comando mv para o menu interativo.
     * Solicita os caminhos de origem e destino para mover/renomear um item.
     * @throws CaminhoNaoEncontradoException Se algum caminho não for encontrado.
     * @throws PermissaoException Se o usuário não tiver permissão.
     */
    public static void mv() throws CaminhoNaoEncontradoException, PermissaoException {
        System.out.println("Insira o caminho antigo do arquivo/diretório (origem):");
        String caminhoAntigo = scanner.nextLine();
        System.out.println("Insira o novo caminho/nome para o arquivo/diretório (destino):");
        String caminhoNovo = scanner.nextLine();
        
        fileSystemProxy.mv(caminhoAntigo, caminhoNovo, user);
    }

    /**
     * Implementa o comando ls para o menu interativo.
     * Solicita o caminho e se a listagem deve ser recursiva.
     * @throws CaminhoNaoEncontradoException Se o caminho não for encontrado.
     * @throws PermissaoException Se o usuário não tiver permissão.
     */
    public static void ls() throws CaminhoNaoEncontradoException, PermissaoException {
        System.out.println("Insira o caminho do diretório a ser listado (ex: /home ou /):");
        String caminho = scanner.nextLine();
        System.out.println("Listar recursivamente? (true/false):");
        boolean recursivo = Boolean.parseBoolean(scanner.nextLine());
        
        fileSystemProxy.ls(caminho, user, recursivo);
    }

    /**
     * Implementa o comando cp para o menu interativo.
     * Solicita os caminhos de origem e destino para copiar um item.
     * @throws CaminhoNaoEncontradoException Se algum caminho não for encontrado.
     * @throws PermissaoException Se o usuário não tiver permissão.
     * @throws CaminhoJaExistenteException Se o item de destino já existir.
     */
    public static void cp() throws CaminhoNaoEncontradoException, PermissaoException, CaminhoJaExistenteException {
        System.out.println("Insira o caminho da origem do arquivo/diretório a ser copiado:");
        String caminhoOrigem = scanner.nextLine();
        System.out.println("Insira o caminho do destino do arquivo/diretório a ser copiado:");
        String caminhoDestino = scanner.nextLine();
        // A flag recursivo para cp é ignorada aqui, pois a implementação é naturalmente recursiva para diretórios.
        boolean recursivo = true; // Força como true, pois a implementação de cp é recursiva para diretórios.
        
        fileSystemProxy.cp(caminhoOrigem, caminhoDestino, user, recursivo);
    }
}
