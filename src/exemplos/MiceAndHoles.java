package exemplos;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import busca.Aleatorio;
import busca.Heuristica;
import busca.Estado;

/**
 * @author Cleber Jorge Amaral
 */
public class MiceAndHoles implements Estado, Heuristica, Aleatorio {

	public String getDescricao() {
		return "One day Masha came home and noticed n mice in the corridor of her flat.\n"
				+ "Of course, she shouted loudly, so scared mice started to run to the holes\n"
				+ " in the corridor.\n"
				+ "The corridor can be represeted as a numeric axis with n mice and m holes on it.\n"
				+ "ith mouse is at the coordinate xi, and jth hole - at coordinate pj. jth hole\n"
				+ "has enough room for cj mice, so not more than cj mice can enter this hole.\n"
				+ "What is the minimum sum of distances that mice have to go through so that\n"
				+ " they all can hide in the holes? If ith mouse goes to the hole j, then its\n"
				+ "distance is |xi-pj|.";
	}

	/** atributos do estado */
	List<Integer> micePosition = new ArrayList<Integer>(); // Posicao atual do rato
	List<Integer> holeCapacity = new ArrayList<Integer>(); // Capacidade atual dos buracos
	static List<Integer> origPosition = new ArrayList<Integer>(); // Usado em custo
	static List<Integer> homelessMice = new ArrayList<Integer>(); // Usado em heuristica
	static List<Integer> origCapacity = new ArrayList<Integer>(); // Usado em realocaces
	static List<Integer> virtCapacity = new ArrayList<Integer>(); // Usado em heuristica
	static List<Integer> shorterDistance = new ArrayList<Integer>(); // Usado em heuristica
	int ithMice = -1; // Comeca pelo -1 (a geracao de sucessores incrementa)
	int custoAcumulado = 0;
	static boolean debug = false;
	static boolean showMiceMap = false;
	/**
	 * 1: menor custo sem superlotar 
	 * 2: menor custo previsto 
	 * 3: custo unitario / heuristica desativada
	 * 4: menor numero de sucessores
	 * 5: a menor quantidade de ratos a serem alocados
	 */
	static int heuristicaAtiva = 2;

