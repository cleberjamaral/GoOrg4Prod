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
	double originalWorkLoad = 0.0;
	double minIdle = 0.0;
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
		this.fields.add("%WL+");
		this.fields.add("%DL+");
		this.fields.add("%Idle+"); //Idleness %
		this.fields.add("%Geral"); //Generalness %
		this.fields.add("%Speci"); //Specificness %
		this.fields.add("Levels");
		this.fields.add("States");
		this.fields.add("miIdle");
		this.fields.add("Idlene"); //Idleness
		this.fields.add("bWL");
		this.fields.add("rWL");
		this.fields.add("bDL");
		this.fields.add("rDL");
		this.fields.add("rTree");
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
			
			double assignedWorkLoad = o.getRolesTree().getSumWorkload();
			double treeGeneralness = o.getRolesTree().getGeneralness();
			double treeSpecificness = o.getRolesTree().getSpecificness();
			
			double assignedDataLoad = 0.0;

			for (final RoleNode or : o.getRolesTree().getTree()) {
				for (DataLoad d : or.getDataLoads()) 
					assignedDataLoad += (double) d.getValue();
			}
			
			Parameters.getInstance();
			line.put("id", (Integer.toString(++id)));
			line.put("Roles", (Integer.toString(o.getRolesTree().getTree().size())));
			line.put("bWL", (String.format("%.2f", originalWorkLoad)));
			line.put("rWL", (String.format("%.2f", assignedWorkLoad)));
			line.put("bDL", (String.format("%.2f", originalDataLoad)));
			line.put("rDL", (String.format("%.2f", assignedDataLoad)));
			
			double addedWorkLoad = 0;
			if (originalWorkLoad > 0) addedWorkLoad = 100 * assignedWorkLoad / originalWorkLoad;
			line.put("%WL+", (String.format("%.0f%%", addedWorkLoad)));

			double addedDataLoad = 0;
			if (originalDataLoad > 0) addedDataLoad = 100 * assignedDataLoad / originalDataLoad;
			line.put("%DL+", (String.format("%.0f%%", addedDataLoad)));
			
			line.put("miIdle", (Double.toString(minIdle)));
			
			double idleness = o.getRolesTree().getTree().size() * Parameters.getMaxWorkload() - originalWorkLoad;
			line.put("Idlene", (Double.toString(idleness)));
			
			line.put("%Idle+", (String.format("%.0f%%", 100 * (idleness - minIdle) / minIdle)));

			line.put("%Geral", (String.format("%.0f%%", 100 * treeGeneralness)));
			line.put("%Speci", (String.format("%.0f%%", 100 * treeSpecificness)));

			line.put("rTree", o.getRolesTree().toString());
			line.put("bgTree", bgTree);
			line.put("States", (Integer.toString(o.getNStates())));
			line.put("Levels", (Integer.toString(o.getRolesTree().getNumberOfLevels())));
			
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
		originalWorkLoad = gTree.getSumEfforts();
		minIdle = Parameters.getMaxWorkload() - (originalWorkLoad % Parameters.getMaxWorkload());
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
