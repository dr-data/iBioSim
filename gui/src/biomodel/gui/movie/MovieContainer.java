package biomodel.gui.movie;


import main.Gui;
import main.util.Utility;
import main.util.dataparser.TSDParser;

import analysis.AnalysisView;
import biomodel.gui.ModelEditor;
import biomodel.gui.movie.SerializableScheme;
import biomodel.gui.schematic.ListChooser;
import biomodel.gui.schematic.Schematic;
import biomodel.gui.schematic.TreeChooser;
import biomodel.gui.schematic.Utils;
import biomodel.gui.textualeditor.MySpecies;
import biomodel.parser.BioModel;
import biomodel.util.GlobalConstants;

import com.google.gson.Gson;


import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.Timer;


public class MovieContainer extends JPanel implements ActionListener {

	public static final String COLOR_PREPEND = "_COLOR";
	public static final String MIN_PREPEND = "_MIN";
	public static final String MAX_PREPEND = "_MAX";
	
	private final String PLAYING = "playing";
	private final String PAUSED = "paused";
	
	private String mode = PLAYING;
	
	public static final int FRAME_DELAY_MILLISECONDS = 20;
	
	private static final long serialVersionUID = 1L;
	
	private Schematic schematic;
	private AnalysisView reb2sac;
	private BioModel gcm;
	private Gui biosim;
	private ModelEditor gcm2sbml;
	private TSDParser parser;
	private Timer playTimer;
	private MovieScheme movieScheme;
	private int initialSliderValue;
	
	private boolean isUIInitialized;
	private boolean isDirty = false;
	private String outputFilename = "";
	
	//movie toolbar/UI elements
	private JButton fileButton;
	private JButton playPauseButton;
	private JButton rewindButton;
	private JButton singleStepButton;
	private JButton clearButton;
	private JToggleButton movieButton;
	private JSlider slider;
	
	
	/**
	 * constructor
	 * 
	 * @param reb2sac_
	 * @param gcm
	 * @param biosim
	 * @param gcm2sbml
	 */
	public MovieContainer(AnalysisView reb2sac_, BioModel gcm, Gui biosim, ModelEditor gcm2sbml){
		
		super(new BorderLayout());
		
		JComboBox compartmentList = MySpecies.createCompartmentChoices(gcm.getSBMLDocument());
		schematic = new Schematic(gcm, biosim, gcm2sbml, false, this, null, gcm.getReactionPanel(), compartmentList);
		this.add(schematic, BorderLayout.CENTER);
		
		this.gcm = gcm;
		this.biosim = biosim;
		this.reb2sac = reb2sac_;
		this.gcm2sbml = gcm2sbml;
		this.movieScheme = new MovieScheme();
		
		this.playTimer = new Timer(0, playTimerEventHandler);
		mode = PAUSED;
	}	
	
	
	//TSD FILE METHODS
	
	/**
	 * returns a vector of strings of TSD filenames within a directory
	 * i don't know why it doesn't return a vector of strings
	 * 
	 * @param directoryName directory for search for files in
	 * @return TSD filenames within the directory
	 */
	private Vector<Object> recurseTSDFiles(String directoryName){
		
		Vector<Object> filenames = new Vector<Object>();
		
		filenames.add(new File(directoryName).getName());
		
		for (String s : new File(directoryName).list()){
			
			String fullFileName = directoryName + File.separator + s;
			File f = new File(fullFileName);
			
			if(s.endsWith(".tsd") && f.isFile()){
				filenames.add(s);
			}
			else if(f.isDirectory()){
				filenames.add(recurseTSDFiles(fullFileName));
			}
		}
		
		return filenames;
	}
	
