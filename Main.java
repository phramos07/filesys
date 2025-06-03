import filesys.IFileSystem;
import filesys.FileSystem;

import java.util.Scanner;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import exception.PermissaoException;
import exception.CaminhoJaExistenteException;
import exception.CaminhoNaoEncontradoException;

/*
 MENU INTERATIVO PARA O SISTEMA DE ARQUIVOS
 SINTA-SE LIVRE PARA ALTERAR A CLASSE MAIN
*/
public class Main {

    // Constantes úteis para a versão interativa.
    private static final String ROOT_USER = "root";
    private static final String ROOT_DIR = "/";
    private static final int READ_BUFFER_SIZE = 256;

    // Sistema de arquivos
    private static IFileSystem fileSystem;

    // Scanner para leitura de entrada do usuário
    private static Scanner scanner = new Scanner(System.in);

    // Usuário que está executando o programa
    private static String user;

    // Lista interna para guardar, temporariamente, cada permissão lida do arquivo
    private static class EntryPermissao {
        String usuario;
        String dir;
        String permissoes;
        EntryPermissao(String u, String d, String p) {
            this.usuario = u;
            this.dir = d;
            this.permissoes = p;
        }
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usuário não fornecido");
            return;
        }
        user = args[1];

        // Lista onde guardaremos as linhas do arquivo 'users/users'
        List<EntryPermissao> entradas = new ArrayList<>();

        // 1) Ler o arquivo users/users e armazenar cada tripla em 'entradas'
        try {
            Scanner userScanner = new Scanner(new java.io.File("users/users"));
            while (userScanner.hasNextLine()) {
                String line = userScanner.nextLine().trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split("\\s+");
                if (parts.length == 3) {
                    String userListed = parts[0];     // Ex: "maria"
                    String dir        = parts[1];     // Ex: "/**"
                    String perm       = parts[2];     // Ex: "rw-"

                    // Armazena temporariamente para aplicar via chmod mais tarde
                    entradas.add(new EntryPermissao(userListed, dir, perm));
                }
                else {
                    System.out.println("Formato ruim no arquivo de usuários. Linha: " + line);
                }
            }
            userScanner.close();
        }
        catch (FileNotFoundException e) {
            System.out.println("Arquivo de usuários não encontrado");
            return;
        }

        // 2) Cria o FileSystem (virtual, em memória)
        //    (aqui poderíamos passar lista de usuários se quisesse, mas vamos usar
        //     a própria chamada chmod para inicializar as permissões)
        fileSystem = new FileSystem();

        // 3) “Processar” cada entrada: chamar chmod em cada tripla (usuário=root)
        for (EntryPermissao ep : entradas) {
            // Traduzimos "/**" como "/" no nosso FileSystem virtual
            String caminhoParaChmod = ep.dir;
            if (caminhoParaChmod.equals("/**")) {
                caminhoParaChmod = ROOT_DIR;
            }
            try {
                // ROOT_USER tem permissão para alterar tudo
                fileSystem.chmod(caminhoParaChmod, ROOT_USER, ep.usuario, ep.permissoes);
            }
            catch (CaminhoNaoEncontradoException | PermissaoException ex) {
                // Geralmente não deve acontecer, pois sabemos que o ROOT_DIR ("/") existe
                // Mas, se o usuário pediu permissão para um diretório que não existe,
                // imprimimos uma mensagem de advertência e continuamos
                System.out.println("Aviso: não foi possível aplicar permissão em '"
                                   + ep.dir + "' para '" + ep.usuario
                                   + "': " + ex.getMessage());
            }
        }

        // Se você quiser garantir que a raiz foi criada (caso FileSystem não faça sozinho),
        // descomente o bloco abaixo:
        /*
        try {
            fileSystem.mkdir(ROOT_DIR, ROOT_USER);
        } catch (CaminhoJaExistenteException | PermissaoException e) {
            System.out.println(e.getMessage());
        }
        */

