package exemplos;

import java.util.LinkedList;
import java.util.List;
import busca.Antecessor;
import busca.Estado;

/**
 * Solucao para o problema dos misssionarios e canibais.
 *
 * @author (Malcus Otavio Quinoto Imhof & Daniel Dalcastagne - 5. Semestre Matutino - BCC)
 * @version (02/04/2004)
 */
public class MissionarioCanibal implements Estado, Antecessor{
    
    public String getDescricao() {
        return
        "Tres misssionarios e tres canibais estao a beira de um rio e dispoem de\n"+
        "um barco com capacidade para apenas duas pessoas. O problema e determinar\n"+
        "as tripulacoes de uma serie de travessias de maneira que todo o grupo passe\n"+
        "para o outro lado do rio, respeitada a condicao de que em momento algum os\n"+
        "canibais sejam mais numerosos do que os missionarios em uma das margens do rio.\n\n"+
        "Implementacao de Malcus Otavio Quinoto Imhof & Daniel Dalcastagne - 5. Semestre Matutino - BCC\n\n";
    }
    
    /** atributos do estado */
    final int missionario, canibal;
    
    /** lado do barco */
    final char barco;
    
    /** operacao que gerou o estado */
    final String op;
    
    /** Recebe a quantidade de canibais na margem esquerda*/
    public MissionarioCanibal(int m, int c,char b, String o) {
        missionario = m;
        canibal = c;
        barco = b;
        op = o;
    }
    
    public boolean ehMeta(){
    	return missionario == 0 && canibal == 0;
    }
    
    public int custo() {
        return 1;
    }
    
    /** Lista de sucessores */
    public List<Estado> sucessores(){
        List<Estado> suc = new LinkedList<Estado>(); //Lista de sucessores
        //Levar um missionario
        levar1m(suc);
        //levar dois missionarios
        levar2m(suc);
        //levar 1 missionario e 1 canibal
        levar1m1c(suc);
        //levar 1 canibal
        levar1c(suc);
        //levar 2 canibais
        levar2c(suc);
        
        //Retornar a lista de Sucessores
        return suc;
    }
    
    /** Lista de antecessores, para busca bidirecional */
    public List<Estado> antecessores(){
        return sucessores();
    }
    
    /** Verifica se o sucessor gerado e valido */
    public boolean ehValido(){
        if (missionario < canibal && missionario != 0)
            return false;
        if (3-missionario < 3-canibal && 3-missionario != 0)
            return false;
        return true;
    }
    
    /** Movimenta um missionario de uma margem a outra */
    public void levar1m(List<Estado> suc){
        //Se o barco estiver do lado esquerdo e houver pelo menos 1 mission�rio nesse lado
        if (barco == 'e' && missionario > 0){
            //Gera um sucessor
            MissionarioCanibal novo = new MissionarioCanibal(missionario-1,canibal,'d',"Levar 1 missionario para margem direita");
            //Verifica se o sucessor gerado � valido
            if (novo.ehValido())
                //Se for v�lido, adiciona na lista de sicessores
                suc.add(novo);
        } else {
            //Se o barco estiver do lado direto e Houver pelo menos 1 mission�rio neste lado
            if (barco == 'd' && missionario < 3) {
                //Gerar o sucessor
                MissionarioCanibal novo = new MissionarioCanibal(missionario+1,canibal,'e',"Levar 1 missionario para margem esquerda");
                //Verificar se ele � valido
                if (novo.ehValido())
                    //Adicionar na lista de sucessores
                    suc.add(novo);
            }
        }
    }
    
