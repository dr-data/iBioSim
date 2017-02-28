/*******************************************************************************
 *  
 * This file is part of iBioSim. Please visit <http://www.async.ece.utah.edu/ibiosim>
 * for the latest version of iBioSim.
 *
 * Copyright (C) 2017 University of Utah
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the Apache License. A copy of the license agreement is provided
 * in the file named "LICENSE.txt" included with this software distribution
 * and also available online at <http://www.async.ece.utah.edu/ibiosim/License>.
 *  
 *******************************************************************************/
package backend.learn.genenet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpeciesCollection
{

	private List<String>			interestingSpecies;
	private Map<Integer, String>	columnSpecies;
	private Map<String, Integer>	speciesColumn;

	public SpeciesCollection()
	{
		interestingSpecies = new ArrayList<String>();
		columnSpecies = new HashMap<Integer, String>();
		speciesColumn = new HashMap<String, Integer>();
	}

	public void addSpecies(String id, int index)
	{
		speciesColumn.put(id, index);
		columnSpecies.put(index, id);
	}

	public void addInterestingSpecies(String id)
	{
		interestingSpecies.add(id);
	}

	public int getColumn(String species)
	{
		return speciesColumn.get(species);
	}

	public String getInterestingSpecies(int index)
	{
		return interestingSpecies.get(index);
	}

	public int size()
	{
		return interestingSpecies.size();
	}

	public List<String> getInterestingSpecies()
	{
		return interestingSpecies;
	}

}
