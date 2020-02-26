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

import annotations.DataLoad;

import org.apache.commons.io.FileUtils;

import organisation.goal.GoalNode;
import organisation.goal.GoalTree;
import organisation.role.RoleNode;
import organisation.search.Organisation;

public class OrganisationStatistics {
	
	private static OrganisationStatistics instance = null;
	
	int id = 0;
	double originalDataLoad = 0.0;
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
		this.fields.add("%DataLoads");
		this.fields.add("%IdleAdded");
		this.fields.add("genStates");
		this.fields.add("bDataLoads");
		this.fields.add("rDataLoads");
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
			
			double assignedDataLoad = 0.0;

			for (final RoleNode or : o.getRolesTree().getTree()) {
				for (DataLoad d : or.getDataLoads()) 
					assignedDataLoad += (double) d.getValue();
			}
			
			Parameters.getInstance();
			line.put("id", (Integer.toString(++id)));
			line.put("Roles", (Integer.toString(o.getRolesTree().getTree().size())));
			line.put("bDataLoads", (String.format("%.2f", originalDataLoad)));
			line.put("rDataLoads", (String.format("%.2f", assignedDataLoad)));
			line.put("%DataLoads", (String.format("%.0f%%", 100 * assignedDataLoad / originalDataLoad)));
			line.put("minIdle", (Double.toString(minIdle)));
			double idleness = o.getRolesTree().getTree().size() * Parameters.getMaxWorkload() - sumEfforts;
			line.put("Idleness", (Double.toString(idleness)));
			line.put("%IdleAdded", (String.format("%.0f%%", 100 * (idleness - minIdle) / minIdle)));
			line.put("bgTree", bgTree);
			line.put("genStates", (Integer.toString(o.getGeneratedStates())));
			
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
			for (DataLoad d : g.getDataLoads()) 
				originalDataLoad += (double) d.getValue();
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
