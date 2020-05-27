package organisation;

import java.util.List;
import java.util.Set;

import busca.AEstrela;
import busca.BuscaLargura;
import busca.BuscaProfundidade;
import busca.MostraStatusConsole;
import busca.Nodo;
import organisation.exception.GoalNotFound;
import organisation.goal.GoalTree;
import organisation.search.Organisation;
import organisation.search.cost.Cost;
import simplelogger.SimpleLogger;

/**
 * @author cleber
 *
 */
public class OrganisationGenerator {
    Organisation inicial;
    private SimpleLogger LOG = SimpleLogger.getInstance();

	public Organisation generateOrganisationFromTree(String name, List<Cost> preferences, String search, boolean oneSolution) {
		try {
			OrganisationStatistics s = OrganisationStatistics.getInstance();
			s.deleteExistingStatistics();
            s.prepareGenerationStatisticsFile(name);

            OrganisationPlot p = new OrganisationPlot();
			p.deleteExistingDiagrams();
			p.deleteExistingGraphs();

			GoalTree gTree = GoalTree.getInstance();
			p.saveDotAsPNG(name + "_original_gdt", p.plotGoalTree(name + "_original_gdt", gTree));
			
			gTree.brakeGoalTree();
			p.saveDotAsPNG(name + "_broken_gdt", p.plotGoalTree(name + "_broken_gdt", gTree));
			s.saveDataOfBrokenTree();
			
			inicial = new Organisation(name, gTree, preferences, oneSolution);
			LOG.info("\n\nEstimated number of states to visit (worst case): "
					+ inicial.getEstimatedNumberOfOrganisations());

			Nodo n = null;
			if (search.equals("BFS")) {
				BuscaLargura busca = new BuscaLargura();
				MostraStatusConsole status = new MostraStatusConsole(busca.getStatus());
				n = busca.busca(inicial);
				status.para();
			}

			if (search.equals("DFS")) {
				BuscaProfundidade busca = new BuscaProfundidade();
				MostraStatusConsole status = new MostraStatusConsole(busca.getStatus());
				n = busca.busca(inicial);
				status.para();
			}
			
			if (search.equals("A*")) {
				AEstrela busca = new AEstrela();
				MostraStatusConsole status = new MostraStatusConsole(busca.getStatus());
				n = busca.busca(inicial);
				status.para();
			}

			// In case of multiple solutions n is null, initial state can be consulted to
			// get list of generated organisations
			if (n != null)
				return ((Organisation)n.getEstado());
			else
				return inicial;
		} catch (GoalNotFound e) {
			e.printStackTrace();
		}
		return null;
	}
}
