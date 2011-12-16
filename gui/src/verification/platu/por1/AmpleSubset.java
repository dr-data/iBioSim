package verification.platu.por1;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import lpn.parser.Transition;

import verification.platu.lpn.LPN;
import verification.platu.lpn.LPNTran;
import verification.platu.lpn.LPNTranRelation;
import verification.platu.lpn.LpnTranList;
import verification.platu.stategraph.StateGraph;

public class AmpleSubset {
	
	private HashSet<Transition> allInterleavingTrans = new HashSet<Transition>();
	private HashMap<Transition, HashSet<Transition>> interleavingSet = new HashMap<Transition, HashSet<Transition>>();
	
	public HashMap<Transition,HashSet<Transition>> getIndepTranSet_FromState(StateGraph[] lpnList,
			                                                           LPNTranRelation lpnTranRelation)
   {
		HashMap<Transition,HashSet<Transition>> indepTranSet = new HashMap<Transition,HashSet<Transition>>();
		SearchDepFromState sg = new SearchDepFromState(lpnList,lpnTranRelation);
		indepTranSet = sg.getIndepTranSet();
		allInterleavingTrans = sg.getInterleavingTrans();
		interleavingSet = sg.getInterleavingSet();
		return indepTranSet;
   }
	
	public HashMap<Transition,HashSet<Transition>> getIndepTranSet_FromLPN(StateGraph[] lpnList)
    {
		HashMap<Transition,HashSet<Transition>> indepTranSet = new HashMap<Transition,HashSet<Transition>>();
		SearchDepFromLPN sg = new SearchDepFromLPN();
        sg.setIndep(lpnList);
        indepTranSet = sg.getIndepSet();
        allInterleavingTrans = sg.getAllInterleavingTrans();
        interleavingSet = sg.getInterleavingSet();
		return indepTranSet;
    }
	
   
	
	/**(POR)
	 * reduce enableList to ampleList
	 * @param lpnList
	 * @param stateVisited
	 * @param enabledArray
	 * @param allIndepSet
	 * @return
	 */
	public LpnTranList generateAmpleList(boolean stateVisited,
										String approach,
										LinkedList<Transition>[] enabledArray,
										HashMap<Transition,HashSet<Transition>> allIndepSet)
	{
		LpnTranList ampleList = new LpnTranList();
		LpnTranList enableTranSet = this.getEnableTranSet(enabledArray);
				
		if(enableTranSet.size()==0)
		{
			System.out.println("no enable transition,deadlock!");
			ampleList = enableTranSet;
		}
		else if(enableTranSet.size()==1)
		{
			ampleList = enableTranSet;
		}
		else
		{
			//cycle
	    	if(stateVisited == true)
	    	{
	    		ampleList = enableTranSet;
	    		return ampleList;
	    	}
	    	//exist independent transition
	    	if(allIndepSet == null || allIndepSet.size() == 0)
	    		ampleList = enableTranSet;
	    	else
	    	{
	    		LpnTranList interleavingEnabledSet = this.getInterleavingEnabledSet(enableTranSet, allInterleavingTrans);
	    		//System.out.println("interleavingEnabledSet.size():"+interleavingEnabledSet.size());
	    		
	    		if(interleavingEnabledSet.size() == 0)   //only consider set1 and set2
	    		{
	    			ampleList = this.getSubset_noInterleaving(enabledArray, allIndepSet);
	    			//System.out.println("ampleList.size():"+ampleList.size());
	    			return ampleList;
	    		}
	    		else //partition enable transition to four part:(1)interleaving(2)dep_interleaving(3)set1 and set2
	    		{
	    			/*
	    			System.out.print("interleaving trans:");
	    			for(LPNTran tran : interleavingEnabledSet)
	    			{
	    				System.out.print(tran.getFullLabel()+",");
	    			}
	    			System.out.println();
	    			*/
	    			ampleList = this.getSubset_withInterleaving(interleavingEnabledSet, enabledArray, allIndepSet,this.interleavingSet);
	    			return ampleList;
	    		}
	    	}
		}

		return ampleList;
	}
	
