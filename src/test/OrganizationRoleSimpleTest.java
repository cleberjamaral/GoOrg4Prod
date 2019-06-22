package test;

import busca.BuscaIterativo;
import busca.BuscaLargura;
import busca.BuscaProfundidade;
import busca.Nodo;
import organizational.OrganizationalRole;
import organizational.GoalNode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class OrganizationRoleSimpleTest {
	
	static List<GoalNode> tree = new ArrayList<GoalNode>();
	static Stack<GoalNode> stack = new Stack<GoalNode>();
	static GoalNode rootNode = null;
	static boolean pushGoalNode = false;
	static GoalNode referenceGoalNode = null;

	public static void main(String[] a) throws IOException {

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

		OrganizationalRole inicial = new OrganizationalRole(paintHouse,3);

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