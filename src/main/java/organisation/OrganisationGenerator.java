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
			
			int openedStates = 0;
			int nStates = 0;
			for (int i = 0; i < 7; i++) {
				if (i == 0) {
					openedStates = 1;
				} else {
					openedStates = (openedStates * i) * 3;
				}
				nStates += openedStates;
			}
			
			System.out.println("\n\nEstimated number of states to visit:" + nStates);

			inicial = new Organisation(name, gTree, c, oneSolution);
			

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
