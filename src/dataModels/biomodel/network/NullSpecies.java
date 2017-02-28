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
package dataModels.biomodel.network;

import dataModels.biomodel.visitor.SpeciesVisitor;

/**
 * This class is used to represent null species.  Null species
 * are a construct created to handle special reactions where
 * production/degradation reactions are not created.
 * @author Nam Nguyen
 * @organization University of Utah
 * @email namphuon@cs.utah.edu
 */
public class NullSpecies extends AbstractSpecies {

	public NullSpecies() {
		super();
		setId("Null");
		setStateName("Null");
	}
	
	@Override
	public void accept(SpeciesVisitor visitor) {
	}

}
