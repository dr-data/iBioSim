package sbol;

import java.awt.*;
import javax.swing.*;

import org.sbolstandard.libSBOLj.*;
import java.io.*;
import java.net.URI;
import java.util.*;

import main.Gui;

public class SbolBrowser extends JPanel {
	
	private HashMap<String, Library> libMap = new HashMap<String, Library>();
	private HashMap<String, DnaComponent> compMap = new HashMap<String, DnaComponent>();
	private HashMap<String, SequenceFeature> featMap = new HashMap<String, SequenceFeature>();
	private String filter = "";
	private String[] options = {"Ok"};
	private JPanel selectionPanel = new JPanel(new GridLayout(1,3));
	private TextArea viewArea = new TextArea("", 0, 0, TextArea.SCROLLBARS_VERTICAL_ONLY);
	
	//Constructor when browsing a single RDF file from the main gui
	public SbolBrowser(String filePath) {
		super(new BorderLayout());
		
		loadRDF(filePath);
		
		constructBrowser();
		
		JPanel browserPanel = new JPanel();
		browserPanel.add(selectionPanel, "North");
		browserPanel.add(viewArea, "Center");
		
		JTabbedPane browserTab = new JTabbedPane();
		browserTab.add("SBOL Browser", browserPanel);
		this.add(browserTab);
	}
	
	//Constructor when browsing RDF file subsets for SBOL to GCM association
	public SbolBrowser(HashSet<String> filePaths, String filter) {
		super(new GridLayout(2,1));
		this.filter = filter;
		
		for (String fp : filePaths)
			loadRDF(fp);
		
		constructBrowser();
		
		this.add(selectionPanel);
		this.add(viewArea);
		
		boolean display = true;
		while (display)
			display = browserOpen();
	}
	
	private boolean browserOpen() {
		JOptionPane.showOptionDialog(Gui.frame, this,
				"SBOL Browser", JOptionPane.DEFAULT_OPTION,
				JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		return false;
	}
	
	private void constructBrowser() {
		viewArea.setEditable(false);
		
		SequenceFeaturePanel featPanel = new SequenceFeaturePanel(featMap, viewArea);
		DnaComponentPanel compPanel = new DnaComponentPanel(compMap, viewArea, featPanel, filter);
		LibraryPanel libPanel = new LibraryPanel(libMap, compMap, featMap, viewArea, compPanel, featPanel, filter);
		libPanel.setLibraries(libMap.keySet());
		
		selectionPanel.add(libPanel);
		selectionPanel.add(compPanel);
		selectionPanel.add(featPanel);
	}
	
	private void loadRDF(String filePath) {
		try {
			FileInputStream in = new FileInputStream(filePath);
			Scanner scanIn = new Scanner(in).useDelimiter("\n");
			String rdfString = "";
			HashSet<String> libIds = new HashSet<String>();
			boolean libFlag = false;
			while (scanIn.hasNext()) {
				String token = scanIn.next();
				if (libFlag && token.startsWith("\t<displayId")) {
						int start = token.indexOf(">");
						int stop = token.indexOf("<", start);
						libIds.add(token.substring(start + 1, stop));
						libFlag = false;
				} else if (token.equals("\t<rdf:type rdf:resource=\"http://sbols.org/sbol.owl#Library\"/>"))
					libFlag = true;
				rdfString = rdfString.concat(token) + "\n";
			}
			scanIn.close();
			SBOLservice factory = SBOLutil.fromRDF(rdfString);
			for (String libId : libIds) {
				Library lib = factory.getLibrary(libId);
				libMap.put(libId, lib);
			}		
		} catch (Exception e1) {
			JOptionPane.showMessageDialog(Gui.frame, "Error opening file.", "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}
}
