package organisation;

import busca.BuscaLargura;
import busca.BuscaProfundidade;
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
		
		if (search.equals("BFS")) 
			new BuscaLargura().busca(inicial);
		
		if (search.equals("DFS")) 
			new BuscaProfundidade().busca(inicial);
    }
    
    public void generateOrganisationFromRoot(GoalNode rootNode, Cost c, String search) {
    	GoalTree gTree = null;
    	gTree = new GoalTree(rootNode);
    	gTree.addAllDescendants(gTree.getRootNode());
    	generateOrganisationFromTree(gTree, c, search);
    }
    
	public void createFullLinkAutomationGDT(Cost c, String search) {
		GoalTree gTree = new GoalTree("FullLink");
		gTree.addGoal("LoadConveyorBelt", "FullLink");
		//gTree.addGoal("PickCrateFromReplenishment", "LoadConveyorBelt", 1);
		//gTree.addWorkload("PickCrateFromReplenishment", "crate_lifting", 8);
		gTree.addGoal("MoveCrateToConveyor", "LoadConveyorBelt", 1);
		gTree.addWorkload("MoveCrateToConveyor", "crate_side_transferring", 10);
		//gTree.addInform("PickCrateFromReplenishment", "crate_is_ready", "MoveCrateToConveyor", 0.1);
		gTree.addGoal("PlaceOrdersOnConveyor", "LoadConveyorBelt", 1);
		gTree.addWorkload("PlaceOrdersOnConveyor", "load_conveyor", 4);
		gTree.addInform("MoveCrateToConveyor", "conveyor_loaded", "PlaceOrdersOnConveyor", 1.6);
//		gTree.addGoal("PutCrateBack", "FullLink");
//		gTree.addGoal("MoveCrateBackToReplenishment", "PutCrateBack");
//		gTree.addWorkload("MoveCrateBackToReplenishment", "crate_side_transferring", 1);
//		gTree.addGoal("PlaceCrateOnReplenishment", "PutCrateBack");
//		gTree.addWorkload("PlaceCrateOnReplenishment", "crate_lifting", 1);
		
/*		
		gTree.addGoal("LoadTruck", "FullLink");
		gTree.addGoal("PickBoxFromConveyor", "LoadTruck", 5);
		gTree.addWorkload("PickBoxFromConveyor", "unload_conveyor", 8);
		gTree.addGoal("MoveBoxToDeliverySite", "LoadTruck", 4);
		gTree.addWorkload("MoveBoxToDeliverySite", "box_side_transferring", 8);
		gTree.addInform("PickBoxFromConveyor", "conveyor_unloaded", "MoveBoxToDeliverySite", 0.8);
		
*/		
		//gTree.addGoal("PlaceOrdersOnTheTruck", "LoadTruck", 1);
		//gTree.addWorkload("PlaceOrdersOnTheTruck", "load_truck", 8);
		//gTree.addInform("MoveBoxToDeliverySite", "box_on_site", "PlaceOrdersOnTheTruck", 0.1);
		generateOrganisationFromTree(gTree, c, search);
	}
/*	
	private GoalTree createPaintHouseGDT() {
		GoalTree gTree = new GoalTree("PaintHouse");
		gTree.addGoal("GetInputs", "PaintHouse");
		gTree.addWorkload("GetInputs", "Contract", 2);
		gTree.addGoal("Paint", "PaintHouse");
		gTree.addGoal("BuyInputs", "GetInputs");
		gTree.addWorkload("BuyInputs", "purchase", 7);
		gTree.addWorkload("BuyInputs","messages",10);
		gTree.addInform("BuyInputs", "reports", "GetInputs", 2);
		gTree.addInform("BuyInputs", "registerSuppliers", "GetInputs", 2);
		gTree.addGoal("GetScaffold", "GetInputs");
		gTree.addWorkload("GetScaffold", "purchase", 7);
		gTree.addWorkload("GetScaffold","messages",10);
		gTree.addInform("GetScaffold", "reports", "GetInputs", 2);
		gTree.addGoal("Inspect", "PaintHouse");
		gTree.addWorkload("Inspect", "inspection", 8);
		gTree.addGoal("Financial", "PaintHouse");
		gTree.addGoal("GetPayment", "Financial");
		gTree.addWorkload("GetPayment", "billing", 8);
		gTree.addGoal("Report", "Financial");
		gTree.addWorkload("Report", "calculus", 8);
		gTree.addGoal("PaintInt", "Paint");
		gTree.addWorkload("PaintInt", "paint", 8);
		gTree.addGoal("PaintExt", "Paint");
		gTree.addWorkload("PaintExt", "paint", 2);
		gTree.addWorkload("PaintExt", "scaffold", 3);
		return gTree;
	}

	private GoalTree createRetrieveInfoGDT() {
		GoalTree gTree = new GoalTree("RetrieveInformation");
		gTree.addGoal("RetrieveInformation", "mediate_communication");
		gTree.addGoal("SplitIntoSubtopics", "RetrieveInformation");
		gTree.addWorkload("SplitIntoSubtopics", "split_search", 1);
		gTree.addGoal("SearchSubtopic", "RetrieveInformation");
		gTree.addWorkload("SearchSubtopic", "store_partial_result", 1);
		gTree.addWorkload("SearchSubtopic", "forward_unknown_subtopic", 1);
		gTree.addGoal("CompileAnswer", "RetrieveInformation");
		gTree.addWorkload("CompileAnswer", "join_partial_results", 1);
		gTree.addWorkload("CompileAnswer", "check_consistency", 1);
		gTree.addWorkload("CompileAnswer", "compile_result", 1);
		gTree.addGoal("ReplyUser", "RetrieveInformation");
		gTree.addWorkload("ReplyUser", "reply_user", 1);
		return gTree;
	}
*/
}
