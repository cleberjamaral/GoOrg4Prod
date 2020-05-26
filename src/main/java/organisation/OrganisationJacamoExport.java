package organisation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;

import fit.Requirement;
import fit.Resource;
import organisation.binder.Binding;
import organisation.search.Organisation;

/**
 * @author cleber
 *
 */
public class OrganisationJacamoExport {
	
	public OrganisationJacamoExport() {}

	public void exportBindingAsJCM(final Organisation o, final String orgIndex, final Binding binding) {
		createJacamoExportFolders();

		String index = "";
		if (!orgIndex.equals(""))
			index = "_" + orgIndex;

		try {
			File f = new File("output/jcm/" + o.getOrgName() + index + ".jcm");
			FileOutputStream outputFile = new FileOutputStream(f, false);

			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append("/* JCM created automatically by GoOrg */\n\n");
			stringBuilder.append("mas " + o.getOrgName() + index + " {\n");

			Map<Requirement, Resource> matches = binding.getMatches();

			for (String agent : binding.getAgents()) {
				stringBuilder.append("\t" + agent + "{\n");
				stringBuilder.append("\t\tjoin: goorg\n");
				// One artifact for each created position
				for (Entry<Requirement, Resource> e : matches.entrySet()) {
					if (e.getValue().getResource().toString().equals(agent)) {
						stringBuilder.append("\t\tfocus: goorg." + e.getKey().getRequirement().toString() + "\n");
					}
				}
				stringBuilder.append("\t}\n");
			}
			stringBuilder.append("\n");
			stringBuilder.append("\tworkspace goorg {\n");
			for (Requirement r : matches.keySet()) {
				stringBuilder.append("\t\tartifact " + r.getRequirement().toString() + ": Counter(0)\n");
			}
			stringBuilder.append("\t}\n");

			stringBuilder.append("}\n");

			byte[] bytes = stringBuilder.toString().getBytes();
			outputFile.write(bytes);
			outputFile.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
            
	public void exportBindingAsJCM() {
		createJacamoExportFolders();
	}

	private void createJacamoExportFolders() {
        // create folders if doesnt exist
		File file = new File("output/jcm/tmp");
        file.getParentFile().mkdirs();
    }

    public void deleteExistingJcms() {
		try {
			final File filepath = new File("output/jcm");
			FileUtils.deleteDirectory(filepath);

		} catch (final IOException e) {
			e.printStackTrace();
		}
    }

}
