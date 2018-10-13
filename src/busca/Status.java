package busca;

import java.util.Date;

/**
 * Contem varias informacoes de status sobre a busca
 * 
 * @author Jomi
 */
public class Status {

    int nroVisitados = 0;
    int profundidadeMax = 0; // a max prox. que a busca foi
    int tamAbertos = 0;
	int custoTotal;
    Date tempoInicio;
    MostraStatusConsole ms;
    boolean resolveu = false;

    void setMostra(MostraStatusConsole ms) {
        this.ms = ms;
    }
    
    void inicia() {
        nroVisitados = 0;
        profundidadeMax = 0;
        custoTotal = 0;
        tempoInicio = new Date();
    }

    public int getTamAbertos() {
		return tamAbertos;
	}

    void termina(boolean resolveu) {
    	//TODO: não deveria retornar resolveu?
        this.resolveu = true;
        if (ms != null) {
            ms.para();
        }
    }

    public boolean resolveu() {
        return resolveu;
    }
    
    public long getTempoDecorrido() {
        Date agora = new Date();
        return  agora.getTime() - tempoInicio.getTime();
    }
    
    public int getVisitados() {
        return nroVisitados;
    }
    
    public int getProfundidade() {
        return profundidadeMax;
    }
    
    public int getCustoTotal() {
        return custoTotal;
    }
    
    /** o algoritmo pegou n para explorar de um total de s */
    public boolean explorandoEhMeta(Nodo n, int s) {
        boolean ehMeta = false;
    	tamAbertos = s;
        nroVisitados++;
        
        
        //Obtenção do custo g da solução, não serve para BSM e BPI que não utiliza este método exploratório
        if (n.estado.ehMeta()) { 
        	custoTotal = n.estado.custoAcumulado();
        	ehMeta = true;
        }
        	
        if (n.getProfundidade() > profundidadeMax) {
            profundidadeMax = n.getProfundidade();
        }    
        return ehMeta;
    }

    /** o algoritmo pegou n para explorar de um total de s */
    public void explorando(Nodo n, int s) {
        tamAbertos = s;
        nroVisitados++;
        
        //Obtenção do custo g da solução, não serve para BSM e BPI que não utiliza este método exploratório
        if (n.estado.ehMeta()) { 
        	custoTotal = n.estado.custoAcumulado();
        }
        	
        if (n.getProfundidade() > profundidadeMax) {
            profundidadeMax = n.getProfundidade();
        }
    }
    
}
