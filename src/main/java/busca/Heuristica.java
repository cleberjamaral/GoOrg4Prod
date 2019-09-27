package busca;


/**
 * Interface para estados que implementam a funcao h()
 *
 * @author  jomi
 */

public interface Heuristica {
    
    /**
     * estimativa de custo
     * @return int of heuristic
     */
    public int h();

}
