package organisation;

import java.io.File;

import busca.BuscaLargura;
import busca.BuscaProfundidade;
import organisation.exception.GoalNotFound;
import organisation.goal.GoalTree;
import organisation.search.Cost;
import organisation.search.Organisation;

public class OrganisationGenerator {
    Organisation inicial;

	public void generateOrganisationFromTree(String name, GoalTree gTree, Cost c, String search) {
		try {
			OrganisationStatistics s = OrganisationStatistics.getInstance();
			s.deleteExistingStatistics();
            s.prepareStatisticsFile(name);

            OrganisationPlot p = new OrganisationPlot();
			p.deleteExistingDiagrams();
			p.deleteExistingGraphs();
			p.saveDotAsPNG(name + "_original_gdt", p.plotGoalTree(name + "_original_gdt", gTree));
			s.saveDataOfGoalTree(gTree);
			
			gTree.brakeGoalTree();
			p.saveDotAsPNG(name + "_broken_gdt", p.plotGoalTree(name + "_broken_gdt", gTree));
			s.saveDataOfBrokenTree(gTree);

			inicial = new Organisation(name, gTree, c);

			if (search.equals("BFS"))
				new BuscaLargura().busca(inicial);

			if (search.equals("DFS"))
				new BuscaProfundidade().busca(inicial);
			
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
