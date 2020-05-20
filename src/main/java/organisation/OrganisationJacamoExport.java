package organisation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import annotations.DataLoad;
import organisation.goal.GoalNode;
import organisation.goal.GoalTree;
import organisation.role.RoleNode;
import organisation.search.Organisation;

public class OrganisationJacamoExport {
	
	public OrganisationJacamoExport() {}

	public void exportOrganisationAsMoiseXML(final Organisation o, final String orgIndex) {
		createJacamoExportFolders();
		
		String index = "";
		if (!orgIndex.equals(""))
			index = "_" + orgIndex;

		try {
			Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			Element root = (Element) document.createElement("organisational-specification");
			root.setAttribute("id", o.getOrgName() + index );
			root.setAttribute("os-version", "0.7");
			root.setAttribute("xmlns", "http://moise.sourceforge.net/os");
			root.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
			root.setAttribute("xsi:schemaLocation",
					"http://moise.sourceforge.net/os http://moise.sourceforge.net/xml/os.xsd");
			document.appendChild(root);

			Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			DOMSource documentDOM = new DOMSource(document);
			StreamResult documentTXT = new StreamResult(new File("output/org/" + o.getOrgName() + index + ".xml"));
			transformer.transform(documentDOM, documentTXT);

		} catch (ParserConfigurationException | TransformerFactoryConfigurationError | TransformerException e) {
			e.printStackTrace();
		}
	}
            
	public void exportBindingAsJCM() {
		createJacamoExportFolders();
	}

	private void createJacamoExportFolders() {
        // create folders if doesnt exist
		File file = new File("output/org/tmp");
		file.getParentFile().mkdirs();
        file = new File("output/jcm/tmp");
        file.getParentFile().mkdirs();
    }

	public void deleteExistingOrgs() {
		try {
			final File filepath = new File("output/org");
			FileUtils.deleteDirectory(filepath);

		} catch (final IOException e) {
			e.printStackTrace();
		}
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
