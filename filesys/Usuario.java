package filesys;

import java.util.HashMap;
import java.util.Map;

public class Usuario {
    private final String nome;
    private final Map<String, String> permissoesPorCaminho; // ex: "/**" -> "rwx"

    public Usuario(String nome, String permissao) {
        this.nome = nome;
        this.permissoesPorCaminho = new HashMap<>();
        this.permissoesPorCaminho.put("**", permissao);
    }

    public Usuario(String nome) {
        this.nome = nome;
        this.permissoesPorCaminho = new HashMap<>();
    }

    public void adicionarPermissao(String caminho, String permissao) {
        permissoesPorCaminho.put(caminho, permissao);
    }

    public String getPermissaoParaCaminho(String caminho) {
        String melhorPermissao = "---";
        int melhorTamanho = -1;
        for (Map.Entry<String, String> entry : permissoesPorCaminho.entrySet()) {
            String padrao = entry.getKey();
            if (caminhoMatches(padrao, caminho) && padrao.length() > melhorTamanho) {
                melhorPermissao = entry.getValue();
                melhorTamanho = padrao.length();
            }
        }
        return melhorPermissao;
    }

    private boolean caminhoMatches(String padrao, String caminho) {
        // Aceita qualquer subcaminho
      if (padrao.endsWith("/**")) {
            String base = padrao.substring(0, padrao.length() - 3);
            // Só casa se for um subcaminho, não o próprio diretório base
            return !caminho.equals(base) && caminho.startsWith(base + "/");
        }
         // Aceita apenas filhos diretos
        if (padrao.endsWith("/*")) {
            String base = padrao.substring(0, padrao.length() - 2);
            if (!caminho.startsWith(base + "/"))
                return false;
            String resto = caminho.substring(base.length() + 1);
            return !resto.isEmpty() && !resto.contains("/"); 
        }
        return padrao.equals(caminho);
    }

    public String getNome() {
        return nome;
    }

    public boolean podeLer() {
        String permissao = permissoesPorCaminho.get("**");
        return permissao != null && permissao.contains("r");
    }

    public boolean podeEscrever() {
        String permissao = permissoesPorCaminho.get("**");
        return permissao != null && permissao.contains("w");
    }

    public boolean podeExecutar() {
        String permissao = permissoesPorCaminho.get("**");
        return permissao != null && permissao.contains("x");
    }

    public String getPermissao() {
        return permissoesPorCaminho.get("**");
    }
}
