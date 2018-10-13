package busca;


/**
 * Interface para estados que implementam a funcao h()
 *
 * @author  jomi
 */

public interface Heuristica {
    
    /**
     * estimativa de custo
     */
    public int h();

}
