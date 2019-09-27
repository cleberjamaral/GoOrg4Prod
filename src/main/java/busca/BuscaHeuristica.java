package busca;

import java.util.Comparator;

/**
 *   Algoritmos de Busca heuristica
 *
 *   @author Jomi Fred Hubner
 */
public abstract class BuscaHeuristica extends Busca {
    
    /** busca sem mostrar status */
    public BuscaHeuristica() {
    }
    
    /**
     * busca mostrando status
     * @param ms shows console
     */
    public BuscaHeuristica(MostraStatusConsole ms) {
        super(ms);
    }

    /**
     * comparador para ordenar os nodos por F
     * @return Node
     */
    Comparator<Nodo> getNodoComparatorF() {
        return new Comparator<Nodo>() {
            public int compare(Nodo no1, Nodo no2) {
                try {
                    //Heuristica eo1 = (Heuristica)no1.estado;
                    //Heuristica eo2 = (Heuristica)no2.estado;
                    int f1 = no1.f();
                    int f2 = no2.f();
                    if (f1 > f2) {
                        return 1;
                    } else if (f1 == f2) {
                        return 0; 
                    } else {
                        return -1;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return 0;
            }
        };
    }
}