	/**
	 * opens a treechooser of the TSD files, then loads and parses the selected TSD file
	 * 
	 * @throws ListChooser.EmptyListException
	 */
	private void prepareTSDFile(){
		
		pause();
	
		// if simID is present, go up one directory.
		String simPath = reb2sac.getSimPath();
		String simID = reb2sac.getSimID();
		
		if(!simID.equals("")){
			simPath = new File(simPath).getParent();
		}
		
		Vector<Object> filenames = recurseTSDFiles(simPath);
		
		String filename;
		
		try{
			filename = TreeChooser.selectFromTree(Gui.frame, filenames, "Choose a simulation file");
		}
		catch(TreeChooser.EmptyTreeException e){
			JOptionPane.showMessageDialog(Gui.frame, "Sorry, there aren't any simulation files. Please simulate then try again.");
			return;
		}
		
		if(filename == null)
			return;
		
		String fullFilePath = reb2sac.getRootPath() + filename;
		this.parser = new TSDParser(fullFilePath, false);
		
		slider.setMaximum(parser.getNumSamples()-1);
		slider.setValue(0);
		
		biosim.log.addText(fullFilePath + " loaded. " + 
				String.valueOf(parser.getData().size()) +
				" rows of data loaded.");
		
		loadPreferences();
	}
	
	
	//UI METHODS
	
	/**
	 * displays the schematic and the movie UI
	 */
	public void display(){
		
		schematic.display();

		if(isUIInitialized == false){
			this.addPlayUI();
			
			isUIInitialized = true;
		}
	}
	
