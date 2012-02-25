
import java.util.*;

import JaCoP.constraints.*;
import JaCoP.core.*;
import JaCoP.search.*;



public class JaCoP_solver {

	private static final int VOID  = -5;

	private int tmax = 500;
	private int maximalEnergy = 3;

	private int    height, width, size; 		// Dimensions of the field
	private Store  store;
	
	// Main variables
	private IntVar energy[][];
	private IntVar left[][];
	private IntVar right[][];
	private IntVar up[][];
	private IntVar down[][];
	private IntVar act[][];
	
	// Useful variables
	private IntVar              brutal_energy[][];
	private PrimitiveConstraint success[];		      // success[t] = true iff every atom is destroyed.
	private PrimitiveConstraint act_[];
	private PrimitiveConstraint expl[][];
	private PrimitiveConstraint empty[][];
	
	// Cost variable
	private IntVar cost[];

	
	public JaCoP_solver(int[][] initialField) {
		this.store = new Store();
		this.width  = initialField.length;
		this.height = initialField[0].length;
		this.size   = width*height;
		this.energy        = new IntVar[tmax][size];
		this.left          = new IntVar[tmax][size];
		this.right         = new IntVar[tmax][size];
		this.up            = new IntVar[tmax][size];
		this.down          = new IntVar[tmax][size];
		this.act           = new IntVar[tmax][size];
		this.brutal_energy = new IntVar[tmax][size];
		this.success       = new PrimitiveConstraint[tmax];
		this.act_          = new PrimitiveConstraint[tmax];
		this.expl          = new PrimitiveConstraint[tmax][size];
		this.empty         = new PrimitiveConstraint[tmax][size];
		this.cost          = new IntVar[tmax];
		IntervalDomain energyDomain = new IntervalDomain(1, maximalEnergy);
		energyDomain.addLastElement(VOID);
		for (int t = 0; t < tmax; t++) {
			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++) {
					this.energy        [t][x+y*width] = new IntVar(store, "Energy in ("+x+","+y+") at "+t,              energyDomain); 
					this.left          [t][x+y*width] = new IntVar(store, "Patricle to LEFT in ("+x+","+y+") at "+t,    0,1); 
					this.right         [t][x+y*width] = new IntVar(store, "Patricle to RIGHT in ("+x+","+y+") at "+t,   0,1); 
					this.up            [t][x+y*width] = new IntVar(store, "Patricle to UP in ("+x+","+y+") at "+t,      0,1); 
					this.down          [t][x+y*width] = new IntVar(store, "Patricle to DOWN in ("+x+","+y+") at "+t,    0,1); 
					this.act           [t][x+y*width] = new IntVar(store, "Action in ("+x+","+y+") at "+t,              0,1);
					this.brutal_energy [t][x+y*width] = new IntVar(store, "Brutal energy in ("+x+","+y+") at "+t, VOID, maximalEnergy+5);
				}
			}
			this.cost[t] = new IntVar(store, "Cost at "+t, 0, tmax);
		}
		defineNotImposedContraints();
		setSuccessConstraints();
		setInitialConstraints(initialField);
		setTransitionConstraints();
		setSingleReaction();
		setCostContraints();
	}
	
	
	/** Define constraints in success, act_, expl, empty.
	    Because they are use for imposed constraints, they must be defined before imposing constraints. **/
	private void defineNotImposedContraints() {
		
		for (int t = 0; t < tmax; t++)
			for (int x = 0; x < size; x++)
				expl[t][x] = new XgtC(brutal_energy[t][x], maximalEnergy);
		
		for (int x = 0; x < size; x++) {
			this.empty[0][x] = new XeqC(energy[0][x], VOID);
			for (int t = 1; t < tmax; t++) 
				this.empty[t][x] = or(empty[t-1][x], expl[t][x]);
		}
		
		for (int t = 0; t < tmax; t++)
			this.success[t] = new And(empty[t]);

		for (int t = 0; t < tmax; t++) {
			PrimitiveConstraint to_act[] = new PrimitiveConstraint[4*size+1];
			for (int x = 0; x < size; x++) {
				to_act[4*x  ] = new XeqC(left [t][x], 0);
				to_act[4*x+1] = new XeqC(right[t][x], 0);
				to_act[4*x+2] = new XeqC(up   [t][x], 0);
				to_act[4*x+3] = new XeqC(down [t][x], 0);
			}
			to_act[4*size] = new Not(success[t]);
			this.act_[t] = new And(to_act);
		}
		
	}
	
	
	/** Set initial constraints **/
	private void setInitialConstraints(int[][] initialField) {
		for (int x = 0; x < width; x++) for (int y = 0; y < height; y++)
			store.impose(new XeqC(energy       [0][x+y*width], (initialField[x][y] == 0) ? VOID : initialField[x][y]));
		for (int x = 0; x < size; x++) {
			store.impose(new XeqC(left         [0][x], 0));
			store.impose(new XeqC(right        [0][x], 0));
			store.impose(new XeqC(up           [0][x], 0));
			store.impose(new XeqC(down         [0][x], 0));
			store.impose(new XeqY(brutal_energy[0][x], energy[0][x]));   // To not let useless free variables
		}
	}
	
	
	/** Set the transition constraints **/
	private void setTransitionConstraints() { 

		for (int t = 1; t < tmax; t++) {
			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++) {
					int x_ = x+y*width;

			// Energy evolution in the cell
					
					ArrayList<IntVar> sum = new ArrayList<IntVar>();
					sum.add(energy[t-1][x_]);
					sum.add(act[t][x_]);
					if (x > 0) 	  	  sum.add(right[t][x_-1]);
					if (x < width-1)  sum.add(left [t][x_+1]);
					if (y > 0)	  	  sum.add(down [t][x_-width]);
					if (y < height-1) sum.add(up   [t][x_+width]);
					store.impose(new Sum(sum, brutal_energy[t][x_]));       // ## Defines brutal_energy
					
					store.impose(new IfThenElse(empty[t][x_], new XeqC(energy[t][x_], VOID), new XeqY(energy[t][x_],brutal_energy[t][x_])));     // ## Imposes energy equation
					
			// Particles created or passing through the cell
					
					store.impose(											// ## Imposes Left equation
							new IfThenElse(expl[t][x_], new XeqC(left[t][x_], 1),
													 (x < width-1) ?
															 new IfThenElse(empty[t][x_], new XeqY(left[t][x_], left[t-1][x_+1]), 
																	 					  new XeqC(left[t][x_], 0))
													 :		 new XeqC(left[t][x_], 0))
					);
					store.impose(
							new IfThenElse(expl[t][x_], new XeqC(right[t][x_],1),
													 (x > 0) ?
															 new IfThenElse(empty[t][x_], new XeqY(right[t][x_], right[t-1][x_-1]), 
																	 					  new XeqC(right[t][x_], 0))
													 :		 new XeqC(right[t][x_], 0))
					);
					store.impose(
							new IfThenElse(expl[t][x_], new XeqC(up[t][x_],1),
													 (y < height-1) ?
															 new IfThenElse(empty[t][x_], new XeqY(up[t][x_], up[t-1][x_+width]), 
																	 					  new XeqC(up[t][x_], 0))
													 :		 new XeqC(up[t][x_], 0))
					);
					store.impose(
							new IfThenElse(expl[t][x_], new XeqC(down[t][x_],1),
													 (y > 0) ?
															 new IfThenElse(empty[t][x_], new XeqY(down[t][x_], down[t-1][x_-width]), 
																	 					  new XeqC(down[t][x_], 0))
													 :		 new XeqC(down[t][x_], 0))
					);
					
				}
			}
		}
	}
	
	
	/** Forbids parallel chain reactions **/
	private void setSingleReaction() {
		
		// Act when required
		for (int t = 0; t < tmax; t++) {
			PrimitiveConstraint some_action[] = new PrimitiveConstraint[size];
			for (int x = 0; x < size; x++) some_action[x] = new XeqC(act[t][x], 1);
			store.impose(new IfThen(act_[t], new Or(some_action)));
		}
		
		// Act at most once
		for (int t = 0; t < tmax; t++) 
			for (int x = 0; x < size; x++)
				for (int y = 0; y < size; y++)
					if (x != y) store.impose(new IfThen(new XeqC(act[t][x], 1), new XeqC(act[t][y], 0)));
		
		//  Act only on non-empty cells
		for (int t = 0; t < tmax; t++) 
			for (int x = 0; x < size; x++)
				store.impose(new IfThen(empty[t][x], new XeqC(act[t][x], 0)));
		
		// Do not act when not required
		// BN: This part is redundant, but could fasten the solver.
		for (int t = 0; t < tmax; t++) 
			for (int x = 0; x < size; x++)
				store.impose(new IfThen(new Not(act_[t]), new XeqC(act[t][x], 0)));
		
	}
	
	
	/** Set constraints about success **/
	private void setSuccessConstraints() {
		store.impose(success[tmax-1]);			//	Final success is required
		for (int t = 1; t < tmax; t++)
			store.impose(new IfThen(success[t-1], success[t]));
	}
	
	
	/** Set constraints defining cost function. **/
	private void setCostContraints() {
		store.impose(new IfThenElse(act_[0], new XeqC(cost[0], 1), new XeqC(cost[0], 0)));
		for (int t = 1; t < tmax; t++)
			store.impose(new IfThenElse(act_[t], new XplusCeqZ(cost[t-1], 1, cost[t]), new XeqY(cost[t], cost[t-1])));
	}
	
	
	/** Solves the CSP. Returns the list of energy gifts. **/
	public LinkedList<int[]> solve() { return solve(0,0, false); }
	
	/** Solves the CSP using DFS. Returns the list of energy gifts.
	 *  Variable selection (which cell at which time we will chose to play or not) during research is chosen according to variable_selection_mode:
	 *    -  0  ->  MostConstrainedStatic,  then MostConstrainedDynamic.
	 *    -  1  ->  SmallestDomain,         then MostConstrainedDynamic.
	 *    -  2  ->  MostConstrainedDynamic, then MostConstrainedStatic.
	 *  Value selection (if the selected cell at the selected instant is played or not) during research is chosen according to variable_selection_mode:
	 *    -  0  ->  By default, the cell is played.
	 *    -  1  ->  By default, the cell is not played.
	 *  Once a potentially non-optimal solution (with cost found_cost) is found: 
	 *    -  if restart_search = true  ->  the DFS is restarted from the beginning, with additional constraint cost < found_cost.
	 *    -  else                      ->  the DFS is continued, with additional constraint cost < found_cost.
	 **/
	public LinkedList<int[]> solve(int variable_selection_mode, int value_selection_mode, boolean restart_search) {
		
		Search<IntVar> label = new DepthFirstSearch<IntVar>();
		
		ComparatorVariable<IntVar> varSelect;
		ComparatorVariable<IntVar> varSelect_;
		Indomain<IntVar>           valSelect;
		
		switch (variable_selection_mode) {
			case 1:
				varSelect  = new SmallestDomain<IntVar>();
				varSelect_ = new MostConstrainedDynamic<IntVar>();
				break;
			case 2:
				varSelect  = new MostConstrainedDynamic<IntVar>();
				varSelect_ = new MostConstrainedStatic<IntVar>();
				break;
			default:
				varSelect  = new MostConstrainedStatic<IntVar>();
				varSelect_ = new MostConstrainedDynamic<IntVar>();
				break;
		}
		
		switch (value_selection_mode) {
		case 1:
			valSelect  = new IndomainMin<IntVar>();
			break;
		default:
			valSelect  = new IndomainMax<IntVar>();
			break;
		}

		SelectChoicePoint<IntVar> select = new SimpleMatrixSelect<IntVar>(act, varSelect, varSelect_, valSelect);
		
		boolean success = false;
		
		if (restart_search) {
			throw new Error("To be completed");
		} else {
			success = label.labeling(store, select, cost[tmax-1]);
		}
		
		if (!success) return null;
		
		// TODO: Return result
		throw new Error("To be completed");
		
	}
	
	
	
	//==============//
	//    Useful    //
	//==============//
		
	/** Produces primitive constraint  ( a || b ). **/
	private PrimitiveConstraint or(PrimitiveConstraint a, PrimitiveConstraint b) {
		PrimitiveConstraint t[] = {a,b};
		return new Or(t);
	}
	
	/** Produces primitive constraint  ( a && b ). **/
	private PrimitiveConstraint and(PrimitiveConstraint a, PrimitiveConstraint b) {
		PrimitiveConstraint t[] = {a,b};
		return new And(t);
	}
	
}