	/** Cria cenario inicial */
	public MiceAndHoles(int mP[], int hC[], int heuristica) {
		int maxMicePosition = 0;
		
		//Seta a heuristica que deve ser utilizada nos ensaios
		heuristicaAtiva = heuristica;

		// Cria lista de ratos
		if (debug)
			System.out.print("micePosition(MiceAndHoles): ");
		
		MiceAndHoles.origPosition.clear();
		for (int i = 0; i < mP.length; i++) {
			micePosition.add(mP[i]);
			MiceAndHoles.origPosition.add(mP[i]);
			if (mP[i] > maxMicePosition)
				maxMicePosition = mP[i];
			if (debug)
				System.out.print(micePosition.get(i) + " ");
		}

		MiceAndHoles.virtCapacity.clear();
		MiceAndHoles.origCapacity.clear();
		// Cria lista de buracos
		for (int i = 0; i < hC.length; i++){
			MiceAndHoles.origCapacity.add(hC[i]);
			holeCapacity.add(hC[i]);
			MiceAndHoles.virtCapacity.add(hC[i]);
		}
		//Cria buracos com capacidade zero caso haja ratos em posicoes inexistentes 
		for (int i = hC.length; i <= maxMicePosition; i++){
			MiceAndHoles.origCapacity.add(0);
			holeCapacity.add(0);
			MiceAndHoles.virtCapacity.add(0);
		}

		MiceAndHoles.shorterDistance.clear();
		// Subtrai a capacidade onde ha ratos e atualiza heuristica shorterDistance
		if (debug)
			System.out.print("shorterDistance(MiceAndHoles): ");
		for (int i = 0; i < micePosition.size(); i++) {
			holeCapacity.set(micePosition.get(i),
					holeCapacity.get(micePosition.get(i)) - 1);
			if (heuristicaAtiva == 1) {
				//Atualiza heuristica marcando o buraco mais proximo ao rato corrente
				int usedHole = -1;
				int closestDist = Integer.MAX_VALUE;
				for (int j = 0; j < MiceAndHoles.virtCapacity.size(); j++){
					if ((MiceAndHoles.virtCapacity.get(j) > 0) && (Math.abs(MiceAndHoles.origPosition.get(i) - j) < closestDist)){
						closestDist = Math.abs(MiceAndHoles.origPosition.get(i) - j);
						usedHole = j;
					}
				}
				virtCapacity.set(usedHole, virtCapacity.get(usedHole)-1);
				MiceAndHoles.shorterDistance.add(closestDist);
				if (debug)
					System.out.print(MiceAndHoles.shorterDistance.get(i) + " ");
			} else if (heuristicaAtiva == 2){
				//Atualiza heuristica marcando o buraco mais proximo ao rato corrente
				int closestDist = Integer.MAX_VALUE;
				for (int j = 0; j < holeCapacity.size(); j++){
					if ((holeCapacity.get(j) > 0) && (Math.abs(MiceAndHoles.origPosition.get(i) - j) < closestDist)){
						closestDist = Math.abs(MiceAndHoles.origPosition.get(i) - j);
					}
				}
				MiceAndHoles.shorterDistance.add(closestDist);
				if (debug)
					System.out.print(MiceAndHoles.shorterDistance.get(i) + " ");
			} else if (heuristicaAtiva == 5) {
				//Para cada rato cria um item do vetor homelessMice dizendo se é ou não um "sem teto"
				if (holeCapacity.get(micePosition.get(i)) >= 0)
					homelessMice.add(0);
				else
					homelessMice.add(1);
			}

		}
		// Imprime mapa de capacidade atualizado
		if (debug) {
			System.out.print("origCapacity(MiceAndHoles): ");
			for (int i = 0; i < origCapacity.size(); i++)
				System.out.print(origCapacity.get(i) + " ");
			System.out.print("\n");
			System.out.print("holeCapacity(MiceAndHoles): ");
			for (int i = 0; i < holeCapacity.size(); i++)
				System.out.print(holeCapacity.get(i) + " ");
			System.out.print("\n");
		}

	}

	public boolean ehMeta() {

		//Gambiarra isso estar aqui, deveria estar em outro local do codigo
		calculaCustoAcumuladoG();

		// Se algum buraco estiver superlotado, nao e meta!
		for (int i = 0; i < holeCapacity.size(); i++) {
			if (holeCapacity.get(i) < 0) {
				return false;
			}
		}

		return true;
	}
	
    /**
     * Custo para geracao deste estado
     * (nao e o custo acumulado --- g)
     */
	public int custo() {
		//3: Custo unitario, sem heuristica ou usado na heuristica de quantos ratos falta alocar
		if ((heuristicaAtiva == 3) || (heuristicaAtiva == 5))
		{
			return 1;
		} 
		//4: numero de sucessores
		else if (heuristicaAtiva == 4)
		{
			return this.sucessores().size();
		}
		else
		{
			// Custo REAL: deslocamento do rato 0 ao ith
			// Nao calcula se for a posicao -1 inicial
			if (this.ithMice >= 0)
				return Math.abs(this.micePosition.get(this.ithMice)
						- MiceAndHoles.origPosition.get(this.ithMice));
			else
				return 0;
		}
	}