	/**
	 * adds the toolbar at the bottom
	 */
	private void addPlayUI(){
		// Add the bottom menu bar
		JToolBar mt = new JToolBar();
		
		fileButton = Utils.makeToolButton("", "choose_simulation_file", "Choose Simulation", this);
		mt.add(fileButton);
		
		clearButton = Utils.makeToolButton("", "clearAppearances", "Clear Appearances", this);
		mt.add(clearButton);
		
		movieButton = new JToggleButton("Make Movie");
		movieButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent event) {
				
				if (movieButton.isSelected()) {
					
					if(parser == null){
						JOptionPane.showMessageDialog(Gui.frame, "Must first choose a simulation file.");
						movieButton.setSelected(false);
					} 
					else {

						outputFilename = Utility.browse(Gui.frame, null, null, JFileChooser.FILES_ONLY, "Save Movie", -1);
						
						if (outputFilename == null || outputFilename.length() == 0) {
							
							movieButton.setSelected(false);
							return;
						}
						
						movieButton.setText("Stop Movie");
						pause();
						playPauseButtonPress();
						initialSliderValue = slider.getValue();
						
						//disable all buttons and stuff
						fileButton.setEnabled(false);
						playPauseButton.setEnabled(false);
						rewindButton.setEnabled(false);
						singleStepButton.setEnabled(false);
						clearButton.setEnabled(false);
						slider.setEnabled(false);
					}
				}
				else {
					
					movieButton.setSelected(false);
					movieButton.setText("Make Movie");
					
					//remove all image files
					removeJPGs();
					pause();
					slider.setValue(0);					
					
					//enable the buttons and stuff
					fileButton.setEnabled(true);
					playPauseButton.setEnabled(true);
					rewindButton.setEnabled(true);
					singleStepButton.setEnabled(true);
					clearButton.setEnabled(true);
					slider.setEnabled(true);
				}			
			}
		});
		mt.add(movieButton);
		
		mt.addSeparator();
		
		rewindButton = Utils.makeToolButton("movie" + File.separator + "rewind.png", "rewind", "Rewind", this);
		mt.add(rewindButton);

		singleStepButton = Utils.makeToolButton("movie" + File.separator + "single_step.png", "singlestep", "Single Step", this);
		mt.add(singleStepButton);
		
		playPauseButton = Utils.makeToolButton("movie" + File.separator + "play.png", "playpause", "Play", this);
		mt.add(playPauseButton);
			
		slider = new JSlider(SwingConstants.HORIZONTAL, 0, 100, 0);
		slider.setSnapToTicks(true);
		mt.add(slider);
		
		mt.setFloatable(false);

		this.add(mt, BorderLayout.SOUTH);
	}
	
	/**
	 * reloads the schematic's grid from file
	 * is called on an analysis view when the normal view/SBML is saved
	 */
	public void reloadGrid() {
		
		schematic.reloadGrid();
		
	}
	
	
	//EVENT METHODS
	
	/**
	 * event handler for when UI buttons are pressed.
	 */
	public void actionPerformed(ActionEvent event) {

		String command = event.getActionCommand();
		
		if(command.equals("rewind")){
			if(parser == null){
				JOptionPane.showMessageDialog(Gui.frame, "Must first choose a simulation file.");
			} 
			else {
				slider.setValue(0);
				updateVisuals();
			}
		}
		else if(command.equals("playpause")){
			if(parser == null){
				JOptionPane.showMessageDialog(Gui.frame, "Must first choose a simulation file.");
			} 
			else {
				playPauseButtonPress();
			}
		}
		else if(command.equals("singlestep")){
			if(parser == null){
				JOptionPane.showMessageDialog(Gui.frame, "Must first choose a simulation file.");
			} 
			else {
				nextFrame();
			}
		}
		else if(command.equals("choose_simulation_file")){
			prepareTSDFile();
		}
		else if(command.equals("clearAppearances")){
			
			movieScheme.clearAppearances();
			schematic.getGraph().buildGraph();
			this.setIsDirty(true);
		}
		else{
			throw new Error("Unrecognized command '" + command + "'!");
		}
	}
	
	
	/**
	 * event handler for when the timer ticks
	 */
	ActionListener playTimerEventHandler = new ActionListener() {
		public void actionPerformed(ActionEvent evt) {
			nextFrame();
		}
	};
	
	
	//MOVIE CONTROL METHODS
	
	/**
	 * switches between play/pause modes
	 * 
	 * Called whenever the play/pause button is pressed, or when the system needs to 
	 * pause the movie (such as at the end)
	 */
	private void playPauseButtonPress(){
		
		if(mode == PAUSED){
			
			if(slider.getValue() >= slider.getMaximum()-1)
				slider.setValue(0);
			
			playTimer.setDelay(FRAME_DELAY_MILLISECONDS);
			Utils.setIcon(playPauseButton, "movie" + File.separator + "pause.png");
			playTimer.start();
			mode = PLAYING;
		}
		else{
			
			Utils.setIcon(playPauseButton, "movie" + File.separator + "play.png");
			playTimer.stop();
			mode = PAUSED;
		}		
	}
	
	/**
	 * calls playpausebuttonpress to pause the movie
	 */
	private void pause(){
		
		if(mode == PLAYING)
			playPauseButtonPress();
	}
	
	/**
	 * advances the movie to the next frame
	 */
	private void nextFrame(){
		
		//if the user wants output, print it to file
		if (movieButton.isSelected() && slider.getValue() > 0) {
			
			//un-zoom so that the frames print properly
			schematic.getGraph().getView().setScale(1.0);
			
			outputJPG();
						
			//if the simulation ends, generate the Movie file using ffmpeg
			//also, remove all of the image files created
			if (slider.getValue() + 1 >= slider.getMaximum()){
				
				outputMovie();
				
				movieButton.setSelected(false);
				movieButton.setText("Make Movie");
				
				//remove all image files
				pause();
				slider.setValue(0);					
				
				//enable the buttons and stuff
				fileButton.setEnabled(true);
				playPauseButton.setEnabled(true);
				rewindButton.setEnabled(true);
				singleStepButton.setEnabled(true);
				clearButton.setEnabled(true);
				slider.setEnabled(true);
			}
		}
		
		slider.setValue(slider.getValue()+1);
		
		if (slider.getValue() >= slider.getMaximum()){			
			pause();
		}
		
		updateVisuals();
	}
	
	/**
	 * updates the visual appearance of cells on the graph (ie, species, components, etc.)
	 * gets called when the timer ticks
	 */
	private void updateVisuals(){
		
		if(parser == null){
			throw new Error("NoSimFileChosen");
		}
		
		int frameIndex = slider.getValue();
		
		if(frameIndex < 0 || frameIndex > parser.getNumSamples()-1){
			throw new Error("Invalid slider value! It is outside the data range!");
		}
		
		HashMap<String, ArrayList<Double>> speciesTSData = parser.getHashMap();
		
		//loop through the species and set their appearances
		for (String speciesID : gcm.getSpecies()) {
			
			//make sure this species has data in the TSD file
			if (speciesTSData.containsKey(speciesID)) {
				
				System.err.println(speciesID);
				
				//get the component's appearance and send it to the graph for updating
				MovieAppearance speciesAppearance = 
					movieScheme.getAppearance(speciesID, GlobalConstants.SPECIES, frameIndex, speciesTSData);
				
				if (speciesAppearance != null)
					schematic.getGraph().setSpeciesAnimationValue(speciesID, speciesAppearance);
			}
		}
		
		//loop through the components and set their appearances
		for (long i = 0; i < gcm.getSBMLDocument().getModel().getNumParameters(); i++) {
			
			if (gcm.getSBMLDocument().getModel().getParameter(i).getId().contains("__locations")) {
				
				String[] splitAnnotation = gcm.getSBMLDocument().getModel().getParameter(i)
				.getAnnotationString().replace("<annotation>","")
				.replace("</annotation>","").replace("]]","").replace("[[","").split("=");
			
				//loop through all components in the locations parameter array
				for (int j = 1; j < splitAnnotation.length; ++j) {
					
					splitAnnotation[j] = splitAnnotation[j].trim();
					int commaIndex = splitAnnotation[j].indexOf(',');
					
					if (commaIndex > 0)
						splitAnnotation[j] = splitAnnotation[j].substring(0, splitAnnotation[j].indexOf(','));
					
					String submodelID = splitAnnotation[j];
					
					//get the component's appearance and send it to the graph for updating
					MovieAppearance compAppearance = 
						movieScheme.getAppearance(submodelID, GlobalConstants.COMPONENT, frameIndex, speciesTSData);
					
					if (compAppearance != null)
						schematic.getGraph().setComponentAnimationValue(submodelID, compAppearance);
				}
			}
		}
		
		//if there's a grid to set the appearance of
		if (gcm.getGrid().isEnabled()) {
			
			//loop through all grid locations and set appearances
			for (int row = 0; row < gcm.getGrid().getNumRows(); ++row) {
				for (int col = 0; col < gcm.getGrid().getNumCols(); ++col) {
					
					String gridID = "ROW" + row + "_COL" + col;
					
					//get the component's appearance and send it to the graph for updating
					MovieAppearance gridAppearance = 
						movieScheme.getAppearance(gridID, GlobalConstants.GRID_RECTANGLE, frameIndex, speciesTSData);
					
					if (gridAppearance != null)
						schematic.getGraph().setGridRectangleAnimationValue(gridID, gridAppearance);
				}
			}			
		}
		
		schematic.getGraph().refresh();	
	}

	/**
	 * creates an Movie using JPG frames of the simulation
	 */
	public void outputMovie() {
		
		String separator = "";
		
		if (File.separator.equals("\\"))
			separator = "\\\\";
		else
			separator = File.separator;
		
		String path = "";
		String movieName = "";

		if (outputFilename.contains(separator)) {
			
			path = outputFilename.substring(0, outputFilename.lastIndexOf(separator));
			movieName = outputFilename.substring(outputFilename.lastIndexOf(separator)+1, outputFilename.length());
		}
		
		if (movieName.contains(".")) {
			movieName = movieName.substring(0, movieName.indexOf("."));
		}
		
		String args = "";
		
		//if we're on windows, add "cmd" to the front of the command line argument
		if (System.getProperty("os.name").contains("Windows")) {
			
			args += "cmd ";
		}
		
		//args for ffmpeg
		args +=
			"ffmpeg " + "-y " +
			"-r " + "5 " +
			"-b " + "5000k " +
			"-i " + reb2sac.getRootPath() + separator + "%09d.jpg " +
			path + separator + movieName + ".mp4";
		//run ffmpeg to generate the Movie movie file
		try {					
			Process p = Runtime.getRuntime().exec(args, null, new File(reb2sac.getRootPath()));
			
			String line = "";
			
		    BufferedReader input =
		    	new BufferedReader(new InputStreamReader(p.getErrorStream()));
		    
		    while ((line = input.readLine()) != null) {
		    	biosim.log.addText(line);
		    }				    
		    
		    removeJPGs();    
		} 
		catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	/**
	 * creates a JPG of the current graph frame
	 */
	public void outputJPG() {
		
		String separator = "";
		
		if (File.separator.equals("\\"))
			separator = "\\\\";
		else
			separator = File.separator;
		
		String filenum = String.format("%09d", slider.getValue() - initialSliderValue);			
		schematic.outputFrame(reb2sac.getRootPath() + separator + filenum  + ".jpg");
	}
	
	/**
	 * removes all created JPGs
	 */
	private void removeJPGs() {
		
		String separator = "";
		
		if (File.separator.equals("\\"))
			separator = "\\\\";
		else
			separator = File.separator;
		
		//remove all created jpg files
	    for (int jpgNum = 0; jpgNum <= slider.getMaximum(); ++jpgNum) {
	    	
	    	String jpgNumString = String.format("%09d", jpgNum);				    	
	    	String jpgFilename = 
	    		reb2sac.getRootPath() + separator + jpgNumString + ".jpg";
		    File jpgFile = new File(jpgFilename);
		    
		    if (jpgFile != null && jpgFile.exists() && jpgFile.canWrite())
		    	jpgFile.delete();		    	
	    }
	}
	
	
	//PREFERENCES METHODS
	
	/**
	 * outputs the preferences file
	 */
	public void savePreferences(){

		Gson gson = new Gson();
		String out = gson.toJson(this.movieScheme.getAllSpeciesSchemes());
		
		String fullPath = getPreferencesFullPath();
		
		FileOutputStream fHandle;
		
		try {
			fHandle = new FileOutputStream(fullPath);
		} 
		catch (FileNotFoundException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(Gui.frame, "An error occured opening preferences file " + fullPath + "\nmessage: " + e.getMessage());
			return;
		}
		
		try {
			fHandle.write(out.getBytes());
		}
		catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(Gui.frame, "An error occured writing the preferences file " + fullPath + "\nmessage: " + e.getMessage());
		}
		
		try {
			fHandle.close();
		} 
		catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(Gui.frame, "An error occured closing the preferences file " + fullPath + "\nmessage: " + e.getMessage());
			return;
		}
		
		biosim.log.addText("file saved to " + fullPath);
		
		this.gcm2sbml.saveParams(false, "", true);
	}

	/**
	 * loads the preferences file if it exists and stores its values into the movieScheme object.
	 */
	public void loadPreferences(){
		
		// load the prefs file if it exists
		String fullPath = getPreferencesFullPath();
		String json = null;
		
		try {
			json = TSDParser.readFileToString(fullPath);
		} 
		catch (IOException e) {
		}
		
		if(json == null){
			
			if (movieScheme == null ||
					movieScheme.getAllSpeciesSchemes().length == 0)
				movieScheme = new MovieScheme();
		}
		else{
			
			Gson gson = new Gson();
			
			try{
				
				SerializableScheme[] speciesSchemes = gson.fromJson(json, SerializableScheme[].class);
				
				//if there's already a scheme, keep it
				if (movieScheme == null ||
						movieScheme.getAllSpeciesSchemes().length == 0) {
					
					movieScheme = new MovieScheme();
					movieScheme.populate(speciesSchemes, parser.getSpecies());
				}				
			}
			catch(Exception e){
				biosim.log.addText("An error occured trying to load the preferences file " + fullPath + " ERROR: " + e.toString());
			}
		}
	}
	

	//GET/SET METHODS
	
	public boolean getIsDirty(){
		return isDirty;
	}
	
	public void setIsDirty(boolean value) {
		isDirty = value;
	}
	
	public TSDParser getTSDParser() {
		return parser;
	}

	public ModelEditor getGCM2SBMLEditor() {
		return gcm2sbml;
	}
	
	private String getPreferencesFullPath(){
		String path = reb2sac.getSimPath();
		String fullPath = path + File.separator + "schematic_preferences.json";
		return fullPath;
	}

	public Schematic getSchematic() {
		return schematic;
	}

	public MovieScheme getMovieScheme() {
		return movieScheme;
	}
	
	public BioModel getGCM() {
		return gcm;
	}

}