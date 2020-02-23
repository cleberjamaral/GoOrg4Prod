package organisation;

import busca.Nodo;
import busca.BuscaLargura;
import busca.BuscaProfundidade;
import organisation.exception.CircularReference;
import organisation.exception.GoalNotFound;
import organisation.goal.GoalNode;
import organisation.goal.GoalTree;
import organisation.search.Cost;
import organisation.search.Organisation;

public class OrganisationGenerator {
    Organisation inicial;

	public void generateOrganisationFromTree(String name, GoalTree gTree, Cost c, String search) {
		try {
			OrganisationPlot p = new OrganisationPlot();
			p.deleteExistingDiagrams();
			p.deleteExistingGraphs();
			p.saveDotAsPNG(name + "_original_gdt", p.plotGoalTree(name + "_original_gdt", gTree));
			gTree.brakeGoalTree();
			p.saveDotAsPNG(name + "_broken_gdt", p.plotGoalTree(name + "_broken_gdt", gTree));

			inicial = new Organisation(name, gTree, c);

			Nodo n = null;
			if (search.equals("BFS"))
				n = new BuscaLargura().busca(inicial);

			if (search.equals("DFS"))
				n = new BuscaProfundidade().busca(inicial);

			final String dot = p.plotOrganisation((Organisation) n.getEstado(), "");
			p.saveDotAsPNG(((Organisation) n.getEstado()).getOrgName(), dot);
		} catch (GoalNotFound e) {
			e.printStackTrace();
		}
	}

    public void sampleOrganisation(Cost c, String search) {
        try {
			GoalTree gTree = new GoalTree("g0");
			gTree.addGoal("g1", "g0");
			gTree.addInform("g1", "i1", "g0", 13.5);
			
            generateOrganisationFromTree("sample", gTree, c, search);
        } catch (CircularReference e) {
            e.printStackTrace();
        }
    }
}
