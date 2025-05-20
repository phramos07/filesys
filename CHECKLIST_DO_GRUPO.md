# Checklist do Projeto - Sistema de Arquivos POSIX

OIII!  
Segue um checklist detalhado do que ainda falta fazer no nosso projeto, com explicações simples para cada item.  
Se tiverem dúvidas, podem perguntar!

---

## 1. Métodos principais do sistema de arquivos

Ainda precisamos implementar os métodos principais que fazem o sistema funcionar, conforme pede o professor.  
Esses métodos são chamados quando o usuário executa comandos como criar, remover, copiar, mover arquivos e diretórios.

**Faltam implementar:**
- `rm` — Remover arquivos ou diretórios (com opção de apagar tudo dentro, se for recursivo)
- `write` — Escrever dados em um arquivo (pode ser para sobrescrever ou adicionar ao final)
- `read` — Ler dados de um arquivo (precisa ler em partes se o arquivo for grande)
- `mv` — Mover ou renomear arquivos/diretórios
- `ls` — Listar o conteúdo de um diretório (mostrar arquivos e subdiretórios)
- `cp` — Copiar arquivos ou diretórios (com opção de copiar tudo dentro, se for recursivo)

**No código, já deixei `// TODO` nesses métodos para facilitar achar onde falta implementar.  
O VS Code mostra uma linha azul do lado esquerdo nesses pontos.**

---

## 2. Testes Unitários

**Faltam testes para:**
- Remover arquivos/diretórios (`rm`)
- Escrever e ler arquivos (`write` e `read`)
- Mover/renomear (`mv`)
- Listar diretórios (`ls`)
- Copiar arquivos/diretórios (`cp`)
- Testar casos de erro (ex: tentar remover sem permissão, tentar ler arquivo inexistente, etc.)
- **Testes de permissão:** Já existe um arquivo chamado `PermissionTest.java` que testa se o sistema está respeitando as permissões de leitura, escrita 
e execução para cada usuário. - mas olhem la 

É importante rodar e revisar esses testes para garantir que o controle de acesso está correto.
Não lembro se precisa de mais testes, olha no arquivo :)

---

## 3. Permissões e Exceções

Em todas as operações, precisamos garantir:
- O usuário só pode fazer a operação se tiver permissão (leitura, escrita, execução)
- Se tentar fazer algo sem permissão, deve lançar uma exceção (erro)
- Se tentar acessar um caminho que não existe, também deve lançar exceção

**Exemplo:**  
Se o usuário "joao" tentar apagar um arquivo que não é dele e não tem permissão, o sistema deve impedir e mostrar um erro.

---

## 4. Menu Interativo

O menu interativo é fundamental para a apresentação e para o professor testar o sistema.  
Ele permite digitar comandos como `mkdir`, `touch`, `ls`, etc., e mostra o resultado na tela.
  
**Detalhes importantes:**
- O menu já está implementado na classe `Main.java`.
- Ele lê os usuários do arquivo `users/users`. 
- Cada opção do menu chama um método do sistema de arquivos (ex: criar diretório, listar, remover, etc.).
- O menu já trata exceções e mostra mensagens de erro se o usuário não tiver permissão ou se o caminho não existir.
- Para comandos de leitura (`read`), o menu já está preparado para ler arquivos grandes em partes, usando o buffer e o `Offset`.
- **Revisar o menu:** Garantir que todos os comandos estão funcionando, que as mensagens estão claras e que o sistema responde corretamente a erros de permissão e caminhos inválidos.


---

## 5. Comentários e Documentação

- Comentar o código explicando o que cada método faz, principalmente os mais importantes.
- Se mudarmos algo na interface fornecida pelo professor, justificar com um comentário.

nao lembro se eu fiz isso, aaaaaaaaa, FAÇAM!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

---

**:p**
- Sempre procure pelos `// TODO` no código para ver o que falta.

---
