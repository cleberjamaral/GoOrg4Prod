package busca;




/**
 *   Algoritmos de Busca (geral, qquer problema)
 *
 *   Busca a solucao por busca em profundidade iterativo.
 *
 *   @author Jomi Fred Hubner
 */
public class BuscaIterativo extends BuscaProfundidade {

    private Status status = new Status();
    
    /** busca sem mostrar status */
    public BuscaIterativo() {
    }
    
    /** busca mostrando status */
    public BuscaIterativo(MostraStatusConsole ms) {
        setMostra(ms);
    }
       
    public void setMostra(MostraStatusConsole ms) {
        mstatus = ms;
        ms.setStatus(status);
        status.setMostra(ms);        
    }

    public Status novoStatus() {
        status = new Status();
        if (mstatus != null) {
            mstatus.setStatus(status);
            status.setMostra(mstatus);
        }
        return status;
    }
    

    /**
     *
     * Busca a solucao por busca em profundidade iterativo.
     *                              ----------------------
     *
     */
    public Nodo busca(Estado inicial) {
        status.inicia();
        initFechados();

        int prof = 0;
        while (!parar) {
            status.profundidadeMax = prof;
            setProfMax(prof++); // indica a profundidade maxima atual
            Nodo n = super.busca(inicial); 
            status.nroVisitados += super.status.nroVisitados; // acumula das varias buscas em profundidade

        	if (getMaxVisitados() > 0 && status.nroVisitados > getMaxVisitados())
            	para();

        	if (getMaxAbertos() > 0 && status.tamAbertos > getMaxAbertos())
            	para();
        	
        	if (getMaxTempo() > 0 && status.getTempoDecorrido() > getMaxTempo())
        		para();
        	
        	if (n != null) {

                //Obtenção do custo g da solução
                if (n.estado.ehMeta()) { 
                	status.custoTotal = n.estado.custoAcumulado();
                }
            	
            	status.termina(true);
                return n;
            }
        }

        status.termina(false);
        return null;
    }    
    
    public String toString() {
    	return "BPI - busca em profundidade iterativo";
    }
}
