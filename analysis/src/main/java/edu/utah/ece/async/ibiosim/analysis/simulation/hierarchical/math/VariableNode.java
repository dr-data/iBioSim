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
package edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.math;

import java.util.ArrayList;
import java.util.List;

import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.states.ValueState;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.states.HierarchicalState.StateType;

/**
 * 
 *
 * @author Leandro Watanabe
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class VariableNode extends HierarchicalNode
{

  protected boolean			isVariableConstant;
  protected boolean hasRule;
  protected boolean hasInitRule;
  protected boolean hasAmountUnits;
  protected boolean     isSetInitialValue;
  
  private List<ReactionNode>	reactionDependents;
  protected HierarchicalNode rateRule;

  public VariableNode(String name)
  {
    super(Type.NAME);
    this.name = name;
    this.hasAmountUnits = true;
  }

  public VariableNode(String name, StateType type)
  {
    super(Type.NAME);
    this.name = name;
    this.state = new ValueState();
    this.hasAmountUnits = true;
  }

  public VariableNode(VariableNode copy)
  {
    super(copy);
    this.name = copy.name;
    this.isVariableConstant = copy.isVariableConstant;
    this.hasAmountUnits = copy.hasAmountUnits;
  }

  public List<ReactionNode> getReactionDependents()
  {
    return reactionDependents;
  }

  public void addReactionDependency(ReactionNode dependency)
  {
    if (reactionDependents == null)
    {
      reactionDependents = new ArrayList<ReactionNode>();
    }
    reactionDependents.add(dependency);
  }

  public void setIsVariableConstant(boolean isConstant)
  {
    this.isVariableConstant = isConstant;
  }

  public boolean isVariableConstant()
  {
    return isVariableConstant;
  }

  @Override
  public void setRateValue(int index, double value)
  {
    if(!isVariableConstant)
    {
      state.setRateValue(index, value);
    }
  }

  @Override
  public void setValue(int index, double value)
  {
    if(!isVariableConstant)
    {
      state.getState(index).setStateValue(value);
    }
  }

  @Override
  public double computeRateOfChange(int index)
  {
    double rate = 0;
    if (rateRule != null)
    {
      rate = Evaluator.evaluateExpressionRecursive(rateRule, index);
      state.setRateValue(index, rate);
    }
    return rate;
  }

  public void setRateRule( HierarchicalNode rateRule)
  {
    this.rateRule = rateRule;
  }

  public HierarchicalNode getRateRule()
  {
    return rateRule;
  }


  @Override
  public String toString()
  {
    return name;
  }

  @Override
  public VariableNode clone()
  {
    return new VariableNode(this);
  }

  public void setHasRule(boolean hasRule)
  {
    this.hasRule = hasRule;
  }

  public boolean hasRule()
  {
    return hasRule;
  }

  public void setHasInitRule(boolean hasInitRule)
  {
    this.hasInitRule = hasInitRule;
  }

  public boolean hasInitRule()
  {
    return hasInitRule;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public boolean hasAmountUnits()
  {
    return this.hasAmountUnits;
  }
  
  public void setHasAmountUnits(boolean hasAmountUnits)
  {
    this.hasAmountUnits = hasAmountUnits;
  }
  
  public void setIsSetInitialValue(boolean isSetInitialValue)
  {
    this.isSetInitialValue = isSetInitialValue;
  }
  
  public boolean isSetInitialValue()
  {
    return this.isSetInitialValue;
  }
  
  
}
