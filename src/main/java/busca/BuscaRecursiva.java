package busca;



/**
 *   Algoritmos de Busca (geral, qquer problema)
 *
 *   Busca a solucao por busca em profundidade.
 *   Implementacao recursiva.
 *
 *   @author Jomi Fred Hubner
 */
public class BuscaRecursiva extends BuscaProfundidade {
     
    /** busca sem mostrar status */
    public BuscaRecursiva() {
    }
    
    /** 
     * busca mostrando status
     * @param ms shows console 
     */
    public BuscaRecursiva(MostraStatusConsole ms) {
        super(ms);
    }
    
    /**
     * Busca a solucao por busca em profundidade.
     *                              ------------
     *
     * Como o algoritmo e recursivo, nao precisa da lista de abertos.
     *
     * max e a profundidade maxima de busca.
     */
    public Nodo busca(Estado inicial) {
        status.inicia();
        initFechados();
        Nodo n = buscaR(new Nodo(inicial, null));
        status.termina(n != null);
        return n;
    }
    
    public Nodo buscaR(Nodo corrente) {
        if (corrente == null) {
            return null;
        }
        status.explorando(corrente, 0);
        if (corrente.estado.ehMeta()) {
            return corrente;
        }
        if (corrente.getProfundidade() > profMax || parar) {
            return null;
        }
        for (Nodo s: sucessores(corrente)) {
            Nodo n = buscaR(s);
            if (n != null) {
                return n;
            }
        }
        return null;
    }   
    
    
    public String toString() {
    	return "BP - Busca em Profundidade (m="+profMax+") - versao recursiva";
    }
}
