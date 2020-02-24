package organisation;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Field;

import org.junit.Before;
import org.junit.Test;

import busca.BuscaLargura;
import organisation.exception.CircularReference;
import organisation.exception.GoalNotFound;
import organisation.goal.GoalTree;
import organisation.search.Cost;
import organisation.search.Organisation;
import organisation.search.Parameters;

public class CostGeneralistTest {

	@Before
	public void resetGoalTreeSingleton() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
	   Field instance = GoalTree.class.getDeclaredField("instance");
	   instance.setAccessible(true);
	   instance.set(null, null);
	}
	
    @Test
    public void testOneRoleGeneralistOrg() {
    	try {
    		System.out.println("\n\ntestOneRoleGeneralistOrg");

			// parameters
    		Parameters.getInstance();
    		Parameters.setMaxWorkload(8.0);
    		Parameters.setMaxDataLoad(8.0);
    		Parameters.setWorkloadGrain(2.0);
    		Parameters.setDataLoadGrain(2.0);
    		
        	GoalTree gTree = GoalTree.getInstance();
        	gTree.setRootNode("g0");
        	gTree.addGoal("g1", "g0");
        	gTree.addWorkload("g1", "w1", 4);
			gTree.addInform("g1", "i1", "g0", 4);
			System.out.println("The given goals tree: " + gTree.getTree());
			System.out.println("Originally the tree has 2 goals");
			assertEquals(2, gTree.getTree().size());

			//TODO: test the broken tree
			System.out.println("Before the search it must be called the process to brake goals.");
			/*gTree.brakeGoalTree();
			System.out.println("The broken goals tree: " + gTree.getTree());
			System.out.println("Asserting if the broken tree must have 4 goals");
			gTree.getTree().forEach(g -> {
				System.out.println(g.toString()); 
				System.out.println(g.getWorkloads()); 
				System.out.println(g.getInforms());
			});
			assertEquals(4, gTree.getTree().size());*/
			//TODO: assert if inform was removed since it is circular
			
			System.out.println("Since thw total workload is 4, which is less than 8 (max) all goals must be assigned to an unique role.");
			Organisation o = new Organisation("generalist", gTree, Cost.GENERALIST);
			new BuscaLargura().busca(o);

			System.out.println("g1 has a workload of 4 and granularity is 2, so g1 must be split into two parts.");
			System.out.println("g1 has to inform g0 of 4 units of data, with granularity of 2, g0 must be split into two parts.");

			assertEquals(1, o.getRolesTree().size());
    	} catch (CircularReference e) {
			e.printStackTrace();
		} catch (GoalNotFound e) {
			e.printStackTrace();
		}
    }
	
    
}