	/**
	 * get the interleaving transitions from the enable set
	 * @param enabledList
	 * @param interleavingSet
	 * @return
	 */
	private LpnTranList getInterleavingEnabledSet(LpnTranList enableTranSet, HashSet<Transition> interleavingSet)
	{
		LpnTranList interleavingEnabledSet = new LpnTranList();
		//System.out.println("interleavingSet.size():"+interleavingSet.size());
		if(interleavingSet != null)
		{
			for(Transition tran : enableTranSet)
			{
				if(interleavingSet.contains(tran))
					interleavingEnabledSet.add(tran);
			}
		}
		return interleavingEnabledSet;
	}
/*
	private LPNTranSet getInterleavingEnabledSet(LinkedList<LPNTran>[] remainEnabledList,
												 HashSet<LPNTran> interleavingSet)
	{
		LPNTranSet interleavingEnabledSet = null;
		for (int index = 0; index < remainEnabledList.length; index++) 
    	{
			if(!remainEnabledList[index].isEmpty())
			{
				LinkedList<LPNTran> enableSet = remainEnabledList[index];
				
				for(LPNTran t: enableSet)
				{
					if(interleavingSet.contains(t))
					{
						if(interleavingEnabledSet==null)
							interleavingEnabledSet = new LPNTranSet();
						interleavingEnabledSet.add(t);
						remainEnabledList[index].remove(t);
					}
				}
			}
    	}
		return interleavingEnabledSet;
	}
*/
	/**
	 * if the enabledSet has no interleaving transition, 
	 * we partition this enableSet to two independent subset and choose the small one.
	 * @param enabledList
	 * @param indepTranSet
	 * @return
	 */
	private LpnTranList getSubset_noInterleaving(LinkedList<Transition>[] enabledList,
			                                    HashMap<Transition,HashSet<Transition>> indepTranSet)
	{
		LpnTranList set1 = new LpnTranList();
		LpnTranList set2 = new LpnTranList();
		//order
		if(enabledList.length>1)
			this.order(enabledList);
		//partition set1 and set2
		for(int i=0;i<=enabledList.length-1;i++)
		{
			for(Transition tran : enabledList[i])
			{
				if(set1.isEmpty())
					set1.add(tran);
				else
				{
					HashSet<Transition> indep_tran = indepTranSet.get(tran);
					if(indep_tran == null)
						set1.add(tran);
					else
					{
						boolean dep_flag = false;
						for(Transition t : set1)
						{
							if(!indep_tran.contains(t))
							{
								dep_flag = true;
								break;
							}
						}
						if(dep_flag)
							set1.add(tran);
						else
							set2.add(tran);
					}
				}
			}
		}
		/*
		System.out.print("Set1:  ");
		for(LPNTran tran: set1)
		{
			System.out.print(tran.getFullLabel()+",");
		}
		System.out.println();
		System.out.print("Set2:  ");
		for(LPNTran tran: set2)
		{
			System.out.print(tran.getFullLabel()+",");
		}
		System.out.println();
		*/
		//set subset
		if(set1.size() > set2.size() && set2.size() != 0)
			return set2;
		else
			return set1;
	}
/**
 * partition to 3 part: 
 * (1)transition t1 dependent with interleaving and transition t2 dependent with t1
 * (2) set1 and set2
 * @param interleavingEnabledSet
 * @param remainEnabledList
 * @param indepTranSet
 * @return
 */
	private LpnTranList getSubset_withInterleaving(LpnTranList interleavingEnabledSet,
			                                      LinkedList<Transition>[] enabledList,
			                                      HashMap<Transition,HashSet<Transition>> indepTranSet,
			                                      HashMap<Transition, HashSet<Transition>> interleavingSet)
	{
		LpnTranList subset = new LpnTranList();
		LpnTranList dep_interleaving = new LpnTranList();
		LpnTranList set1 = new LpnTranList();
		LpnTranList set2 = new LpnTranList();
		//order
		if(enabledList.length>1)
			this.order(enabledList);
		//partition to 3 part               
		for(int i=0;i<=enabledList.length-1;i++)
		{
			for(Transition tran : enabledList[i])
			{
				if(interleavingEnabledSet.contains(tran))
					continue;
				
				HashSet<Transition> indep_tran = indepTranSet.get(tran);
				if(indep_tran == null)
					dep_interleaving.add(tran);
				else
				{
					boolean dep_interleavingTR = false;
					for(Transition interleaveTran : interleavingEnabledSet)
					{
						if(!indep_tran.contains(interleaveTran))
						{
							dep_interleavingTR = true;
							dep_interleaving.add(tran);
							break;
						}
					}
					//indep with interleaving. (1)consider indep with depset(2)consider set1 and set2
					if(dep_interleavingTR == false)
					{
						boolean dep_dep = false;
						if(!dep_interleaving.isEmpty()) 
						{
							for(Transition depTran : dep_interleaving)
							{
								if(!indep_tran.contains(depTran))
								{
									dep_dep = true;
									dep_interleaving.add(depTran);
									break;
								}
							}
						}
						//indep with dep_transition set	,consider set1 and set2
						if(dep_dep == false)
						{
							if(set1.isEmpty())
								set1.add(tran);
							else
							{
								boolean dep_flag = false;
								for(Transition t : set1)
								{
									if(!indep_tran.contains(t))
									{
										dep_flag = true;
										break;
									}
								}
								if(dep_flag)
									set1.add(tran);
								else
									set2.add(tran);
							}
						}	
					}
				}
			}
		}
		//choose independent subset
		if(!set1.isEmpty() || !set2.isEmpty())
		{
			//set subset
			if(set1.size() > set2.size() && set2.size() != 0)
				subset = set2;
			else
				subset = set1;
		}
		else//indep is empty
		{
			
			for(Transition tran : interleavingEnabledSet)
				set1.add(tran);
			for(Transition tran : dep_interleaving)
				set1.add(tran);
			return set1;
			
			//return this.subset_indepIsEmpty(interleavingEnabledSet, interleavingSet, dep_interleaving,indepTranSet);
		}
		return subset;
	}
	
