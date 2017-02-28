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
package frontend.biomodel.gui.util;

import java.util.ArrayList;

import javax.swing.JComponent;

/**
 * Objects that implement this interface changes state,
 * which affects objects that monitor this element.
 * @author Nam Nguyen
 * @organization University of Utah
 * @email namphuon@cs.utah.edu
 */
public interface StateElement {
	/**
	 * Adds a set of enable/disable elements
	 */
	public void addStateElements(ArrayList<JComponent> elements);
		
}
