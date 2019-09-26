package busca;

import java.util.LinkedList;
import java.util.List;


/**
 *   Algoritmos de Busca (geral, qquer problema)
 *   Busca a solucao por busca em profundidade.
 *
 *   @author Jomi Fred Hubner
 */
public class BuscaProfundidade extends Busca {

    protected int profMax = 1000;

    /** busca sem mostrar status */
    public BuscaProfundidade() {
    }

    public BuscaProfundidade(int m) {
        profMax = m;
    }
    
    /** busca mostrando status */
    public BuscaProfundidade(MostraStatusConsole ms) {
        super(ms);
    }
    public BuscaProfundidade(int m, MostraStatusConsole ms) {
        super(ms);
        profMax = m;
    }
    
	
	public void setProfMax(int m) {
		profMax = m;
	}
	
    public Nodo busca(Estado inicial) {
        status.inicia();
        initFechados();
        
        List<Nodo> abertos = new LinkedList<Nodo>();
        
        abertos.add(new Nodo(inicial, null));
        
        while (!parar && abertos.size() > 0) {
            
            Nodo n = abertos.remove(0);
//            status.explorando(n,abertos.size());
//            if (n.estado.ehMeta()) {
            //Para evitar que ehMeta do Estado seja chamada duas vezes seguidas
            if (status.explorandoEhMeta(n,abertos.size())) {
                status.termina(true);
                return n;
            }
        
            if (n.getProfundidade() < profMax) {
                abertos.addAll( 0, sucessores(n) );
            } else {
            	status.termina(false);
            	return null;
            }
        }
        status.termina(false);
        return null;
    }        
    
    public String toString() {
    	return "BP - Busca em Profundidade";
    }
}
