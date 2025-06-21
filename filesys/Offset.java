package filesys;

/**
 * Classe utilitária para representar e manipular o offset (posição) de
 * escrita/leitura em arquivos.
 * Permite controle de valor máximo, incremento seguro e validação de limites.
 *
 * <p>
 * Usada para operações sequenciais em arquivos no sistema de arquivos virtual.
 * </p>
 *
 * Exemplo de uso:
 * 
 * <pre>
 * Offset offset = new Offset(0);
 * offset.add(10);
 * int pos = offset.getValue();
 * </pre>
 *
 * @author SeuNome
 */
public class Offset {
  /** Valor atual do offset. */
  private int value;
  /** Valor máximo permitido para o offset. */
  private int max;

  /**
   * Cria um offset com valor inicial 0 e valor máximo Integer.MAX_VALUE.
   */
  public Offset() {
    this.setValue(0);
    this.setMax(Integer.MAX_VALUE);
  }

  /**
   * Cria um offset com valor inicial definido e valor máximo Integer.MAX_VALUE.
   * 
   * @param initialValue valor inicial do offset
   */
  public Offset(int initialValue) {
    this.setValue(initialValue);
    this.setMax(Integer.MAX_VALUE);
  }

  /**
   * Cria um offset com valor inicial e valor máximo definidos.
   * 
   * @param initialValue valor inicial do offset
   * @param max          valor máximo permitido
   */
  public Offset(int initialValue, int max) {
    this.setValue(initialValue);
    this.setMax(max);
  }

  /**
   * Retorna o valor atual do offset.
   * 
   * @return valor do offset
   */
  public int getValue() {
    return value;
  }

  /**
   * Define o valor do offset, respeitando os limites.
   * 
   * @param value novo valor do offset
   * @throws IllegalArgumentException se value < 0 ou value > max
   */
  public void setValue(int value) {
    if (value < 0) {
      throw new IllegalArgumentException("Offset não pode ser negativo.");
    }
    if (value > max) {
      throw new IllegalArgumentException("Offset excede o valor máximo permitido (" + max + ").");
    }
    this.value = value;
  }

  /**
   * Incrementa o offset pelo delta informado.
   * 
   * @param delta valor a ser somado ao offset
   * @throws IllegalArgumentException se o novo valor ultrapassar os limites
   */
  public void add(int delta) {
    setValue(this.value + delta);
  }

  /**
   * Reseta o offset para zero.
   */
  public void reset() {
    this.value = 0;
  }

  /**
   * Define o valor máximo permitido para o offset.
   * 
   * @param max novo valor máximo
   * @throws IllegalArgumentException se max < 0
   */
  public void setMax(int max) {
    if (max < 0)
      throw new IllegalArgumentException("Max deve ser positivo.");
    this.max = max;
    if (value > max)
      value = max;
  }

  /**
   * Retorna o valor máximo permitido para o offset.
   * 
   * @return valor máximo
   */
  public int getMax() {
    return max;
  }
}
