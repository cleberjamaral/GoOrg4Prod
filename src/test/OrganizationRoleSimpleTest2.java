package test;

import busca.BuscaIterativo;
import busca.BuscaLargura;
import busca.BuscaProfundidade;
import busca.Nodo;
import organizational.OrganizationalRole2;
import organizational.GoalNode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class OrganizationRoleSimpleTest2 {
	
	static List<GoalNode> tree = new ArrayList<GoalNode>();
	static Stack<GoalNode> stack = new Stack<GoalNode>();
	static GoalNode rootNode = null;
	static boolean pushGoalNode = false;
	static GoalNode referenceGoalNode = null;

	public static void main(String[] a) throws IOException {

		
		// Sample organization
		GoalNode g0 = new GoalNode(null, "g0");
		GoalNode g1 = new GoalNode(g0, "g1");
		g1.addSkill("s1");
		GoalNode g2 = new GoalNode(g1, "g2");
		g2.addSkill("s2");
		GoalNode g3 = new GoalNode(g1, "g3");
		g3.addSkill("s3");
		GoalNode g4 = new GoalNode(g0, "g4");
		GoalNode g5 = new GoalNode(g4, "g5");
		g5.addSkill("s5");
		GoalNode g6 = new GoalNode(g4, "g6");
		g6.addSkill("s5");

		
/*
		// Sample organization : paint a house
		GoalNode paintHouse = new GoalNode(null, "paintHouse");
		GoalNode contracting = new GoalNode(paintHouse, "contracting");
		contracting.setOperator("parallel");
		contracting.addSkill("getBids");
		GoalNode bidIPaint = new GoalNode(contracting, "bidIPaint");
		bidIPaint.addSkill("bid");
		bidIPaint.addSkill("paint");
		GoalNode bidEPaint = new GoalNode(contracting, "bidEPaint");
		bidEPaint.addSkill("bid");
		bidEPaint.addSkill("paint");
		GoalNode execute = new GoalNode(paintHouse, "execute");
		GoalNode contractWinner = new GoalNode(execute, "contractWinner");
		contractWinner.addSkill("contract");
		GoalNode iPaint = new GoalNode(execute, "iPaint");
		iPaint.addSkill("bid");
		iPaint.addSkill("paint");
		GoalNode ePaint = new GoalNode(execute, "ePaint");
		ePaint.addSkill("paint");
*/		
		
		OrganizationalRole2 inicial = new OrganizationalRole2(g0,3);

		String str;
		BufferedReader teclado;
		teclado = new BufferedReader(new InputStreamReader(System.in));

		Nodo n = null;

		System.out.print("Digite sua opcao de busca { Digite S para finalizar }\n");
		System.out.print("\t1  -  Largura\n");
		System.out.print("\t2  -  Profundidade\n");
		System.out.print("\t3  -  Pronfundidade Iterativo\n");
		System.out.print("Opcao: ");
		str = teclado.readLine().toUpperCase();
		if (!str.equals("S")) {
			if (str.equals("1")) {
				System.out.println("Busca em Largura");
				n = new BuscaLargura().busca(inicial);
			} else {
				if (str.equals("2")) {
					System.out.println("Busca em Profundidade");
					n = new BuscaProfundidade(100).busca(inicial);
				} else {
					if (str.equals("3")) {
						System.out.println("Busca em Profundidade Iterativo");
						n = new BuscaIterativo().busca(inicial);
					}
				}
			}
			if (str.equals("1") || str.equals("2") || str.equals("3")) {
				if (n == null) {
					System.out.println("Sem Solucao!");
				} else {
					System.out.println("Solucao:\n" + n.montaCaminho() + "\n\n");
				}
			}
		}
	}
}
