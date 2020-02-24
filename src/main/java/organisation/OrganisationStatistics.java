package organisation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.io.FileUtils;

import organisation.goal.GoalNode;
import organisation.goal.GoalTree;
import organisation.role.RoleNode;
import organisation.search.Organisation;

public class OrganisationStatistics {
	
	private static OrganisationStatistics instance = null;
	
	int numberOfGoals = 0;
	int numberOfWorkloads = 0;
	int numberOfInforms = 0;
	int numberOfBrokenGoals = 0;
	int numberOfBrokenWorkloads = 0;
	int numberOfBrokenInforms = 0;
	double sumEfforts = 0.0;
	List<String> fields = new ArrayList<>();
	
	public static OrganisationStatistics getInstance() 
    { 
        if (instance == null) 
        	instance = new OrganisationStatistics(); 
  
        return instance; 
    } 
	
	private OrganisationStatistics() {
		this.fields.add("Goals");
		this.fields.add("Workloads");
		this.fields.add("Informs");
		this.fields.add("bGoals");
		this.fields.add("bWorkloads");
		this.fields.add("bInforms");
		this.fields.add("sumEfforts");
		this.fields.add("rGoals");
		this.fields.add("rWorkloads");
		this.fields.add("rInforms");
	}
	
	public void prepareStatisticsFile(final String orgName) {
		createOutPutFolders();

		try (FileWriter fw = new FileWriter("output/statistics/" + orgName + ".csv", false);
				BufferedWriter bw = new BufferedWriter(fw);
				PrintWriter out = new PrintWriter(bw)) {
			out.print(StringUtils.join(this.fields, ",\t"));
	
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void saveOnStatistics(final Organisation o) {
		try (FileWriter fw = new FileWriter("output/statistics/" + o.getOrgName() + ".csv", true);
				BufferedWriter bw = new BufferedWriter(fw);
				PrintWriter out = new PrintWriter(bw)) {
			
			Map<String,String> line = new HashMap<>();
			
			int numberOfAssignedGoals = 0;
			int numberOfAssignedWorkloads = 0;
			int numberOfAssignedInforms = 0;

			for (final RoleNode or : o.getRolesTree().getTree()) {
				numberOfAssignedGoals += or.getAssignedGoals().size();
				numberOfAssignedWorkloads += or.getWorkloads().size();
				numberOfAssignedInforms += or.getInforms().size();
			}
			
			line.put("Goals", (Integer.toString(numberOfGoals)));
			line.put("Workloads", (Integer.toString(numberOfWorkloads)));
			line.put("Informs", (Integer.toString(numberOfInforms)));
			line.put("bGoals", (Integer.toString(numberOfBrokenGoals)));
			line.put("bWorkloads", (Integer.toString(numberOfBrokenWorkloads)));
			line.put("bInforms", (Integer.toString(numberOfBrokenInforms)));
			line.put("sumEfforts", (Double.toString(sumEfforts)));
			line.put("rGoals", (Integer.toString(numberOfAssignedGoals)));
			line.put("rWorkloads", (Integer.toString(numberOfAssignedWorkloads)));
			line.put("rInforms", (Integer.toString(numberOfAssignedInforms)));
			line.put("Roles", (Integer.toString(o.getRolesTree().getTree().size())));
			
			out.print("\n");
			for (int i = 0; i < fields.size(); i++) {
				out.print(line.get(fields.get(i)));
				if (i != fields.size()-1) out.print(",\t");
			}
	
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void saveDataOfGoalTree(GoalTree gTree) {
		this.numberOfGoals = gTree.getTree().size();

		for (GoalNode g : gTree.getTree()) {
			this.numberOfWorkloads += g.getWorkloads().size();
			this.numberOfInforms += g.getInforms().size();
        }
	}
	
	public void saveDataOfBrokenTree(GoalTree gTree) {
		this.numberOfBrokenGoals = gTree.getTree().size();
		
		for (GoalNode g : gTree.getTree()) {
			this.numberOfBrokenWorkloads += g.getWorkloads().size();
			this.numberOfBrokenInforms += g.getInforms().size();
        }

		this.sumEfforts = gTree.sumEfforts();
	}
	
    private void createOutPutFolders() {
        // create folders if doesnt exist
		File file = new File("output/statistics/tmp");
        file.getParentFile().mkdirs();
    }

	public void deleteExistingStatistics() {
		try {
			final File filepath = new File("output/statistics");
			FileUtils.deleteDirectory(filepath);

		} catch (final IOException e) {
			e.printStackTrace();
		}
	}
}