	private LpnTranList subset_indepIsEmpty(LpnTranList interleavingEnabledSet,
									       HashMap<Transition, HashSet<Transition>> interleavingSet,
									       LpnTranList dep_interleaving,
										   HashMap<Transition,HashSet<Transition>> allIndepSet)
	{
		LpnTranList ready_interleaving = new LpnTranList();

		// if there exist interleaving pair
		for(Transition tran1 : interleavingEnabledSet)
		{
			HashSet<Transition> inter_setTran1 = interleavingSet.get(tran1);
			boolean existNotContain = false;
			for(Transition inter : inter_setTran1)
			{
				if(!interleavingEnabledSet.contains(inter))
				{
					existNotContain = true;break;
				}
			}
			//interleaving pair exist, can be fire first
			if(!existNotContain)
			{
				ready_interleaving.add(tran1);
				for(Transition inter : inter_setTran1)
					ready_interleaving.add(inter);
			}
		}
		
		if(!ready_interleaving.isEmpty())
		{
			if(!dep_interleaving.isEmpty())
			{
				LpnTranList dep = this.getDepOfInterleavingTrans(ready_interleaving, dep_interleaving, allIndepSet);
				for(Transition tran : dep)
					ready_interleaving.add(tran);
			}
			return ready_interleaving;
		}
		else
		{
			LpnTranList subset = this.getSubsetOfInterleavingTrans(interleavingEnabledSet);
			if(!dep_interleaving.isEmpty())
			{
				LpnTranList dep = this.getDepOfInterleavingTrans(subset, dep_interleaving, allIndepSet);
				for(Transition tran : dep)
					subset.add(tran);
			}
			return subset;
		}
	}
	/**
	 * if there does not exist ready_interleaving, 
	 * get subset of the interleaving
	 * @param interleavingEnabledSet
	 * @return
	 */
	private LpnTranList getSubsetOfInterleavingTrans(LpnTranList interleavingEnabledSet)
	{
		LpnTranList set1 = new LpnTranList();
		LpnTranList set2 = new LpnTranList();
		for(Transition tran : interleavingEnabledSet)
		{
			if(set1.isEmpty())
				set1.add(tran);
			else
			{
				boolean contain = false;
				for(Transition setTran : set1)
				{
					HashSet<Transition> inter_setTran = interleavingSet.get(setTran);
					if(inter_setTran.contains(tran))
					{
						contain = true;
						break;
					}
				}
				if(contain == true)
					set1.add(tran);
				else
					set2.add(tran);	
			}
		}
		
		if(set1.size() > set2.size() && set2.size() != 0)
			return set2;
		else
			return set1;
	}
	
