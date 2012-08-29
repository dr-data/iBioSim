package verification.platu.partialOrders;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import verification.platu.main.Options;

import lpn.parser.ExprTree;
import lpn.parser.LhpnFile;
import lpn.parser.Place;
import lpn.parser.Transition;

public class StaticSets {
	private Transition curTran;
	private HashMap<Integer, Transition[]> allTransitions;
	private HashSet<LpnTransitionPair> disableSet; 
	private HashSet<LpnTransitionPair> disableByStealingToken;
	private HashSet<LpnTransitionPair> enableSet;
	private HashSet<LpnTransitionPair> curTranDisableOtherTranFailEnablingCond; // A set of transitions (with associated LPNs) whose enabling condition can become false due to executing curTran's assignments. 
	private HashSet<LpnTransitionPair> otherTranDisableCurTranFailEnablingCond; // A set of transitions (with associated LPNs) whose enabling condition can become false due to executing curTran's assignments.
	private HashSet<LpnTransitionPair> modifyAssignment;
	
	public StaticSets(Transition curTran, HashMap<Integer,Transition[]> allTransitions) {
		this.curTran = curTran;
		this.allTransitions = allTransitions;
		disableSet = new HashSet<LpnTransitionPair>();
		disableByStealingToken = new HashSet<LpnTransitionPair>();
		curTranDisableOtherTranFailEnablingCond = new HashSet<LpnTransitionPair>();
		otherTranDisableCurTranFailEnablingCond = new HashSet<LpnTransitionPair>();
		enableSet = new HashSet<LpnTransitionPair>();
		modifyAssignment = new HashSet<LpnTransitionPair>();
	}
		
	/**
	 * Build a set of transitions that curTran can disable.
	 * @param curLpnIndex 
	 */
	public void buildCurTranDisableOtherTransSet(int curLpnIndex) {
		// Test if curTran can disable other transitions by stealing their tokens.
		if (curTran.hasConflictSet()) {
			if (!tranFormsSelfLoop()) {
				Set<Integer> conflictSet = curTran.getConflictSetTransIndices();
				conflictSet.remove(curTran.getIndex());
				for (Integer i : conflictSet) {
					LpnTransitionPair lpnTranPair = new LpnTransitionPair(curLpnIndex, i);
					disableByStealingToken.add(lpnTranPair);
				}
			}
		}
		// Test if curTran can disable other transitions by executing its assignments
		if (!curTran.getAssignments().isEmpty()) {
			for (Integer lpnIndex : allTransitions.keySet()) {
				Transition[] allTransInOneLpn = allTransitions.get(lpnIndex);
				for (int i = 0; i < allTransInOneLpn.length; i++) {
					if (curTran.equals(allTransInOneLpn[i]))
						continue;
					Transition anotherTran = allTransInOneLpn[i];
					ExprTree anotherTranEnablingTree = anotherTran.getEnablingTree();
					if (anotherTranEnablingTree != null
							&& (anotherTranEnablingTree.getChange(curTran.getAssignments())=='F'
							 || anotherTranEnablingTree.getChange(curTran.getAssignments())=='f'
							 || anotherTranEnablingTree.getChange(curTran.getAssignments())=='X')) {
						if (Options.getDebugMode()) {
							System.out.println(curTran.getName() + " can disable " + anotherTran.getName() + ". " 
						                       + anotherTran.getName() + "'s enabling condition, which is " + anotherTranEnablingTree + ", may become false due to firing of " 
									           + curTran.getName() + ".");
							if (anotherTranEnablingTree.getChange(curTran.getAssignments())=='F') 
								System.out.println("Reason is " + anotherTran.getName() + "_enablingTree.getChange(" + curTran.getName() + ".getAssignments()) = F.");
							if (anotherTranEnablingTree.getChange(curTran.getAssignments())=='f') 
								System.out.println("Reason is " + anotherTran.getName() + "_enablingTree.getChange(" + curTran.getName() + ".getAssignments()) = f.");
							if (anotherTranEnablingTree.getChange(curTran.getAssignments())=='X') 
								System.out.println("Reason is " + anotherTran.getName() + "_enablingTree.getChange(" + curTran.getName() + ".getAssignments()) = X.");					
						}
						curTranDisableOtherTranFailEnablingCond.add(new LpnTransitionPair(lpnIndex, anotherTran.getIndex()));
					}
				}
			}
		}
		disableSet.addAll(disableByStealingToken);
		disableSet.addAll(curTranDisableOtherTranFailEnablingCond);
//		printIntegerSet(disableByStealingToken, "disableByStealingToken");
//		printIntegerSet(disableByFailingEnableCond, "disableByFailingEnableCond");
	}
	
