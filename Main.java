import filesys.IFileSystem;
import filesys.Usuario;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.io.FileNotFoundException;

import exception.PermissaoException;
import exception.CaminhoJaExistenteException;
import exception.CaminhoNaoEncontradoException;


import filesys.FileSystem;
import filesys.Dir;

// MENU INTERATIVO PARA O SISTEMA DE ARQUIVOS
// SINTA-SE LIVRE PARA ALTERAR A CLASSE MAIN
public class Main {

    // Constantes úteis para a versão interativa. 
    // Para esse tipo de execução, o tamanho max do buffer de 
    // leitura pode ser menor.
    private static final String ROOT_USER = "root";
    private static final String ROOT_DIR = "/";
    private static final int READ_BUFFER_SIZE = 256;

    // Sistema de arquivos
    private static IFileSystem fileSystem;

    // Scanner para leitura de entrada do usuário
    private static Scanner scanner = new Scanner(System.in);

    // Usuário que está executando o programa
    private static String user;

    // Lista de usuários
    private static List<Usuario> usuarios = new ArrayList<>();

    // O sistema de arquivos é inteiramente virtual, ou seja, será reiniciado a cada execução do programa.
    // Logo, não é necessário salvar os arquivos em disco. O sistema será uma simulação em memória.
    public static void main(String[] args) {
        // Usuário que está executando o programa.
        // Para quaisquer operações que serão feitas por esse usuário em um caminho /path/**,
        // deve-se checar se o usuário tem permissão de escrita (r) neste caminho.
        if (args.length < 2) {
            System.out.println("Usuário não fornecido");
            return;
        }
        user = args[1];
        
        // Carrega a lista de usuários do sistema a partir de arquivo
        // Formato do arquivo users:
        //      username dir permission
        // Exemplo:
        //      maria /** rw-
        //      luzia /** rwx
        // Essa permissão vale para o diretório raiz e sub diretórios.
        // A partir do momento que um usuário cria outro diretório ou arquivo, 
        // a permissão desse usuário é de leitura, escrita e execução nesse novo diretório/arquivo,
        // e sempre será rwx para o usuário root.
        try {
            Scanner userScanner = new Scanner(new java.io.File("users/users"));
            while (userScanner.hasNextLine()) {
                String line = userScanner.nextLine().trim();
                if (!line.isEmpty()) {
                    String[] parts = line.split(" ");
                    if (parts.length == 3) {
                        String userListed = parts[0];
                        String dir = parts[1];
                        String dirPermission = parts[2];
                        
                        /* A FAZER:
                         * Processar a permissão de todos os usuários existentes por diretório.
                         * Por enquanto esse código somente imprime as permissões contidas no arquivo users.
                        */
                        System.out.println(userListed + " " + dir + " " + dirPermission); // Somente imprime o usuário, diretório e permissão

                        // Adicionar usuário à lista
                        usuarios.add(new Usuario(userListed, dir, dirPermission));
                    } else {
                        System.out.println("Formato ruim no arquivo de usuários. Linha: " + line);
                    }
                }
            }
            userScanner.close();
        } catch (FileNotFoundException e) { // Retorna se o arquivo de usuários não for encontrado
            System.out.println("Arquivo de usuários não encontrado");

            return;
        }

        /* REMOVER ISSO PQ NÃO PRECISA
        System.out.println("Usuários carregados:");
        for (Usuario user : usuarios) {
            System.out.println(user);
        }
        */

        // Finalmente cria o Sistema de Arquivos
        // Lista de usuários é imutável durante a execução do programa
        // Obs: Como passar a lista de usuários para o FileSystem?
        fileSystem = new FileSystem(usuarios);

        // // DESCOMENTE O BLOCO ABAIXO PARA CRIAR O DIRETÓRIO RAIZ ANTES DE RODAR O MENU
        // Cria o diretório raiz do sistema. Root sempre tem permissão total "rwx"
        /*
        try {
            fileSystem.mkdir(ROOT_DIR, ROOT_USER);
        } catch (CaminhoJaExistenteException | PermissaoException e) {
            System.out.println(e.getMessage());
        }
        */

        // Menu interativo.
        menu();
    }

    // Menu interativo para fins de teste.
    // Os testes junit não são feitos com esse menu,
    // mas diretamente na interface IFileSystem
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
        System.out.println("Insira a permissão (formato: 3 caracteres\"rwx\"):");
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


    public static void touch() throws CaminhoJaExistenteException, PermissaoException, CaminhoNaoEncontradoException {
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

    // Read foi modificado
    public static void read() throws CaminhoNaoEncontradoException, PermissaoException {
        System.out.println("Insira o caminho do arquivo a ser lido:");
        String caminho = scanner.nextLine();
        byte[] buffer = new byte[READ_BUFFER_SIZE]; // Exemplo de tamanho de buffer por load/leitura . O que acontece se o Buffer for menor que o conteúdo a ser lido?     

        int offset = 0; // Offset para leitura
        int offsetAntes;
        int leitura;

        System.out.println("Lendo arquivo: " + caminho + "\n");

        do {
            offsetAntes = offset; // Salva o offset antes da leitura
            offset += fileSystem.read(caminho, user, buffer, offset); // Lê o arquivo no caminho especificado
            leitura = offset - offsetAntes; // Calcula a quantidade de bytes lidos nesta iteração

            if (leitura > 0) {
                System.out.write(buffer, 0, leitura); // Escreve os bytes lidos no console
            } else {
                break; // Se não houver mais bytes para ler, sai do loop
            }
        } while (leitura > 0); // Garante que o offset seja não negativo

        System.out.flush(); // Garante que todos os bytes sejam escritos no console
        System.out.println("\n\nLeitura concluída.");
    }

    public static void mv() throws CaminhoNaoEncontradoException, PermissaoException {
        System.out.println("Insira o caminho do arquivo a ser movido:");
        String caminhoAntigo = scanner.nextLine();
        System.out.println("Insira o novo caminho do arquivo:");
        String caminhoNovo = scanner.nextLine();
        
        fileSystem.mv(caminhoAntigo, caminhoNovo, user);
    }

    public static void ls() throws CaminhoNaoEncontradoException, PermissaoException {
        System.out.print("Insira o caminho do diretório a ser listado: ");
        String caminho = scanner.nextLine();
        System.out.print("Listar recursivamente? (true/false): ");
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
