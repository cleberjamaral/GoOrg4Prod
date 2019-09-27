package busca;

import java.util.LinkedList;
import java.util.List;

/**
 * Busca Bi-direcional.
 *
 *  @author Jomi Fred Hubner
 */
public class BuscaBidirecional extends Busca {
    
    /** busca sem mostrar status */
    public BuscaBidirecional() {
    }

    /**
     * busca mostrando status
     * @param ms shows console
     */
    public BuscaBidirecional(MostraStatusConsole ms) {
        super(ms);
    }

    /**
     * Busca a solucao por busca em Bi-direcional.
     * @param inicial first state
     * @param meta a goal state
     * @return Node
     */
    public Nodo busca(Estado inicial, Estado meta) {
        status.inicia();
        usarFechado = false; // tem que usar poda so por ascendencia! nao pode usar fechados
        
        List<Nodo> abertosCima  = new LinkedList<Nodo>();
        List<Nodo> abertosBaixo = new LinkedList<Nodo>();
        
        abertosCima.add(new Nodo(inicial, null));
        Nodo nodoMeta = new Nodo(meta, null);
        abertosBaixo.add(nodoMeta);
        
        while (!parar && abertosCima.size() > 0 && abertosBaixo.size() > 0) {
            
            // incrementa em cima
            //
            Nodo n = abertosCima.remove(0);
            status.explorando(n, abertosCima.size()+abertosBaixo.size());
            // ve se tem n na borda da �rvore de baixo
            int io = abertosBaixo.indexOf(n); 
            if (io >= 0) {
                Nodo nb = abertosBaixo.get(io);
                nb.invertePaternidade();
                nb.pai = n.pai;
                nodoMeta.setProfundidade();
                status.termina(true);
                return nodoMeta;
            }
            abertosCima.addAll( sucessores(n) );
            
            // incrementa para baixo
            //
            n = abertosBaixo.remove(0);
            status.explorando(n, abertosCima.size()+abertosBaixo.size());
            // ve se tem n na borda da �rvore de cima
            io = abertosCima.indexOf(n);
            if (io >= 0) {
                Nodo nc = abertosCima.get(io);
                n.invertePaternidade();
                n.pai = nc.pai;
                nodoMeta.setProfundidade();
                status.termina(true);
                return nodoMeta;
            }
            abertosBaixo.addAll( antecessores(n) );
            
        }
        status.termina(false);
        return null;
    }

    
    public Nodo busca(Estado inicial) throws Exception {
    	throw new Exception("Esta classe nao implementa a busca com um unico parametro"); 
    }
    
    public String toString() {
    	return "BBD - busca bi-direcional";
    }
}
