package organisation;

import java.io.File;

import busca.BuscaLargura;
import busca.BuscaProfundidade;
import busca.MostraStatusConsole;
import organisation.exception.GoalNotFound;
import organisation.goal.GoalTree;
import organisation.search.Organisation;
import organisation.search.cost.Cost;

public class OrganisationGenerator {
    Organisation inicial;

	public void generateOrganisationFromTree(String name, Cost c, String search, boolean oneSolution) {
		try {
			OrganisationStatistics s = OrganisationStatistics.getInstance();
			s.deleteExistingStatistics();
            s.prepareStatisticsFile(name);

            OrganisationPlot p = new OrganisationPlot();
			p.deleteExistingDiagrams();
			p.deleteExistingGraphs();

			GoalTree gTree = GoalTree.getInstance();
			p.saveDotAsPNG(name + "_original_gdt", p.plotGoalTree(name + "_original_gdt", gTree));
			
			gTree.brakeGoalTree();
			p.saveDotAsPNG(name + "_broken_gdt", p.plotGoalTree(name + "_broken_gdt", gTree));
			s.saveDataOfBrokenTree();
			
			inicial = new Organisation(name, gTree, c, oneSolution);
			System.out.println("\n\nEstimated number of states to visit (worst case): "
					+ inicial.getNumberOfOrganisationsEstimation());			

			if (search.equals("BFS")) {
				BuscaLargura busca = new BuscaLargura();
				MostraStatusConsole status = new MostraStatusConsole(busca.getStatus());
				busca.busca(inicial);
				status.para();
			}

			if (search.equals("DFS")) {
				BuscaProfundidade busca = new BuscaProfundidade();
				MostraStatusConsole status = new MostraStatusConsole(busca.getStatus());
				busca.busca(inicial);
				status.para();
			}
			
		} catch (GoalNotFound e) {
			e.printStackTrace();
		}
	}

    public void createOutPutFolders() {
        // create folders if does not exist	
		File file = new File("output/diagrams/tmp");
		file.getParentFile().mkdirs();
        file = new File("output/graphs/tmp");
        file.getParentFile().mkdirs();
        file = new File("output/statistics/tmp");
        file.getParentFile().mkdirs();
    }
}
