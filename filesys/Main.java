package filesys;

import java.util.Scanner;
import java.nio.charset.StandardCharsets;

import exception.PermissaoException;
import exception.CaminhoJaExistenteException;
import exception.CaminhoNaoEncontradoException;

public class Main {

    private static final String ROOT_USER = "root";
    private static final String ROOT_DIR = "/";
    private static final int READ_BUFFER_SIZE = 4096;

    private static FileSystem fileSystemProxy;
    private static FileSystemImpl fileSystemImpl;
    private static Scanner scanner = new Scanner(System.in);
    private static String user;

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Uso: java Main <nome_do_usuario>");
            System.out.println("Exemplo: java Main joao");
            return;
        }
        user = args[0];

        fileSystemProxy = new FileSystem();
        fileSystemImpl = (FileSystemImpl) fileSystemProxy.fileSystemImpl;
        fileSystemImpl.changeUser(user);

        System.out.println("Bem-vindo ao Sistema de Arquivos Virtual!");
        System.out.println("Usuário logado: " + fileSystemImpl.getCurrentUser());
        System.out.println("Digite 'help' para ver os comandos disponíveis.");

        menu();
    }

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
                System.err.println("Erro: " + e.getMessage());
            }

            System.out.println("Pressione Enter para continuar...");
            scanner.nextLine();
        }
    }

    public static void chmod() throws CaminhoNaoEncontradoException, PermissaoException {
        System.out.println("Insira o caminho do arquivo ou diretório:");
        String caminho = scanner.nextLine();
        System.out.println("Insira o usuário para o qual deseja alterar as permissões (e.g., 'root', 'user1', 'other'):");
        String usuarioAlvo = scanner.nextLine();
        System.out.println("Insira a permissão (formato: 3 caracteres 'rwx', 'rw-', 'r-x', '---'):");
        String permissoes = scanner.nextLine();

        fileSystemProxy.chmod(caminho, user, usuarioAlvo, permissoes);
    }

    public static void mkdir() throws CaminhoJaExistenteException, PermissaoException {
        System.out.println("Insira o caminho do diretório a ser criado (ex: /home/user/newdir):");
        String caminho = scanner.nextLine();

        fileSystemProxy.mkdir(caminho, user);
    }

    public static void rm() throws CaminhoNaoEncontradoException, PermissaoException {
        System.out.println("Insira o caminho do item a ser removido (ex: /home/user/file.txt ou /home/user/mydir):");
        String caminho = scanner.nextLine();
        System.out.println("Remover recursivamente para diretórios? (true/false):");
        boolean recursivo = Boolean.parseBoolean(scanner.nextLine());

        fileSystemProxy.rm(caminho, user, recursivo);
    }

    public static void touch() throws CaminhoJaExistenteException, PermissaoException, CaminhoNaoEncontradoException {
        System.out.println("Insira o caminho do arquivo a ser criado ou atualizado (ex: /home/user/newfile.txt):");
        String caminho = scanner.nextLine();

        fileSystemProxy.touch(caminho, user);
    }

    public static void write() throws CaminhoNaoEncontradoException, PermissaoException {
        System.out.println("Insira o caminho do arquivo a ser escrito (ex: /home/user/data.txt):");
        String caminho = scanner.nextLine();
        System.out.println("Anexar ao final do arquivo? (true/false):");
        boolean anexar = Boolean.parseBoolean(scanner.nextLine());
        System.out.println("Insira o conteúdo a ser escrito:");
        String content = scanner.nextLine();
        byte[] buffer = content.getBytes(StandardCharsets.UTF_8);

        fileSystemProxy.write(caminho, user, anexar, buffer);
    }

    public static void read() throws CaminhoNaoEncontradoException, PermissaoException {
        System.out.println("Insira o caminho do arquivo a ser lido (ex: /home/user/data.txt):");
        String caminho = scanner.nextLine();
        byte[] buffer = new byte[READ_BUFFER_SIZE];

        try {
            fileSystemProxy.read(caminho, user, buffer);
            Object node = fileSystemImpl.getNodeAtPath(caminho);
            if (node instanceof filesys.File) {
                filesys.File file = (filesys.File) node;
                int actualLength = (int) Math.min(file.getSize(), READ_BUFFER_SIZE);
                String readContent = new String(buffer, 0, actualLength, StandardCharsets.UTF_8);
                System.out.println("Conteúdo lido: \"" + readContent + "\"");
            } else {
                System.out.println("Não foi possível exibir o conteúdo. O caminho não é um arquivo ou está vazio.");
            }
        } catch (CaminhoNaoEncontradoException | PermissaoException e) {
            System.out.println("Erro ao ler/obter o arquivo para exibição do conteúdo: " + e.getMessage());
        }
    }

    public static void mv() throws CaminhoNaoEncontradoException, PermissaoException {
        System.out.println("Insira o caminho antigo do arquivo/diretório (origem):");
        String caminhoAntigo = scanner.nextLine();
        System.out.println("Insira o novo caminho/nome para o arquivo/diretório (destino):");
        String caminhoNovo = scanner.nextLine();

        fileSystemProxy.mv(caminhoAntigo, caminhoNovo, user);
    }

    public static void ls() throws CaminhoNaoEncontradoException, PermissaoException {
        System.out.println("Insira o caminho do diretório a ser listado (ex: /home ou /):");
        String caminho = scanner.nextLine();
        System.out.println("Listar recursivamente? (true/false):");
        boolean recursivo = Boolean.parseBoolean(scanner.nextLine());

        fileSystemProxy.ls(caminho, user, recursivo);
    }

    public static void cp() throws CaminhoNaoEncontradoException, PermissaoException, CaminhoJaExistenteException {
        System.out.println("Insira o caminho da origem do arquivo/diretório a ser copiado:");
        String caminhoOrigem = scanner.nextLine();
        System.out.println("Insira o caminho do destino do arquivo/diretório a ser copiado:");
        String caminhoDestino = scanner.nextLine();
        boolean recursivo = true;

        fileSystemProxy.cp(caminhoOrigem, caminhoDestino, user, recursivo);
    }
}