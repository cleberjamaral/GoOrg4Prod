package busca;

/**
 * Interface para estados que implementam a geracao de estados aleatorios
 *
 * @author  jomi
 */
public interface Aleatorio {
    /**
     * gera um estado aleatorio
     * @return generated State
     */
    public Estado geraAleatorio();


}
