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
	
	int id = 0;
	int numberOfBrokenDataLoads = 0;
	double minIdle = 0.0;
	double sumEfforts = 0.0;
	String bgTree = "";
	
	List<String> fields = new ArrayList<>();
	
	public static OrganisationStatistics getInstance() 
    { 
        if (instance == null) 
        	instance = new OrganisationStatistics(); 
  
        return instance; 
    } 
	
	private OrganisationStatistics() {
		//fields and sequence of columns in the CSV file
		this.fields.add("id");
		this.fields.add("Roles");
		this.fields.add("rWorkloads");
		this.fields.add("rDataLoads");
		this.fields.add("bDataLoads");
		this.fields.add("minIdle");
		this.fields.add("Idleness");
		this.fields.add("bgTree");
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
			
			int numberOfAssignedWorkloads = 0;
			int numberOfAssignedDataLoads = 0;

			for (final RoleNode or : o.getRolesTree().getTree()) {
				numberOfAssignedWorkloads += or.getWorkloads().size();
				numberOfAssignedDataLoads += or.getDataLoads().size();
			}
			
			Parameters.getInstance();
			line.put("id", (Integer.toString(++id)));
			line.put("Roles", (Integer.toString(o.getRolesTree().getTree().size())));
			line.put("rWorkloads", (Integer.toString(numberOfAssignedWorkloads)));
			line.put("rDataLoads", (Integer.toString(numberOfAssignedDataLoads)));
			line.put("bDataLoads", (Integer.toString(numberOfBrokenDataLoads)));
			line.put("minIdle", (Double.toString(minIdle)));
			line.put("Idleness", (Double.toString(o.getRolesTree().getTree().size() * Parameters.getMaxWorkload() - sumEfforts)));
			line.put("bgTree", bgTree);
			
			out.print("\n");
			for (int i = 0; i < fields.size(); i++) {
				out.print(line.get(fields.get(i)));
				if (i != fields.size()-1) out.print(",\t");
			}
	
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void saveDataOfBrokenTree() {
		GoalTree gTree = GoalTree.getInstance();
		this.bgTree = gTree.getTree().toString();
		
		for (GoalNode g : gTree.getTree()) {
			this.numberOfBrokenDataLoads += g.getDataLoads().size();
        }

		Parameters.getInstance();
		sumEfforts = gTree.getSumEfforts();
		minIdle = Parameters.getMaxWorkload() - (sumEfforts % Parameters.getMaxWorkload());
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