	/**
	 * Build a set of transitions that disable curTran.
	 * @param curLpnIndex 
	 */
	public void buildOtherTransDisableCurTranSet(int curLpnIndex) {
				// Test if other transition(s) can disable curTran by stealing their tokens.
				if (curTran.hasConflictSet()) {
					if (!tranFormsSelfLoop()) {
						Set<Integer> conflictSet = curTran.getConflictSetTransIndices();
						conflictSet.remove(curTran.getIndex());
						for (Integer i : conflictSet) {
							LpnTransitionPair lpnTranPair = new LpnTransitionPair(curLpnIndex, i);
							disableByStealingToken.add(lpnTranPair);
						}
					}
				}
				// Test if other transitions can disable curTran by executing their assignments
				if (!curTran.isPersistent()) {
					for (Integer lpnIndex : allTransitions.keySet()) {
						Transition[] allTransInOneLpn = allTransitions.get(lpnIndex);
						for (int i = 0; i < allTransInOneLpn.length; i++) {
							if (curTran.equals(allTransInOneLpn[i]))
								continue;
							Transition anotherTran = allTransInOneLpn[i];
							if (!anotherTran.getAssignments().isEmpty()) {
								ExprTree curTranEnablingTree = curTran.getEnablingTree();
								if (curTranEnablingTree != null
										&& (curTranEnablingTree.getChange(anotherTran.getAssignments())=='F'
										 || curTranEnablingTree.getChange(anotherTran.getAssignments())=='f'
										 || curTranEnablingTree.getChange(anotherTran.getAssignments())=='X')) {
									if (Options.getDebugMode()) {
										System.out.println(curTran.getName() + " can be disabled by " + anotherTran.getName() + ". " 
									                       + curTran.getName() + "'s enabling condition, which is " + curTranEnablingTree +  ", may become false due to firing of " 
												           + anotherTran.getName() + ".");
										if (curTranEnablingTree.getChange(anotherTran.getAssignments())=='F') 
											System.out.println("Reason is " + curTran.getName() + "_enablingTree.getChange(" + anotherTran.getName() + ".getAssignments()) = F.");
										if (curTranEnablingTree.getChange(anotherTran.getAssignments())=='f') 
											System.out.println("Reason is " + curTran.getName() + "_enablingTree.getChange(" + anotherTran.getName() + ".getAssignments()) = f.");
										if (curTranEnablingTree.getChange(anotherTran.getAssignments())=='X') 
											System.out.println("Reason is " + curTran.getName() + "_enablingTree.getChange(" + anotherTran.getName() + ".getAssignments()) = X.");					
									}	
									otherTranDisableCurTranFailEnablingCond.add(new LpnTransitionPair(lpnIndex, anotherTran.getIndex()));
								}
							}
						}
					}
				}
				disableSet.addAll(disableByStealingToken);
				disableSet.addAll(otherTranDisableCurTranFailEnablingCond);
				buildModifyAssignSet();
				disableSet.addAll(modifyAssignment);
				otherTranDisableCurTranFailEnablingCond.addAll(modifyAssignment);
	}
	
	private boolean tranFormsSelfLoop() {
		boolean isSelfLoop = false;
		Place[] curPreset = curTran.getPreset();
		Place[] curPostset = curTran.getPostset();
		for (Place preset : curPreset) {
			for (Place postset : curPostset) {
				if (preset == postset) {
					isSelfLoop = true;
					break;
				}	
			}
			if (isSelfLoop)
				break;
		}
		return isSelfLoop;
	}