    /** Movimenta 2 missionarios de uma margem a outra */
    public void levar2m(List<Estado> suc){
        //Se o barco estiver do lado esquerdo e houver pelo menos 2 mission�rios deste lado
        if (barco == 'e' && missionario > 1){
            //Gerar o sucessor
            MissionarioCanibal novo = new MissionarioCanibal(missionario-2,canibal,'d',"Levar 2 missionarios para margem direita");
            //Verificar se ele � valido
            if (novo.ehValido())
                //Adicionar na lista de sucessores
                suc.add(novo);
        }else{
            //Se o barco estiver do lado direito e houver dois mission�rios deste lado
            if (barco == 'd' && missionario < 2){
                //Gerar sucessor
                MissionarioCanibal novo = new MissionarioCanibal(missionario+2,canibal,'e',"Levar 2 mission�rios para margem esquerda");
                //Verificar se ele � valido
                if (novo.ehValido())
                    //Adicionar na lista de sucessores
                    suc.add(novo);
            }
        }
    }
    
    /** Movimentar 1 mission�rio e 1 canibal */
    public void levar1m1c(List<Estado> suc){
        //Se o barco estiver do lado esquerdo e houver pelo menos um mission�rio e um canibal deste lado
        if (barco == 'e' && missionario > 0 && canibal > 0){
            //Gerar sucessor
            MissionarioCanibal novo = new MissionarioCanibal(missionario-1,canibal-1,'d',"Levar 1 missionario e 1 canibal para margem direita");
            //Verificar se ele � valido
            if (novo.ehValido())
                //Adicionar na lista de sucessores
                suc.add(novo);
        }else{
            //Se o barco estiver do lado direito e houver pelo menos um mission�rio e um canibal deste lado
            if (barco == 'd' && missionario < 3 && canibal <3){
                //Gerar Sucessor
                MissionarioCanibal novo = new MissionarioCanibal(missionario+1,canibal+1,'e',"Levar 1 missionario e 1 canibal para margem esquerda");
                //Verificar se ele � v�lido
                if (novo.ehValido())
                    //Adicionar na lista de sucessores
                    suc.add(novo);
            }
        }
    }
    
    /** Movimentar um canibal de uma margem a outra */
    public void levar1c(List<Estado> suc){
        //Se o barco estiver do lado esquero e houver pelo menos um canibal deste lado
        if (barco == 'e' && canibal > 0){
            //Gerar sucessor
            MissionarioCanibal novo = new MissionarioCanibal(missionario,canibal-1,'d',"Levar 1 canibal para margem direita");
            //Verificar se ele � v�lido
            if (novo.ehValido())
                //Adicionar na lista
                suc.add(novo);
        }else{
            //Se o barco estiver no lado diretio e houver pelo menos 1 canibal deste lado
            if (barco == 'd' && canibal < 3){
                //Gerar sucessor
                MissionarioCanibal novo = new MissionarioCanibal(missionario,canibal+1,'e',"Levar 1 canibal para margem esquerda");
                //Verificar se ele � v�lido
                if (novo.ehValido())
                    //Adicionar na lista de sucessores
                    suc.add(novo);
            }
        }
    }
    
    /** Levar 2 canibais de uma margem a outra */
    public void levar2c(List<Estado> suc){
        //Se o barco estiver do lado esquerdo e houver pelo menos dois canibais deste lado
        if (barco == 'e' && canibal > 1){
            //Gerar sucessor
            MissionarioCanibal novo = new MissionarioCanibal(missionario,canibal-2,'d',"Levar 2 canibais para margem direita");
            //Verificar se � v�lido
            if (novo.ehValido())
                //Adicionar na lista de sucessores
                suc.add(novo);
        }else{
            //Se o barco estiver do lado direto e houver pelo menos 2 canibais deste lado
            if (barco == 'd' && canibal < 2){
                //Gerar sucessor
                MissionarioCanibal novo = new MissionarioCanibal(missionario,canibal+2,'e',"Levar 2 canibais para margem esquerda");
                //Verificar se ele � v�lido
                if (novo.ehValido())
                    //Adicionar na lista
                    suc.add(novo);
            }
        }
    }
   
	/**
     * Custo acumulado g
     */
    public int custoAcumulado(){
		return 0;
    }
}
