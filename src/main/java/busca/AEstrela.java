package busca;

import java.util.PriorityQueue;
import java.util.Queue;

/**
 *   Algoritmos de Busca A*
 *
 *   @author Jomi Fred Hubner
 */
public class AEstrela extends BuscaHeuristica {
    
	int maxF = -1; // max F
	Nodo theBest;

    /** 
     * busca sem mostrar status 
     */
    public AEstrela() {
    }
    
    /**
     * busca mostrando status
     * @param ms shows status on console
     */
    public AEstrela(MostraStatusConsole ms) {
        super(ms);
    }
	
	/**
	 * Return the best node
	 * @return best node
	 */
	public Nodo getTheBest() {
		return theBest;
	}
	
    /**
     *
     * Busca a solucao por busca em heuristica.
     *                              ----------
     * (baseado no Russel and Norvig)
     */
    public Nodo busca(Estado inicial) {
        status.inicia();
        initFechados();
        
        Queue<Nodo> abertos = new PriorityQueue<Nodo>(100, getNodoComparatorF()); // lista ordenada por f()
        Nodo nInicial = new Nodo(inicial, null);
        abertos.add(nInicial);
        theBest = nInicial; // o melhor nodo ja gerado
        
        while (!parar && abertos.size() > 0) {
            
            Nodo melhor = abertos.remove();
            status.explorando(melhor, abertos.size());
            if (melhor.estado.ehMeta()) {
                
                status.termina(true);
                return melhor;
            }
            
            if (maxF < 0 || melhor.f() < maxF) {
                abertos.addAll( sucessores(melhor) );
            }
            if (getMaxAbertos() > 0 && abertos.size() > getMaxAbertos()) {
                break;
            }
            
            // o "the best" e o codigo que segue so para fins de interface
            if (melhor.f() < theBest.f()) {
                theBest = melhor;
            }
            
        }
        status.termina(false);
        return null;
    }
    
    public String toString() {
    	return "A* - busca heuristica"; 
    }
    
}
