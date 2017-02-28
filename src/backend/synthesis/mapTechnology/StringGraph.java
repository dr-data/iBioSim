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
package backend.synthesis.mapTechnology;

import java.util.HashMap;
import java.util.Map;

public class StringGraph
{
	private Map<Integer, Integer> _nodes; 
	public int stateNum = 0;
	
	public StringGraph()
	{
		_nodes = new HashMap<Integer, Integer>();
	}
	
	public void createStringGraph()
	{ 
		StringNode n = new StringNode(stateNum);
		_nodes.put(stateNum, stateNum);
		stateNum++;
		
	}

}