	/**
	 * get the dependent transitions of the enableInterleaving transitions
	 * @param subInterleavingSet
	 * @param dep_interleaving
	 * @param allIndepSet
	 * @return
	 */
	private LpnTranList getDepOfInterleavingTrans(LpnTranList subInterleavingSet, LpnTranList dep_interleaving,
			                                     HashMap<Transition,HashSet<Transition>> allIndepSet)
	{
		//dependent on interleaving
		LpnTranList dep_sub = new LpnTranList();
		for(Transition tran : dep_interleaving)
		{
			HashSet<Transition> indep_tran = allIndepSet.get(tran);
			for(Transition tran_inter : subInterleavingSet)
			{
				if(!indep_tran.contains(tran_inter))
				{
					dep_sub.add(tran);break;
				}
			}
		}
		//dependent on dep_interleaving
		LpnTranList dep_dep = new LpnTranList();
		for(Transition tran : dep_interleaving)
		{
			if(!dep_sub.contains(tran))
			{
				HashSet<Transition> indep_tran = allIndepSet.get(tran);
				for(Transition tran_dep : dep_sub)
				{
					if(!indep_tran.contains(tran_dep))
					{
						dep_dep.add(tran);break;
					}
				}
			}
		}
		
		for(Transition dep : dep_dep)
		{
			dep_sub.add(dep);
		}
		return dep_sub;
	}
	
	/**
	 * order the enabledList: the smallest the first
	 * @param enabledList
	 */
	private void order(LinkedList<Transition>[] enabledList)
	{
		LinkedList<Transition> temp = new LinkedList<Transition>();
		for(int i=0;i<enabledList.length-1;i++)
		{
			for(int j=i+1;j<enabledList.length;j++)
			{
				if(enabledList[i].size()>enabledList[j].size())
				{
					temp = enabledList[j];
					enabledList[j] = enabledList[i];
					enabledList[i] = temp;
				}
			}
		}
	}
	
	private LpnTranList getEnableTranSet(LinkedList<Transition>[] enabledList)
	{
		LpnTranList enableTranSet = new LpnTranList();
		for (int index = 0; index < enabledList.length; index++) 
    	{
    		//System.out.println("index:"+index);
			if(enabledList[index].size()!=0)
			{
				for(Transition t: enabledList[index])
					enableTranSet.add(t);
			}
    	}
		//System.out.println("enableTranSet.size():"+enableTranSet.size());
		return enableTranSet;
	}
	
	public void printMap(HashMap<Transition, HashSet<Transition>> tranSet)
	{
		long number = 0;
		Iterator it = tranSet.keySet().iterator();
		if(!it.hasNext())
		{
			System.out.println("this transition type is null");
		}
		while(it.hasNext())
		{
			Transition key = (Transition)it.next();
			HashSet<Transition> value = tranSet.get(key);
			System.out.print(key.getFullLabel()+"   " );
			Iterator bb = value.iterator();
			while(bb.hasNext())
			{
				number++;
				System.out.print(((Transition)bb.next()).getFullLabel()+",");
			}
			System.out.println();
		}
		System.out.println("number"+number);
	}
	
}