	/**
	 * Construct a set of transitions that can make the enabling condition of curTran true, by firing their assignments.
	 * @param lpnIndex 
	 */
	public void buildEnableSet() {
		for (Integer lpnIndex : allTransitions.keySet()) {
			Transition[] allTransInOneLpn = allTransitions.get(lpnIndex);
			for (int i = 0; i < allTransInOneLpn.length; i++) {
				if (curTran.equals(allTransInOneLpn[i]))
					continue;
				Transition anotherTran = allTransInOneLpn[i];
				ExprTree curTranEnablingTree = curTran.getEnablingTree();
				if (curTranEnablingTree != null
						&& (curTranEnablingTree.getChange(anotherTran.getAssignments())=='T'
						|| curTranEnablingTree.getChange(anotherTran.getAssignments())=='t'
						|| curTranEnablingTree.getChange(anotherTran.getAssignments())=='X')) {
					enableSet.add(new LpnTransitionPair(lpnIndex, anotherTran.getIndex()));
				}			
			}
		}
	}
	
	public void buildModifyAssignSet() {
		// modifyAssignment contains transitions (T) that satisfy at least one of the conditions below: for every t in T, 
	    // (1) intersection(VA(curTran), supportA(t)) != empty
		// (2) intersection(VA(t), supportA(curTran)) != empty
		// (3) intersection(VA(t), VA(curTran) != empty
		// VA(t) : set of variables being assigned in transition t.
		// supportA(t): set of variables appearing in the expressions assigned to the variables of t (r.h.s of the assignment).
		for (Integer lpnIndex : allTransitions.keySet()) {
			Transition[] allTransInOneLpn = allTransitions.get(lpnIndex);
			for (int i = 0; i < allTransInOneLpn.length; i++) {
				if (curTran.equals(allTransInOneLpn[i]))
					continue;
				Transition anotherTran = allTransInOneLpn[i];
				for (String v : curTran.getAssignTrees().keySet()) {
					for (ExprTree anotherTranAssignTree : anotherTran.getAssignTrees().values()) {
						if (anotherTranAssignTree != null && anotherTranAssignTree.containsVar(v)) {
							modifyAssignment.add(new LpnTransitionPair(lpnIndex, anotherTran.getIndex()));
						}					
					}
				}
				for (ExprTree exprTree : curTran.getAssignTrees().values()) {
					for (String v : anotherTran.getAssignTrees().keySet()) {
						if (exprTree != null && exprTree.containsVar(v)) {
							modifyAssignment.add(new LpnTransitionPair(lpnIndex, anotherTran.getIndex()));
						}
					}
				}
				for (String v1 : curTran.getAssignTrees().keySet()) {
					for (String v2 : anotherTran.getAssignTrees().keySet()) {
						if (v1.equals(v2)) {
							modifyAssignment.add(new LpnTransitionPair(lpnIndex, anotherTran.getIndex()));
						}					
					}
				}
			}
		}
	}
 
	public HashSet<LpnTransitionPair> getModifyAssignSet() {
		return modifyAssignment;
	}
	
	public HashSet<LpnTransitionPair> getDisableSet() {
		return disableSet;
	}
	
	public HashSet<LpnTransitionPair> getOtherTransDisableCurTranSet() {
		return otherTranDisableCurTranFailEnablingCond;
	}

	public HashSet<LpnTransitionPair> getDisableByStealingToken() {
		return disableByStealingToken;
	}

	public HashSet<LpnTransitionPair> getEnable() {
		return enableSet;
	}
	
	public Transition getTran() {
		return curTran;
	}
//	private void printIntegerSet(HashSet<Integer> integerSet, String setName) {
//		if (!setName.isEmpty())
//			System.out.print(setName + ": ");
//		if (integerSet == null) {
//			System.out.println("null");
//		}
//		else if (integerSet.isEmpty()) {
//			System.out.println("empty");
//		}
//		else {
//			for (Iterator<Integer> curTranDisableIter = integerSet.iterator(); curTranDisableIter.hasNext();) {
//				Integer tranInDisable = curTranDisableIter.next();
//				System.out.print(lpn.getAllTransitions()[tranInDisable] + " ");
//			}
//			System.out.print("\n");
//		}				
//	}
 }
