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
package edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.methods;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.Model;

import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.HierarchicalSimulation;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.model.HierarchicalModel;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.model.HierarchicalModel.ModelType;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.states.VectorWrapper;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.util.setup.ModelSetup;
import edu.utah.ece.async.ibiosim.dataModels.util.exceptions.BioSimException;

/**
 * 
 *
 * @author Leandro Watanabe
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public final class HierarchicalMixedSimulator extends HierarchicalSimulation
{

  private double fbaTime;
	private HierarchicalFBASimulator	fbaSim;
	private HierarchicalODERKSimulator	odeSim;
	private VectorWrapper wrapper;
	// private HierarchicalSimulation ssaSim;

	public HierarchicalMixedSimulator(String SBMLFileName, String rootDirectory, String outputDirectory, int runs, double timeLimit, double maxTimeStep, double minTimeStep, long randomSeed, double printInterval, double stoichAmpValue,
			String[] interestingSpecies, String quantityType, double initialTime, double outputStartTime) throws IOException, XMLStreamException, BioSimException
	{
		super(SBMLFileName, rootDirectory, outputDirectory, randomSeed, runs, timeLimit, maxTimeStep, minTimeStep, printInterval, stoichAmpValue, interestingSpecies, quantityType, initialTime, outputStartTime, SimType.MIXED);

	}

	@Override
	public void initialize(long randomSeed, int runNumber) throws IOException, XMLStreamException
	{
		if (!isInitialized)
		{
		  currProgress = 0;
			setCurrentTime(0);
			this.wrapper = new VectorWrapper(initValues); 

			
			ModelSetup.setupModels(this, ModelType.HODE, wrapper);
			computeFixedPoint();


      setupForOutput(runNumber);
			isInitialized = true;
		}

	}

	@Override
	public void simulate()
	{

		if (!isInitialized)
		{
			try
			{
				initialize(0, getCurrentRun());
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			catch (XMLStreamException e)
			{
				e.printStackTrace();
			}
		}
		double nextEndTime = currentTime.getValue(0);
    fbaTime = nextEndTime;
		double dt = getTopLevelValue("dt");

		while (nextEndTime < timeLimit)
		{
			nextEndTime = nextEndTime + getMaxTimeStep();

			if(nextEndTime > fbaTime)
			{
			  nextEndTime = fbaTime;
			}
			
			if (nextEndTime > printTime.getValue())
			{
				nextEndTime = printTime.getValue();
			}

			if (nextEndTime > getTimeLimit())
			{
				nextEndTime = getTimeLimit();
			}

			odeSim.setTimeLimit(nextEndTime);
			
			if(nextEndTime <= fbaTime)
			{
	      fbaSim.simulate();

        fbaTime = nextEndTime + dt;
			}

			computeAssignmentRules();

			odeSim.simulate();

			currentTime.setValue(nextEndTime);

			printToFile();
		}
	}

	@Override
	public void cancel()
	{
	}

	@Override
	public void clear()
	{
	}

	@Override
	public void setupForNewRun(int newRun)
	{
	  fbaTime = 0;
	}

	public void createODESim(HierarchicalModel topmodel, List<HierarchicalModel> odeModels) throws IOException, XMLStreamException
	{
			odeSim = new HierarchicalODERKSimulator(this, topmodel);
			odeSim.setListOfHierarchicalModels(odeModels);
	}

	public void createSSASim(HierarchicalModel topmodel, Map<String, HierarchicalModel> submodels)
	{
		// TODO:
	}

	public void createFBASim(HierarchicalModel topmodel, Model model)
	{
		fbaSim = new HierarchicalFBASimulator(this, topmodel);
		fbaSim.setFBA(model);
	}

	@Override
	public void printStatisticsTSD()
	{
		// TODO Auto-generated method stub

	}
	
	VectorWrapper getVectorWrapper()
	{
	  return this.wrapper;
	}

}
