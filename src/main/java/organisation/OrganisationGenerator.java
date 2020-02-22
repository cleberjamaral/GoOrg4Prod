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

	public void generateOrganisationFromTree(GoalTree gTree, Cost c, String search) {
		try {
			OrganisationPlot p = new OrganisationPlot();
			p.deleteExistingDiagrams();
			p.deleteExistingGraphs();
			p.saveDotAsPNG("original_gdt", p.plotGoalTree("original_gdt", gTree));
			gTree.brakeGoalTree();
			p.saveDotAsPNG("broken_gdt", p.plotGoalTree("broken_gdt", gTree));

			System.exit(0);

			inicial = new Organisation("orgApp", gTree, c);

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

    public void generateOrganisationFromRoot(GoalNode rootNode, Cost c, String search) {
        GoalTree gTree = new GoalTree(rootNode);
        gTree.addAllDescendants(gTree.getRootNode());
        generateOrganisationFromTree(gTree, c, search);
    }

    public void sampleOrganisation(Cost c, String search) {
        try {
			GoalTree gTree = new GoalTree("g0");
			gTree.addGoal("g1", "g0");
			gTree.addGoal("g2", "g1");
			gTree.addInform("g1", "i1", "g2", 13.5);
			
            generateOrganisationFromTree(gTree, c, search);
        } catch (CircularReference e) {
            e.printStackTrace();
        }
    }
}