        // 4) Inicia o menu interativo
        menu();
    }

    // … o resto de Main fica inalterado, igual ao que você já tinha …
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
                    case "0":
                        System.out.println("Encerrando...");
                        return;
                    default:
                        System.out.println("Comando inválido!");
                }
            }
            catch (CaminhoNaoEncontradoException | CaminhoJaExistenteException | PermissaoException e) {
                System.out.println("Erro: " + e.getMessage());
            }

            System.out.println("Pressione Enter para continuar...");
            scanner.nextLine();
            System.out.print("\033[H\033[2J");
            System.out.flush();
        }
    }

    public static void chmod() throws CaminhoNaoEncontradoException, PermissaoException {
        System.out.println("Insira o caminho do arquivo ou diretório:");
        String caminho = scanner.nextLine();
        System.out.println("Insira o usuário para o qual deseja alterar as permissões:");
        String usuarioAlvo = scanner.nextLine();
        System.out.println("Insira a permissão (formato: 3 caracteres\"rwx\"): ");
        String permissoes = scanner.nextLine();

        fileSystem.chmod(caminho, user, usuarioAlvo, permissoes);
    }

    public static void mkdir() throws CaminhoJaExistenteException, PermissaoException {
        System.out.println("Insira o caminho do diretório a ser criado:");
        String caminho = scanner.nextLine();

        fileSystem.mkdir(caminho, user);
    }

    public static void rm() throws CaminhoNaoEncontradoException, PermissaoException {
        System.out.println("Insira o caminho do diretório a ser removido:");
        String caminho = scanner.nextLine();
        System.out.println("Remover recursivamente? (true/false):");
        boolean recursivo = Boolean.parseBoolean(scanner.nextLine());

        fileSystem.rm(caminho, user, recursivo);
    }

    public static void touch() throws CaminhoJaExistenteException, PermissaoException {
        System.out.println("Insira o caminho do arquivo a ser criado:");
        String caminho = scanner.nextLine();

        fileSystem.touch(caminho, user);
    }

    public static void write() throws CaminhoNaoEncontradoException, PermissaoException {
        System.out.println("Insira o caminho do arquivo a ser escrito:");
        String caminho = scanner.nextLine();
        System.out.println("Anexar? (true/false):");
        boolean anexar = Boolean.parseBoolean(scanner.nextLine());
        System.out.println("Insira o conteúdo a ser escrito:");
        String content = scanner.nextLine();
        byte[] buffer = content.getBytes();

        fileSystem.write(caminho, user, anexar, buffer);
    }

    public static void read() throws CaminhoNaoEncontradoException, PermissaoException {
        System.out.println("Insira o caminho do arquivo a ser lido:");
        String caminho = scanner.nextLine();
        byte[] buffer = new byte[READ_BUFFER_SIZE];

        fileSystem.read(caminho, user, buffer);
    }

    public static void mv() throws CaminhoNaoEncontradoException, PermissaoException {
        System.out.println("Insira o caminho do arquivo a ser movido:");
        String caminhoAntigo = scanner.nextLine();
        System.out.println("Insira o novo caminho do arquivo:");
        String caminhoNovo = scanner.nextLine();

        fileSystem.mv(caminhoAntigo, caminhoNovo, user);
    }

    public static void ls() throws CaminhoNaoEncontradoException, PermissaoException {
        System.out.println("Insira o caminho do diretório a ser listado:");
        String caminho = scanner.nextLine();
        System.out.println("Listar recursivamente? (true/false):");
        boolean recursivo = Boolean.parseBoolean(scanner.nextLine());

        fileSystem.ls(caminho, user, recursivo);
    }

    public static void cp() throws CaminhoNaoEncontradoException, PermissaoException {
        System.out.println("Insira o caminho da origem do arquivo a ser copiado:");
        String caminhoOrigem = scanner.nextLine();
        System.out.println("Insira o caminho do destino do arquivo a ser copiado:");
        String caminhoDestino = scanner.nextLine();
        System.out.println("Copiar recursivamente? (true/false):");
        boolean recursivo = Boolean.parseBoolean(scanner.nextLine());

        fileSystem.cp(caminhoOrigem, caminhoDestino, user, recursivo);
    }
}
