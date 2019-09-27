package busca;


/**
 * Representa um nodo da Arvore de busca
 */
public class Nodo implements Comparable<Object> {
    
    Estado estado;  // o estado
    Nodo   pai;     // o pai
    int    profundidade = 0;
    int    g = 0; // custo de ter gerado o nodo (todo o caminho)
    int    f = -1; // f = g + h
    
    public Nodo(Estado e, Nodo p) {
        estado = e;
        pai = p;
        if (p == null) {
            profundidade = 0;
            g = e.custo();
        } else {
            profundidade = p.getProfundidade() + 1;
            g = e.custo() + p.g;
        }
    }
    
    public int getProfundidade() {
        return profundidade;
    }
    
    public Estado getEstado() {
    	return estado;
    }
    
    public Nodo getPai() {
    	return pai;
    }
    
    /** retorna o custo acumulado de gerar o nodo 
     *  (baseado no acumulo do custo de gerar os estados)
     * @return int representing cost
     */
    public int g() {
        return g;
    }
    
    /**
     * Custo total
     * @return integer representing cost + heuristic
     */
    public int f() {
        if (f == -1) {
            f = g + ((Heuristica)estado).h();
        }
        return f;
    }

    void invertePaternidade() {
        if (pai.pai != null) {
            pai.invertePaternidade();
        }
        pai.pai = this;
    }
    
    /**
     * arruma a profundidade de um nodo e de seus pais
     */
    void setProfundidade() {
        if (pai == null) {
            profundidade = 0;
        } else {
            pai.setProfundidade();
            profundidade = pai.getProfundidade() + 1;
        }
    }
    
    
    /**
     * testa se o nodo nAo tem um ascensor igual a ele
     * (se um dos pais eh igual a ele)
     * @param ascensor is a node to check if this current one is descending to
     * @return true if it is
     */
    boolean ehDescendenteNovo(Nodo ascensor) {
        if (ascensor == null) {
            return true;
        } else {
            if (ascensor.estado.equals(this.estado)) {
                return false;
            } else {
                return ehDescendenteNovo(ascensor.pai);
            }
        }
    }
    
    /**
     * se dois nodos sAo iguais
     * (por enquato, sO verifica se os estados sAo iguais --
     *  usado no bi-direcional)
     */
    public boolean equals(Object o) {
        try {
            Nodo n = (Nodo)o;
            return this.estado.equals(n.estado);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /** utiliza o custo (g) como elemento de ordenaCAo */
    public int compareTo(Object obj) {
        try {
            Nodo outro = (Nodo)obj;
            if (g > outro.g) {
                return 1; // sou maior (fica depois na fila)
            } else if (g == outro.g) {
                return 0; // sou =
            } else {
                return -1; // sou menor
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return 0; // sou igual
    }
    
    /**
     *  imprime o caminho atE a raIz
     * @return String of the path
     */
    public String montaCaminho() {
        return montaCaminho(this);
    }
    
    public String montaCaminho(Nodo n) {
        if (n != null) {
            return montaCaminho(n.pai) + n + "; ";
        }
        return "";
    }
    
    public String toString() {
        return estado.toString();
    }
}