	/** Lista de sucessores */
	public List<Estado> sucessores() {

		List<Estado> suc = new LinkedList<Estado>(); // Lista de sucessores


		//System.out.println("\nMice: "+ithMice);
		int novoIthMice = this.ithMice + 1;

		//Nao deve alcancar profundidade alem da quantidade de ratos
		if (novoIthMice >= micePosition.size()) {
			return suc;
		}
		
		//Cria sucessores em buffer circular sem repetir a posição atual (do atual ao fim)
		for (int i = 0; i < holeCapacity.size(); i++) {
			MiceAndHoles novo = new MiceAndHoles(this);

			// Para o rato selecionado, crie sucessores para as possiveis posicoes
			novo.ithMice = novoIthMice;
			// Movimenta rato da copia
			novo.micePosition.set(novo.ithMice, i);

			// Aloca buracos para a configuracao desta copia
			for (int j = 0; j < novo.micePosition.size(); j++) {
				novo.holeCapacity.set(novo.micePosition.get(j),
						novo.holeCapacity.get(novo.micePosition.get(j)) - 1);

				if (debug) {
					System.out
							.print("\nSucessores para mice(" + novo.ithMice + ") de [");
					for (int k = 0; k < micePosition.size(); k++)
						System.out.print(micePosition.get(k) + " ");
					System.out.print("]\n");
				}
			}

			if (!novo.poda()) {
				if (debug)	System.out.println("Adicionando nodo referente ao rato "+novo.ithMice+
						" ocupando a posicao "+novo.micePosition.get(novo.ithMice));

				suc.add(novo);
				
			} 
		}
		// Retornar a lista de Sucessores
		return suc;
	}

	/**
	 * cria um estado inicial a partir de outro (copia)
	 */
	MiceAndHoles(MiceAndHoles modelo) {

		//Cria vetores de ratos e menores distancias copiando do nodo pai
		for (int i = 0; i < modelo.micePosition.size(); i++) {
			micePosition.add(modelo.micePosition.get(i));
		}
		//Cria vetor de buracos com a capacidade original (sem nenhuma alocacao)
		for (int i = 0; i < modelo.holeCapacity.size(); i++){
			holeCapacity.add(MiceAndHoles.origCapacity.get(i));
		}
	}

	public String toString() {
		String r = "";

		if (showMiceMap) {
			// Forma de impressao do relatorio 1
			r += "\t[" + ithMice + "/" + (micePosition.size()-1) + "] \n";
			r +=  "Orig position: ";
			for (int j = 0; j < MiceAndHoles.origPosition.size(); j++)
				r += MiceAndHoles.origPosition.get(j) + " ";
			r += "\n";
			r +=  "Mice position: ";
			for (int j = 0; j < micePosition.size(); j++)
				r += micePosition.get(j) + " ";
			r += "\n";
			r += "Orig Capacity: ";
			for (int j = 0; j < MiceAndHoles.origCapacity.size(); j++)
				r += MiceAndHoles.origCapacity.get(j) + " ";
			r += "\n";
			r += "Hole Capacity: ";
			for (int j = 0; j < holeCapacity.size(); j++)
				r += holeCapacity.get(j) + " ";
			r += "\n";
			r += "Individ. cost: ";
			for (int j = 0; j < micePosition.size(); j++)
				r += Math.abs(micePosition.get(j) - MiceAndHoles.origPosition.get(j)) + " ";
			r += "\n";
		}
		
		if (showMiceMap) {
			// Forma de impressao do relatorio 2
			for (int j = 0; j < holeCapacity.size(); j++) {
				r += "\tHole(" + j + "), capacity: " + holeCapacity.get(j)
						+ " ";
				for (int i = 0; i < micePosition.size(); i++)
					if (micePosition.get(i) == j)
						r += "Mice(" + i + ") ";
				r += "\n";
			}
		}
		if (showMiceMap){
			r += "\t[" + ithMice + "/" + (micePosition.size()-1) +
					"] Custo total heuristica("+heuristicaAtiva+"): " + custoAcumulado() + "\n";
			System.out.print(r);
		}

		return r;
	}

	private void calculaCustoAcumuladoG() {
		//Calcula custo g ate o rato atual.
		int calculaCustoAcc = 0;
		for (int j = 0; j < micePosition.size(); j++)
			calculaCustoAcc += Math.abs(micePosition.get(j) - MiceAndHoles.origPosition.get(j));
		custoAcumulado = calculaCustoAcc;
	}

