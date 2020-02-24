package organisation;

import java.io.File;

import busca.BuscaLargura;
import busca.BuscaProfundidade;
import organisation.exception.CircularReference;
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

    public void sampleOrganisation(Cost c, String search) {
        try {
			GoalTree gTree = GoalTree.getInstance();
			gTree.setRootNode("g0");
			gTree.addGoal("g1", "g0");
			gTree.addWorkload("g1", "w1", 4);
			gTree.addInform("g1", "i1", "g0", 4);
			
            generateOrganisationFromTree("sample", gTree, c, search);
        } catch (CircularReference e) {
            e.printStackTrace();
        }
    }

    public void createOutPutFolders() {
        // create folders if doesnt exist
		File file = new File("output/diagrams/tmp");
		file.getParentFile().mkdirs();
        file = new File("output/graphs/tmp");
        file.getParentFile().mkdirs();
        file = new File("output/proofs/tmp");
        file.getParentFile().mkdirs();
        file = new File("output/statistics/tmp");
        file.getParentFile().mkdirs();
    }
}
