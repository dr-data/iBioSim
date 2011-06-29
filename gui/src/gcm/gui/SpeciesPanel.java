package gcm.gui;

import gcm.gui.modelview.movie.MovieContainer;
import gcm.gui.modelview.movie.visualizations.ColorScheme;
import gcm.gui.modelview.movie.visualizations.ColorSchemeChooser;
import gcm.parser.GCMFile;
import gcm.util.GlobalConstants;
import gcm.util.Utility;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Properties;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.jfree.layout.CenterLayout;
import org.sbml.libsbml.Species;

import sbmleditor.MySpecies;

import main.Gui;

public class SpeciesPanel extends JPanel implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final String COPY_COLOR_TO_ALL = "copy colors to all species";
	
	public SpeciesPanel(String selected, PropertyList speciesList, PropertyList influencesList,
			PropertyList conditionsList, PropertyList componentsList, GCMFile gcm, boolean paramsOnly,
			GCMFile refGCM, GCM2SBMLEditor gcmEditor){

		super(new BorderLayout());
		constructor(selected, speciesList, influencesList, conditionsList, componentsList, gcm, paramsOnly, refGCM, gcmEditor, null);
	}

	public SpeciesPanel(String selected, PropertyList speciesList, PropertyList influencesList,
			PropertyList conditionsList, PropertyList componentsList, GCMFile gcm, boolean paramsOnly,
			GCMFile refGCM, GCM2SBMLEditor gcmEditor, MovieContainer movieContainer){

		super(new BorderLayout());
		constructor(selected, speciesList, influencesList, conditionsList, componentsList, gcm, paramsOnly, refGCM, gcmEditor, movieContainer);
	}
	
	private void constructor(String selected, PropertyList speciesList, PropertyList influencesList,
			PropertyList conditionsList, PropertyList componentsList, GCMFile gcm, boolean paramsOnly,
			GCMFile refGCM,  GCM2SBMLEditor gcmEditor, MovieContainer movieContainer) {

		JPanel grid;
		if (gcm.getSBMLDocument().getLevel() > 2) {
			if (gcm.getSBMLDocument().getModel().getNumCompartments()==1) {
				grid = new JPanel(new GridLayout(12,1));
			} else {
				grid = new JPanel(new GridLayout(13,1));
			}
		} else {
			if (gcm.getSBMLDocument().getModel().getNumCompartments()==1) {
				grid = new JPanel(new GridLayout(11,1));
			} else {
				grid = new JPanel(new GridLayout(12,1));
			}
		}
		this.add(grid, BorderLayout.CENTER);
		

		this.selected = selected;
		this.speciesList = speciesList;
		this.influences = influencesList;
		this.conditions = conditionsList;
		this.components = componentsList;
		this.gcm = gcm;
		this.paramsOnly = paramsOnly;
		this.movieContainer = movieContainer;
		this.gcmEditor = gcmEditor;

		fields = new HashMap<String, PropertyField>();

		// ID field
		PropertyField field = new PropertyField(GlobalConstants.ID, "", null, null, Utility.IDstring,
				paramsOnly, "default");
		if (paramsOnly) {
			field.setEnabled(false);
		}
		fields.put(GlobalConstants.ID, field);
		grid.add(field);

		// Name field
		field = new PropertyField(GlobalConstants.NAME, "", null, null, Utility.NAMEstring,
				paramsOnly, "default");
		if (paramsOnly) {
			field.setEnabled(false);
		}
		fields.put(GlobalConstants.NAME, field);
		grid.add(field);

		// Type field
		JPanel tempPanel = new JPanel();
		JLabel tempLabel = new JLabel(GlobalConstants.TYPE);
		typeBox = new JComboBox(types);
		typeBox.addActionListener(this);
		tempPanel.setLayout(new GridLayout(1, 2));
		tempPanel.add(tempLabel);
		tempPanel.add(typeBox);
		if (paramsOnly) {
			tempLabel.setEnabled(false);
			typeBox.setEnabled(false);
		}
		grid.add(tempPanel);

		Species species = gcm.getSBMLDocument().getModel().getSpecies(selected);

		// compartment field
		tempPanel = new JPanel();
		tempLabel = new JLabel("Compartment");
		compartBox = MySpecies.createCompartmentChoices(gcm.getSBMLDocument());
		compartBox.setSelectedItem(species.getCompartment());
		compartBox.addActionListener(this);
		tempPanel.setLayout(new GridLayout(1, 2));
		tempPanel.add(tempLabel);
		tempPanel.add(compartBox);
		if (paramsOnly) {
			tempLabel.setEnabled(false);
			compartBox.setEnabled(false);
		}
		if (gcm.getSBMLDocument().getModel().getNumCompartments()>1) {
			grid.add(tempPanel);
		}
		
		String[] optionsTF = { "true", "false" };

		// Boundary condition field
		tempPanel = new JPanel();
		tempLabel = new JLabel("Boundary Condition");
		specBoundary = new JComboBox(optionsTF);
		if (species.getBoundaryCondition()) {
			specBoundary.setSelectedItem("true");
		} else {
			specBoundary.setSelectedItem("false");
		}
		tempPanel.setLayout(new GridLayout(1, 2));
		tempPanel.add(tempLabel);
		tempPanel.add(specBoundary);
		if (paramsOnly) {
			tempLabel.setEnabled(false);
			specBoundary.setEnabled(false);
		}
		grid.add(tempPanel);

		// Constant field
		tempPanel = new JPanel();
		tempLabel = new JLabel("Constant");
		specConstant = new JComboBox(optionsTF);
		if (species.getConstant()) {
			specConstant.setSelectedItem("true");
		} else {
			specConstant.setSelectedItem("false");
		}
		tempPanel.setLayout(new GridLayout(1, 2));
		tempPanel.add(tempLabel);
		tempPanel.add(specConstant);
		if (paramsOnly) {
			tempLabel.setEnabled(false);
			specConstant.setEnabled(false);
		}
		grid.add(tempPanel);

		// Has only substance units field
		tempPanel = new JPanel();
		tempLabel = new JLabel("Has Only Substance Units");
		specHasOnly = new JComboBox(optionsTF);
		if (species.getHasOnlySubstanceUnits()) {
			specHasOnly.setSelectedItem("true");
		} else {
			specHasOnly.setSelectedItem("false");
		}
		tempPanel.setLayout(new GridLayout(1, 2));
		tempPanel.add(tempLabel);
		tempPanel.add(specHasOnly);
		if (paramsOnly) {
			tempLabel.setEnabled(false);
			specHasOnly.setEnabled(false);
		}
		grid.add(tempPanel);
		
		// Units field
		tempPanel = new JPanel();
		tempLabel = new JLabel("Units");
		unitsBox = MySpecies.createUnitsChoices(gcm.getSBMLDocument());
		if (species.isSetUnits()) {
			unitsBox.setSelectedItem(species.getUnits());
		}
		tempPanel.setLayout(new GridLayout(1, 2));
		tempPanel.add(tempLabel);
		tempPanel.add(unitsBox);
		if (paramsOnly) {
			tempLabel.setEnabled(false);
			unitsBox.setEnabled(false);
		}
		grid.add(tempPanel);
		
		// Conversion factor field
		if (gcm.getSBMLDocument().getLevel() > 2) {
			tempPanel = new JPanel();
			tempLabel = new JLabel("Conversion Factor");
			convBox = MySpecies.createConversionFactorChoices(gcm.getSBMLDocument());
			if (species.isSetConversionFactor()) {
				convBox.setSelectedItem(species.getConversionFactor());
			}
			tempPanel.setLayout(new GridLayout(1, 2));
			tempPanel.add(tempLabel);
			tempPanel.add(convBox);
			if (paramsOnly) {
				tempLabel.setEnabled(false);
				convBox.setEnabled(false);
			}
			grid.add(tempPanel);
		}
		
		// Initial field
		String origString = "default";
		if (paramsOnly) {
			String defaultValue = refGCM.getParameter(GlobalConstants.INITIAL_STRING);
			if (refGCM.getSpecies().get(selected).containsKey(GlobalConstants.INITIAL_STRING)) {
				defaultValue = refGCM.getSpecies().get(selected).getProperty(GlobalConstants.INITIAL_STRING);
				origString = "custom";
			}
			else if (gcm.globalParameterIsSet(GlobalConstants.INITIAL_STRING)) {
				defaultValue = gcm.getParameter(GlobalConstants.INITIAL_STRING);
			}
			field = new PropertyField(GlobalConstants.INITIAL_STRING, 
					gcm.getParameter(GlobalConstants.INITIAL_STRING), origString, defaultValue,
					Utility.SWEEPstring + "|" + Utility.CONCstring, paramsOnly, origString);
		}
		else {
			field = new PropertyField(GlobalConstants.INITIAL_STRING, 
					gcm.getParameter(GlobalConstants.INITIAL_STRING), origString, 
					gcm.getParameter(GlobalConstants.INITIAL_STRING), Utility.NUMstring + "|" + Utility.CONCstring, paramsOnly,
					origString);
		}
		fields.put(GlobalConstants.INITIAL_STRING, field);
		grid.add(field);

		// Decay field
		origString = "default";
		if (paramsOnly) {
			String defaultValue = refGCM.getParameter(GlobalConstants.KDECAY_STRING);
			if (refGCM.getSpecies().get(selected).containsKey(GlobalConstants.KDECAY_STRING)) {
				defaultValue = refGCM.getSpecies().get(selected).getProperty(
						GlobalConstants.KDECAY_STRING);
				origString = "custom";
			}
			else if (gcm.globalParameterIsSet(GlobalConstants.KDECAY_STRING)) {
				defaultValue = gcm.getParameter(GlobalConstants.KDECAY_STRING);
			}
			field = new PropertyField(GlobalConstants.KDECAY_STRING, gcm
					.getParameter(GlobalConstants.KDECAY_STRING), origString, defaultValue,
					Utility.SWEEPstring, paramsOnly, origString);
		}
		else {
			field = new PropertyField(GlobalConstants.KDECAY_STRING, gcm
					.getParameter(GlobalConstants.KDECAY_STRING), origString, gcm
					.getParameter(GlobalConstants.KDECAY_STRING), Utility.NUMstring, paramsOnly,
					origString);
		}
		fields.put(GlobalConstants.KDECAY_STRING, field);
		grid.add(field);

		// add the color chooser for the movie component
		if(paramsOnly){
			JPanel colorsPanel = new JPanel(new BorderLayout());
			this.add(colorsPanel, BorderLayout.SOUTH);
			ColorScheme colorScheme = movieContainer.getMoviePreferences().getOrCreateColorSchemeForSpecies(selected, movieContainer.getTSDParser());
			colorSchemeChooser = new ColorSchemeChooser(colorScheme);
			colorsPanel.add(colorSchemeChooser, BorderLayout.CENTER);
			
			
			// build a special button to display the extra options
			JPanel tpanel = new JPanel(new CenterLayout());
			JButton copyButton = new JButton("Copy Color Properties to All Species");
			copyButton.setToolTipText("This button will copy these settings to all other components of the same type.");
			copyButton.setActionCommand(COPY_COLOR_TO_ALL);
			copyButton.addActionListener(this);
			copyButton.setSize(200, 25);
			tpanel.add(copyButton);
			colorsPanel.add(tpanel, BorderLayout.SOUTH);
			
		}
		
		
		// Complex Equilibrium Constant Field
		origString = "default";
		if (paramsOnly) {
			String defaultValue = refGCM.getParameter(GlobalConstants.KCOMPLEX_STRING);
			if (refGCM.getSpecies().get(selected).containsKey(GlobalConstants.KCOMPLEX_STRING)) {
				defaultValue = refGCM.getSpecies().get(selected).getProperty(
						GlobalConstants.KCOMPLEX_STRING);
				origString = "custom";
			}
			else if (gcm.globalParameterIsSet(GlobalConstants.KCOMPLEX_STRING)) {
				defaultValue = gcm.getParameter(GlobalConstants.KCOMPLEX_STRING);
			}
			field = new PropertyField(GlobalConstants.KCOMPLEX_STRING, 
					gcm.getParameter(GlobalConstants.KCOMPLEX_STRING), origString, defaultValue,
					Utility.SLASHSWEEPstring, paramsOnly, origString);
		}
		else {
			field = new PropertyField(GlobalConstants.KCOMPLEX_STRING, 
					gcm.getParameter(GlobalConstants.KCOMPLEX_STRING), origString, 
					gcm.getParameter(GlobalConstants.KCOMPLEX_STRING), Utility.SLASHstring, paramsOnly,
					origString);
		}
		fields.put(GlobalConstants.KCOMPLEX_STRING, field);
		grid.add(field);
		
		// Membrane Diffusible Field
		origString = "default";
		if (paramsOnly) {
			String defaultValue = refGCM.getParameter(GlobalConstants.MEMDIFF_STRING);
			if (refGCM.getSpecies().get(selected).containsKey(GlobalConstants.MEMDIFF_STRING)) {
				defaultValue = refGCM.getSpecies().get(selected).getProperty(
						GlobalConstants.MEMDIFF_STRING);
				origString = "custom";
			}
			else if (gcm.globalParameterIsSet(GlobalConstants.MEMDIFF_STRING)) {
				defaultValue = gcm.getParameter(GlobalConstants.MEMDIFF_STRING);
			}
			field = new PropertyField(GlobalConstants.MEMDIFF_STRING, gcm
					.getParameter(GlobalConstants.MEMDIFF_STRING), origString, defaultValue,
					Utility.SWEEPstring, paramsOnly, origString);
		}
		else {
			field = new PropertyField(GlobalConstants.MEMDIFF_STRING, gcm
					.getParameter(GlobalConstants.MEMDIFF_STRING), origString, gcm
					.getParameter(GlobalConstants.MEMDIFF_STRING), Utility.NUMstring, paramsOnly,
					origString);
		}
		fields.put(GlobalConstants.MEMDIFF_STRING, field);
		grid.add(field);

		if (selected != null) {
			Properties prop = gcm.getSpecies().get(selected);
			fields.get(GlobalConstants.ID).setValue(selected);
			//This will generate the action command associated with changing the type combo box
			typeBox.setSelectedItem(prop.getProperty(GlobalConstants.TYPE));
			loadProperties(prop);
		}
		else {
			typeBox.setSelectedItem(types[1]);
		}
		boolean display = false;
		while (!display) {
			display = openGui();
		}
	}

	private boolean checkValues() {
		for (PropertyField f : fields.values()) {
			if (!f.isValidValue() || f.getValue().equals("RNAP") || f.getValue().endsWith("_RNAP")
					|| f.getValue().endsWith("_bound")) {
				return false;
			}
		}
		return true;
	}

	private boolean openGui() {
		
		int value = JOptionPane.showOptionDialog(Gui.frame, this, "Species Editor",
				JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		
		// the new id of the species. Will be filled in later.
		String newSpeciesID = null;
		
		// if the value is -1 (user hit escape) then set it equal to the cancel value
		if(value == -1)
			for(int i=0; i<options.length; i++){if(options[i] == options[1]){value = i;}}
		
		if (options[value].equals(options[0])) { // "OK" or "Ok and copy..."
			if (!checkValues()) {
				Utility.createErrorMessage("Error", "Illegal values entered.");
				return false;
			}
			if (selected == null) {
				if (gcm.getComponents().containsKey(fields.get(GlobalConstants.ID).getValue())
						|| gcm.getSpecies().containsKey(fields.get(GlobalConstants.ID).getValue())
						|| gcm.getParameters().containsKey(fields.get(GlobalConstants.ID).getValue())
						|| gcm.getPromoters().containsKey(fields.get(GlobalConstants.ID).getValue())) {
					Utility.createErrorMessage("Error", "Id already exists.");
					return false;
				}
			}
			else if (!selected.equals(fields.get(GlobalConstants.ID).getValue())) {
				if (gcm.getComponents().containsKey(fields.get(GlobalConstants.ID).getValue())
						|| gcm.getSpecies().containsKey(fields.get(GlobalConstants.ID).getValue())
						|| gcm.getParameters().containsKey(fields.get(GlobalConstants.ID).getValue())
						|| gcm.getPromoters().containsKey(fields.get(GlobalConstants.ID).getValue())) {
					Utility.createErrorMessage("Error", "Id already exists.");
					return false;
				}
			}
			if (selected != null
					&& !gcm.editSpeciesCheck(selected, typeBox.getSelectedItem().toString())) {
				Utility.createErrorMessage("Error", "Cannot change species type.  "
						+ "Species is used as a port in a component.");
				return false;
			}
			if (selected != null && (typeBox.getSelectedItem().toString().equals(types[0]) || 
					typeBox.getSelectedItem().toString().equals(types[3]))) {
				for (String infl : gcm.getInfluences().keySet()) {
					String geneProduct = GCMFile.getOutput(infl);
					if (selected.equals(geneProduct)) {
						Utility.createErrorMessage("Error", "There must be no connections to an input species " +
						"or constitutive species.");
						return false;
					}
				}
			}
			newSpeciesID = fields.get(GlobalConstants.ID).getValue();

			Properties property = new Properties();

			// copy the old values into the new property. Some will then be
			// overwritten ,
			// but others (such as positioning info) will not and need to be
			// preserved.
			if (selected != null) {
				for (Object s : gcm.getSpecies().get(selected).keySet()) {
					String k = s.toString();
					String v = (gcm.getSpecies().get(selected).getProperty(k)).toString();
					if (!k.equals("label")) {
						property.put(k, v);
					}
				}
				if (!paramsOnly) {
					Species species = gcm.getSBMLDocument().getModel().getSpecies(selected);
					species.setId(fields.get(GlobalConstants.ID).getValue());
					species.setName(fields.get(GlobalConstants.NAME).getValue());
					if (Utility.isValid(fields.get(GlobalConstants.INITIAL_STRING).getValue(), Utility.NUMstring)) {
						species.setInitialAmount(Double.parseDouble(fields.get(GlobalConstants.INITIAL_STRING).getValue()));
					} else {
						String conc = fields.get(GlobalConstants.INITIAL_STRING).getValue();
						species.setInitialConcentration(Double.parseDouble(conc.substring(1,conc.length()-1)));
					}
					if (specBoundary.getSelectedItem().equals("true")) {
						species.setBoundaryCondition(true);
					}
					else {
						species.setBoundaryCondition(false);
					}
					if (specConstant.getSelectedItem().equals("true")) {
						species.setConstant(true);
					}
					else {
						species.setConstant(false);
					}
					if (specHasOnly.getSelectedItem().equals("true")) {
						species.setHasOnlySubstanceUnits(true);
					}
					else {
						species.setHasOnlySubstanceUnits(false);
					}
					String unit = (String) unitsBox.getSelectedItem();
					if (unit.equals("( none )")) {
						species.unsetUnits();
					}
					else {
						species.setUnits(unit);
					}
					String convFactor = null;
					if (gcm.getSBMLDocument().getLevel() > 2) {
						convFactor = (String) convBox.getSelectedItem();
					}
					if (convFactor.equals("( none )")) {
						species.unsetConversionFactor();
					}
					else {
						species.setConversionFactor(convFactor);
					}
				}
			}

			for (PropertyField f : fields.values()) {
				if (f.getState() == null || f.getState().equals(f.getStates()[1])) {
					property.put(f.getKey(), f.getValue());
				}
				else {
					property.remove(f.getKey());
				}
			}
			property.put(GlobalConstants.TYPE, typeBox.getSelectedItem().toString());

			if (selected != null && !selected.equals(newSpeciesID)) {
				gcm.changeSpeciesName(selected, newSpeciesID);
				((DefaultListModel) influences.getModel()).clear();
				influences.addAllItem(gcm.getInfluences().keySet());
				((DefaultListModel) conditions.getModel()).clear();
				conditions.addAllItem(gcm.getConditions());
				((DefaultListModel) components.getModel()).clear();
				for (String c : gcm.getComponents().keySet()) {
					components.addItem(c + " "
							+ gcm.getComponents().get(c).getProperty("gcm").replace(".gcm", "")
							+ " " + gcm.getComponentPortMap(c));
				}
			}
			gcm.addSpecies(newSpeciesID, property);
			if (paramsOnly) {
				if (fields.get(GlobalConstants.INITIAL_STRING).getState().equals(
						fields.get(GlobalConstants.INITIAL_STRING).getStates()[1])
						|| fields.get(GlobalConstants.KCOMPLEX_STRING).getState().equals(
								fields.get(GlobalConstants.KCOMPLEX_STRING).getStates()[1])
						|| fields.get(GlobalConstants.KDECAY_STRING).getState().equals(
								fields.get(GlobalConstants.KDECAY_STRING).getStates()[1])
						|| fields.get(GlobalConstants.MEMDIFF_STRING).getState().equals(
								fields.get(GlobalConstants.MEMDIFF_STRING).getStates()[1])) {
					newSpeciesID += " Modified";
				}
			}
			speciesList.removeItem(selected);
			speciesList.removeItem(selected + " Modified");
			speciesList.addItem(newSpeciesID);
			speciesList.setSelectedValue(newSpeciesID, true);
			// ColorSchemeChooser
			if(colorSchemeChooser != null){
				//movieProperties.setProperty(id + MovieContainer.COLOR_PREPEND, colorSchemeChooser.)
			}

			if(paramsOnly)
				colorSchemeChooser.saveChanges();
			
			gcmEditor.setDirty(true);
		}

		if(options[value].equals(options[1])) { // "Cancel"
			// System.out.println();
			return true;
		}
		return true;
	}

	public String updates() {
		String updates = "";
		if (paramsOnly) {
			if (fields.get(GlobalConstants.INITIAL_STRING).getState().equals(
					fields.get(GlobalConstants.INITIAL_STRING).getStates()[1])) {
				updates += fields.get(GlobalConstants.ID).getValue() + "/"
						+ GlobalConstants.INITIAL_STRING + " "
						+ fields.get(GlobalConstants.INITIAL_STRING).getValue();
			}
			if (fields.get(GlobalConstants.KDECAY_STRING).getState().equals(
					fields.get(GlobalConstants.KDECAY_STRING).getStates()[1])) {
				if (!updates.equals("")) {
					updates += "\n";
				}
				updates += fields.get(GlobalConstants.ID).getValue() + "/"
						+ GlobalConstants.KDECAY_STRING + " "
						+ fields.get(GlobalConstants.KDECAY_STRING).getValue();
			}
			if (fields.get(GlobalConstants.KCOMPLEX_STRING).getState().equals(
					fields.get(GlobalConstants.KCOMPLEX_STRING).getStates()[1])) {
				if (!updates.equals("")) {
					updates += "\n";
				}
				updates += fields.get(GlobalConstants.ID).getValue() + "/"
						+ GlobalConstants.KCOMPLEX_STRING + " "
						+ fields.get(GlobalConstants.KCOMPLEX_STRING).getValue();
			}
			if (fields.get(GlobalConstants.MEMDIFF_STRING).getState().equals(
					fields.get(GlobalConstants.MEMDIFF_STRING).getStates()[1])) {
				if (!updates.equals("")) {
					updates += "\n";
				}
				updates += fields.get(GlobalConstants.ID).getValue() + "/"
						+ GlobalConstants.MEMDIFF_STRING + " "
						+ fields.get(GlobalConstants.MEMDIFF_STRING).getValue();
			}
			if (updates.equals("")) {
				updates += fields.get(GlobalConstants.ID).getValue() + "/";
			}
		}
		return updates;
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("comboBoxChanged")) {
			setType(typeBox.getSelectedItem().toString());
		}else if(e.getActionCommand().equals(COPY_COLOR_TO_ALL)){
			colorSchemeChooser.saveChanges();
			movieContainer.copyMoviePreferencesSpecies(this.selected);
		}
	}

	private void setType(String type) {
		//input
		if (type.equals(types[0])) {
			fields.get(GlobalConstants.KDECAY_STRING).setEnabled(false);
			fields.get(GlobalConstants.KCOMPLEX_STRING).setEnabled(false);
			fields.get(GlobalConstants.MEMDIFF_STRING).setEnabled(false);
		}
		//internal
		else if (type.equals(types[1])) {
			fields.get(GlobalConstants.KDECAY_STRING).setEnabled(true);
			fields.get(GlobalConstants.KCOMPLEX_STRING).setEnabled(true);
			fields.get(GlobalConstants.MEMDIFF_STRING).setEnabled(false);
		}
		//output
		else if (type.equals(types[2])) {
			fields.get(GlobalConstants.KDECAY_STRING).setEnabled(true);
			fields.get(GlobalConstants.KCOMPLEX_STRING).setEnabled(true);
			fields.get(GlobalConstants.MEMDIFF_STRING).setEnabled(false);
		} 
		//diffusible
		else if (type.equals(types[4])) {
			fields.get(GlobalConstants.KDECAY_STRING).setEnabled(true);
			fields.get(GlobalConstants.KCOMPLEX_STRING).setEnabled(true);
			fields.get(GlobalConstants.MEMDIFF_STRING).setEnabled(true);
		} 
		else {
			fields.get(GlobalConstants.KDECAY_STRING).setEnabled(true);
			fields.get(GlobalConstants.KCOMPLEX_STRING).setEnabled(false);
			fields.get(GlobalConstants.MEMDIFF_STRING).setEnabled(false);
		}
	}

	private void loadProperties(Properties property) {
		for (Object o : property.keySet()) {
			if (fields.containsKey(o.toString())) {
				fields.get(o.toString()).setValue(property.getProperty(o.toString()));
				fields.get(o.toString()).setCustom();
			}
		}
	}
	
	private String selected = "";

	private PropertyList speciesList = null;

	private PropertyList influences = null;
	
	private PropertyList conditions = null;
	
	private PropertyList components = null;

	private String[] options = { "Ok", "Cancel" };

	private GCMFile gcm = null;

	private JComboBox typeBox = null;

	private JComboBox compartBox = null;

	private JComboBox convBox = null;

	private JComboBox unitsBox = null;

	private JComboBox specBoundary = null;

	private JComboBox specConstant = null;

	private JComboBox specHasOnly = null;

	private static final String[] types = new String[] { GlobalConstants.INPUT, GlobalConstants.INTERNAL, 
		GlobalConstants.OUTPUT, GlobalConstants.SPASTIC, GlobalConstants.DIFFUSIBLE};

	private HashMap<String, PropertyField> fields = null;

	private boolean paramsOnly;
	
	private MovieContainer movieContainer;

	private ColorSchemeChooser colorSchemeChooser;
	
	private GCM2SBMLEditor gcmEditor;
}