	/**
	 * Verifica se um estado eh igual a outro ja inserido na lista de sucessores
	 * (usado para poda)
	 */
	public boolean equals(Object o) {
		try {
			if (o instanceof MiceAndHoles) {
				MiceAndHoles e = (MiceAndHoles) o;
				//Se qualquer rato estiver numa posicao diferente este estado e diferente
				if (e.ithMice != this.ithMice)
					return false;
				for (int i = 0; i < this.micePosition.size(); i++)
					if (this.micePosition.get(i) != e.micePosition.get(i))
						return false;
				return true;
			}
			return false;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * retorna o hashCode desse estado (usado para poda, conjunto de fechados)
	 */

	public int hashCode() {
		String r = ithMice + " ";
		for (int j = 0; j < micePosition.size(); j++) r += micePosition.get(j) + " ";

		return r.hashCode();
	}

	public int h() {
		//1: Custo de cada de chegar no buraco mais proximo penalizando em caso de superlotacao (com alocação virtual)
		int estimativa = 0;
		if (heuristicaAtiva == 1){
			for (int i = ithMice+1; i < MiceAndHoles.shorterDistance.size(); i++) {
				estimativa += MiceAndHoles.shorterDistance.get(i);
			}
		} 
		//2: Custo de cada rato nao computado de chegar ao buraco mais proximo (estranho pois é fixa!)
		else if (heuristicaAtiva == 2)
		{
			for (int i = ithMice+1; i < MiceAndHoles.shorterDistance.size(); i++) {
				estimativa += MiceAndHoles.shorterDistance.get(i);
			}
		}
		//3: Custo unitario, sem heuristica
		else if (heuristicaAtiva == 3)
		{
			estimativa = 0;
		}
		//4: Baseado na quantidade de sucessores
		else if (heuristicaAtiva == 4)
		{
			//Esta heuristica estima a quantidade de sucessores pela quantidade de buracoes unicos
			for (int j = holeCapacity.size()-1; j > ithMice; j--){
				if (holeCapacity.get(j) > 0) {
					estimativa++;
				}
			}
		}
		//5: Baseado em quantos ratos falta alocar
		else if (heuristicaAtiva == 5)
		{
			//Esta heuristica estima quantos ratos falta alocar
			for (int j = ithMice+1; j < homelessMice.size(); j++){
				if (homelessMice.get(j) > 0) estimativa++;
			}
		}

		return estimativa;
	}

	/**
	 * retorna true se o estado deve ser podado
	 */
	protected boolean poda() {
		// Se o buraco que este rato esta tentando usar 
		// estiver superlotado, pode podar!
		if (holeCapacity.get(micePosition.get(ithMice)) < 0) {
			return true;
		}
		return false;
	}

	public Estado geraAleatorio() {
		MiceAndHoles aleatorio = new MiceAndHoles(this);

		// Posiciona ratos aleatoriamente
		for (int i = 0; i < aleatorio.micePosition.size(); i++)
			aleatorio.micePosition.set(i,
					Math.round((float) (Math.random() * (aleatorio.holeCapacity
							.size() - 1))));

		if (debug) {
			System.out.print("\nNovo posicionamento aleatorio: ");
			for (int i = 0; i < aleatorio.micePosition.size(); i++)
				System.out.print(aleatorio.micePosition.get(i) + " ");
			System.out.print("\n");
		}

		// Aloca buracos (serve apenas para exibição)
		for (int j = 0; j < aleatorio.micePosition.size(); j++) {
			aleatorio.holeCapacity.set(aleatorio.micePosition.get(j),
					aleatorio.holeCapacity.get(aleatorio.micePosition.get(j)) - 1);
		}

		// Atualiza o ithMice ate o j rato ja corretamente posicionado
		int j = 0; 
		for (; j < aleatorio.micePosition.size(); j++) 
			if (aleatorio.holeCapacity.get(aleatorio.micePosition.get(j)) < 0) 
				break;
		aleatorio.ithMice = j-1; // Comeca sempre do ithRato -1
		
		if (debug)
			System.out.println("Gerado aleatório do ithMice: "+aleatorio.ithMice);
		
		aleatorio.calculaCustoAcumuladoG();

		return aleatorio;
	}

	/**
     * Custo acumulado g
     */
    public int custoAcumulado(){
		return custoAcumulado;
    	
    }
	
}


