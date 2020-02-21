package organisation;

import busca.Nodo;
import busca.BuscaLargura;
import busca.BuscaProfundidade;
import organisation.exception.CircularReference;
import organisation.goal.GoalNode;
import organisation.goal.GoalTree;
import organisation.search.Cost;
import organisation.search.Organisation;

public class OrganisationGenerator {
    Organisation inicial;

    public void generateOrganisationFromTree(GoalTree gTree, Cost c, String search) {
        OrganisationPlot p = new OrganisationPlot();
        p.deleteExistingDiagrams();
        p.deleteExistingGraphs();
        p.saveDotAsPNG("original_gdt", p.plotGoalTree("original_gdt", gTree));
        gTree.brakeGoalTree();
        p.saveDotAsPNG("broken_gdt", p.plotGoalTree("broken_gdt", gTree));

        inicial = new Organisation("orgApp", gTree, c);

        Nodo n = null;
        if (search.equals("BFS"))
            n = new BuscaLargura().busca(inicial);

        if (search.equals("DFS"))
            n = new BuscaProfundidade().busca(inicial);

        final String dot = p.plotOrganisation((Organisation) n.getEstado(), "");
        p.saveDotAsPNG(((Organisation) n.getEstado()).getOrgName(), dot);
    }

    public void generateOrganisationFromRoot(GoalNode rootNode, Cost c, String search) {
        GoalTree gTree = new GoalTree(rootNode);
        gTree.addAllDescendants(gTree.getRootNode());
        generateOrganisationFromTree(gTree, c, search);
    }

    public void createFullLinkAutomationGDT(Cost c, String search) {
        try {
            GoalTree gTree = new GoalTree("FullLink");
            gTree.addGoal("LoadConveyor", "FullLink");
            // gTree.addGoal("PickCrateFromReplenishment", "LoadConveyor", 1);
            // gTree.addWorkload("PickCrateFromReplenishment", "crate_lifting", 8);
            gTree.addGoal("MoveCrate", "LoadConveyor", 1);
            gTree.addWorkload("MoveCrate", "move_crate", 10);
            // gTree.addInform("PickCrateFromReplenishment", "crate_is_ready",
            // "MoveCrateToConveyor", 0.1);
            gTree.addGoal("PlaceItems", "LoadConveyor", 1);
            gTree.addWorkload("PlaceItems", "pnp", 4);
            gTree.addInform("MoveCrate", "loaded", "PlaceItems", 1.6);
            // gTree.addGoal("PutCrateBack", "FullLink");
            // gTree.addGoal("MoveCrateBackToReplenishment", "PutCrateBack");
            // gTree.addWorkload("MoveCrateBackToReplenishment", "crate_side_transferring",
            // 1);
            // gTree.addGoal("PlaceCrateOnReplenishment", "PutCrateBack");
            // gTree.addWorkload("PlaceCrateOnReplenishment", "crate_lifting", 1);

            /*
             * gTree.addGoal("LoadTruck", "FullLink"); gTree.addGoal("PickBoxFromConveyor",
             * "LoadTruck", 5); gTree.addWorkload("PickBoxFromConveyor", "unload_conveyor",
             * 8); gTree.addGoal("MoveBoxToDeliverySite", "LoadTruck", 4);
             * gTree.addWorkload("MoveBoxToDeliverySite", "box_side_transferring", 8);
             * gTree.addInform("PickBoxFromConveyor", "conveyor_unloaded",
             * "MoveBoxToDeliverySite", 0.8);
             * 
             */
            // gTree.addGoal("PlaceOrdersOnTheTruck", "LoadTruck", 1);
            // gTree.addWorkload("PlaceOrdersOnTheTruck", "load_truck", 8);
            // gTree.addInform("MoveBoxToDeliverySite", "box_on_site",
            // "PlaceOrdersOnTheTruck", 0.1);
            generateOrganisationFromTree(gTree, c, search);
        } catch (CircularReference e) {
            e.printStackTrace();
        }
    }
}
