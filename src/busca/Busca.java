package busca;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;



/**
 *   Algoritmos de Busca (geral, qquer problema)
 *
 *   @author Jomi Fred H�bner
 */
public abstract class Busca {

	protected boolean parar = false;
    protected boolean podar = true;
    protected boolean usarFechado = true;
    
    private int maxVisitados = -1;
    private int maxAbertos = -1;
    private long maxTempo = -1;

    protected Status status = new Status(); // a classe que tem o status (model)
    protected MostraStatusConsole mstatus = null; // a classe que mostra o stauts (view)

    private Map<Estado,Integer> fechados = null; // mapeia o estado para um custo g
    
    /** busca sem mostrar status */
    public Busca() {
    }

    /** busca mostrando status */
    public Busca(MostraStatusConsole ms) {
        setMostra(ms);
    }
    
    protected void initFechados() {
    	fechados = new HashMap<Estado,Integer>();
    }
    
    public Status getStatus() {
        return status;
    }
    
    public Status novoStatus() {
        status = new Status();
        if (mstatus != null) {
            mstatus.setStatus(status);
            status.setMostra(mstatus);
        }
        return status;
    }
    
    public void setMostra(MostraStatusConsole ms) {
        mstatus = ms;
        ms.setStatus(status);
        status.setMostra(ms);        
    }
    
    public String toString() {
    	return "Algoritmo de busca geral";
    }
    
    public abstract Nodo busca(Estado inicial) throws Exception;
    
    public void setParar(boolean b) {
        parar = b;
    }
    public void para() {
    	parar = true;
        status.termina(false);
    }

    public void setPodar(boolean b) {
    	podar = b;
    }
    
    public void usarFechados(boolean b) {
    	usarFechado = b;
    }
    
    /**
     * Numero maximo permitido de nodos Abertos
     */
    public int getMaxAbertos() {
    	return maxAbertos;
    }

    /**
     * Configura numero maximo de nodos Abertos -1 para desabilitar
     */
    public void setMaxAbertos(int ma) {
    	maxAbertos = ma;
    }

    /**
     * Numero maximo permitido de nodos Visitados
     */
    public int getMaxVisitados() {
    	return maxVisitados;
    }
    
    /**
     * Configura numero maximo de nodos Visitados -1 para desabilitar
     */
    public void setMaxVisitados(int mv) {
    	maxVisitados = mv;
    }
    
    /**
     * Numero maximo permitido de nodos Visitados
     */
    public long getMaxTempo() {
    	return maxTempo;
    }
    
    /**
     * Configura numero maximo de nodos Visitados -1 para desabilitar
     */
    public void setMaxTempo(long mt) {
    	maxTempo = mt;
    }

    /**
     * gera uma lista de sucessores do nodo.
     */
    public List<Nodo> sucessores(Nodo pai) {

    	if (maxVisitados > 0 && status.nroVisitados > maxVisitados)
        	para();

    	if (maxAbertos > 0 && status.tamAbertos > maxAbertos)
        	para();
    	
    	if (maxTempo > 0 && status.getTempoDecorrido() > maxTempo)
    		para();

    	return soNovos(pai.estado.sucessores(),pai); // lista de todos os sucessores
    }

    public List<Nodo> antecessores(Nodo pai) {
        try {
            return soNovos( ((Antecessor)pai.estado).antecessores(),pai);
        } catch (Exception e) {
            System.err.println("O estado "+pai.estado+" nao implementa antecessores!");
            return new LinkedList<Nodo>();
        }
    }
    

    private List<Nodo> soNovos(List<Estado> estados, Nodo pai) {
        List<Nodo> sucNodo   = new LinkedList<Nodo>(); // a lista de sucessores novos
        for (Estado e: estados) {
            Nodo filho = new Nodo( e, pai);
            if (podar) {
                if (usarFechado && fechados != null) {
                    Integer custo = fechados.get(e);
                    if (custo == null || filho.g < custo.intValue()) { // nao esta em fechados ou tem custo menor
                        sucNodo.add(filho);
                        fechados.put(e, filho.g);
                    }
                } else if (filho.ehDescendenteNovo(pai)) { // poda os filhos que tem um ascensor igual a ele
                    sucNodo.add(filho);
                }
            } else {
                sucNodo.add(filho);
            }
        }
        return sucNodo;
    }
}
