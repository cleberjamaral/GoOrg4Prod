package busca;

import java.util.List;

/**
 * Representa um estado do mundo e as transicoes possiveis
 */
public interface Estado {

    /**
     * retorna uma descricao do problema que esta representacao
     * de estado resolve
     * @return String of description
     */
    public String getDescricao();
    
    /**
     * verifica se o estado e meta 
     * @return boolean if it is a goal state
     */
    public boolean ehMeta();


    /**
     * Custo para geracao deste estado
     * (nao e o custo acumulado --- g)
     * @return int representing cost
     */
    public int custo();
    

	/**
     * Custo acumulado g
     * @return int representing accumulated cost
     */
    public int custoAcumulado();


   /**
     * gera uma lista de sucessores do nodo.
     * @return list of states
     */
    public List<Estado> sucessores();

}
