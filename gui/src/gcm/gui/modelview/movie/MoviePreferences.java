package gcm.gui.modelview.movie;

import gcm.parser.GCMFile;
import gcm.util.GlobalConstants;

import java.util.HashMap;

import parser.TSDParser;

public class MoviePreferences {

	private HashMap<String, ColorScheme> speciesColorSchemes;
	private HashMap<String, ComponentScheme> componentSchemes;
	
	public MoviePreferences(){
		speciesColorSchemes = new HashMap<String, ColorScheme>();
		componentSchemes = new HashMap<String, ComponentScheme>();
	}
	
	/**
	 * Returns the ColorScheme for a given species. If such a color scheme doesn't exist
	 * then a new one will be created.
	 * @param species
	 * @return
	 */
	public ColorScheme getOrCreateColorSchemeForSpecies(String species, TSDParser tsdParser){
		
		ColorScheme cs = speciesColorSchemes.get(species);
		
		if(cs == null){
			cs = new ColorScheme(tsdParser);
			speciesColorSchemes.put(species, cs);
		}
		
		return cs;
	}
	
	/**
	 * Returns the ColorScheme for a given species. 
	 * @param species
	 * @return
	 */
	public ColorScheme getColorSchemeForSpecies(String species){
		
		ColorScheme cs = speciesColorSchemes.get(species);
		return cs;
	}
	
	/**
	 * Returns the ComponentScheme for a given component. If such a scheme doesn't exist
	 * then a new one will be created.
	 * @param species
	 * @param tsdParser: can be null
	 * @return
	 */
	public ComponentScheme getOrCreateComponentSchemeForComponent(String comp, TSDParser tsdParser){
		
		ComponentScheme cs = componentSchemes.get(comp);
		
		if(cs == null){
			cs = new ComponentScheme(tsdParser);
			componentSchemes.put(comp, cs);
		}
		
		return cs;
	}
	
	/**
	 * Returns the component scheme for a component or null if it doesn't exist.
	 * @param comp
	 * @return
	 */
	public ComponentScheme getComponentSchemeForComponent(String comp){
		
		ComponentScheme cs = componentSchemes.get(comp);
		return cs;
	}
	
	public void copyMoviePreferencesComponent(String masterComponentName, GCMFile gcm, TSDParser tsdParser){
		
		ComponentScheme masterScheme = this.getComponentSchemeForComponent(masterComponentName);
		String compGCMFileName = gcm.getComponents().get(masterComponentName).getProperty("gcm");
		
		for(String currentComponentName : gcm.getComponents().keySet()){
			// make sure this is the same type of component
			if(gcm.getComponents().get(currentComponentName).getProperty("gcm").equals(compGCMFileName)){
				
				if(!currentComponentName.equals(masterComponentName)){ // skip the current component
					
					ComponentScheme currentScheme = this.getOrCreateComponentSchemeForComponent(currentComponentName, tsdParser);
					currentScheme.duplicatePreferences(masterScheme, currentComponentName);
				}
			}
		}
	}
	
	public void copyMoviePreferencesSpecies(String masterSpeciesName, GCMFile gcm, TSDParser tsdParser){
		ColorScheme masterScheme = this.getOrCreateColorSchemeForSpecies(masterSpeciesName, tsdParser);
		for(String currentSpeciesName:gcm.getSpecies().keySet()){
			if(!currentSpeciesName.equals(masterSpeciesName)){
				ColorScheme currentScheme = this.getOrCreateColorSchemeForSpecies(currentSpeciesName, tsdParser);
				currentScheme.duplicatePreferences(masterScheme);
			}
		}
	}
	
}
