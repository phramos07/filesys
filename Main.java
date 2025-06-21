import filesys.IFileSystem;
import filesys.FileSystemImpl;

import java.util.Scanner;
import java.io.FileNotFoundException;

import exception.PermissaoException;
import exception.CaminhoJaExistenteException;
import exception.CaminhoNaoEncontradoException;

// MENU INTERATIVO PARA O SISTEMA DE ARQUIVOS
public class Main {
    private static final String ROOT_USER = "root";
    private static final String ROOT_DIR = "/";
    private static final int READ_BUFFER_SIZE = 256;

    private static IFileSystem fileSystem;
    private static Scanner scanner = new Scanner(System.in);
    private static String user;

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usuário não fornecido");
            return;
        }
        user = args[0];

        try {
            Scanner userScanner = new Scanner(new java.io.File("./users/users"));
            while (userScanner.hasNextLine()) {
                String line = userScanner.nextLine().trim();
                if (!line.isEmpty()) {
                    String[] parts = line.split(" ");
                    if (parts.length == 3) {
                        String userListed = parts[0];
                        String dir = parts[1];
                        String dirPermission = parts[2];
                        System.out.println(userListed + " " + dir + " " + dirPermission);
                    } else {
                        System.out.println("Formato ruim no arquivo de usuários. Linha: " + line);
                    }
                }
            }
            userScanner.close();
        } catch (FileNotFoundException e) {
            System.out.println("Arquivo de usuários não encontrado");
            return;
        }

        fileSystem = new FileSystemImpl();

        // ✅ Criação do diretório raiz /
        try {
            fileSystem.mkdir(ROOT_DIR, ROOT_USER);
        } catch (CaminhoJaExistenteException | PermissaoException e) {
            // Raiz já existe ou erro de permissão
            System.out.println("Aviso ao criar raiz: " + e.getMessage());
        }

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
                    case "1": chmod(); break;
                    case "2": mkdir(); break;
                    case "3": rm(); break;
                    case "4": touch(); break;
                    case "5": write(); break;
                    case "6": read(); break;
                    case "7": mv(); break;
                    case "8": ls(); break;
                    case "9": cp(); break;
                    case "0": System.out.println("Encerrando..."); return;
                    default: System.out.println("Comando inválido!");
                }
            } catch (Exception e) {
                System.out.println("Erro: " + e.getMessage());
            }
            System.out.println("Pressione Enter para continuar...");
            scanner.nextLine();
            System.out.print("\033[H\033[2J");
            System.out.flush();
        }
    }

    public static void chmod() throws CaminhoNaoEncontradoException, PermissaoException {
        System.out.print("Caminho: "); String caminho = scanner.nextLine();
        System.out.print("Usuário alvo: "); String usuarioAlvo = scanner.nextLine();
        System.out.print("Permissões (rwx): "); String permissoes = scanner.nextLine();
        fileSystem.chmod(caminho, user, usuarioAlvo, permissoes);
    }

    public static void mkdir() throws CaminhoJaExistenteException, PermissaoException {
        System.out.print("Caminho do diretório: ");
        String caminho = scanner.nextLine();
        fileSystem.mkdir(caminho, user);
    }

    public static void rm() throws CaminhoNaoEncontradoException, PermissaoException {
        System.out.print("Caminho a remover: ");
        String caminho = scanner.nextLine();
        System.out.print("Remover recursivamente? (true/false): ");
        boolean recursivo = Boolean.parseBoolean(scanner.nextLine());
        fileSystem.rm(caminho, user, recursivo);
    }

    public static void touch() throws CaminhoJaExistenteException, PermissaoException {
        System.out.print("Caminho do novo arquivo: ");
        String caminho = scanner.nextLine();
        fileSystem.touch(caminho, user);
    }

    public static void write() throws CaminhoNaoEncontradoException, PermissaoException {
        System.out.print("Caminho do arquivo: ");
        String caminho = scanner.nextLine();
        System.out.print("Anexar? (true/false): ");
        boolean anexar = Boolean.parseBoolean(scanner.nextLine());
        System.out.print("Conteúdo: ");
        String content = scanner.nextLine();
        fileSystem.write(caminho, user, anexar, content.getBytes());
    }

    public static void read() throws CaminhoNaoEncontradoException, PermissaoException {
        System.out.print("Caminho do arquivo: ");
        String caminho = scanner.nextLine();
        byte[] buffer = new byte[READ_BUFFER_SIZE];
        fileSystem.read(caminho, user, buffer);
        System.out.println("Conteúdo: " + new String(buffer).trim());
    }

    public static void mv() throws CaminhoNaoEncontradoException, PermissaoException {
        System.out.print("Caminho antigo: ");
        String origem = scanner.nextLine();
        System.out.print("Novo caminho: ");
        String destino = scanner.nextLine();
        fileSystem.mv(origem, destino, user);
    }

    public static void ls() throws CaminhoNaoEncontradoException, PermissaoException {
        System.out.print("Caminho do diretório: ");
        String caminho = scanner.nextLine();
        System.out.print("Recursivo? (true/false): ");
        boolean recursivo = Boolean.parseBoolean(scanner.nextLine());
        fileSystem.ls(caminho, user, recursivo);
    }

    public static void cp() throws CaminhoNaoEncontradoException, PermissaoException {
        System.out.print("Caminho origem: ");
        String origem = scanner.nextLine();
        System.out.print("Caminho destino: ");
        String destino = scanner.nextLine();
        System.out.print("Recursivo? (true/false): ");
        boolean recursivo = Boolean.parseBoolean(scanner.nextLine());
        fileSystem.cp(origem, destino, user, recursivo);
    }
}
