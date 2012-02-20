
import java.util.*;

import JaCoP.constraints.*;
import JaCoP.core.*;
import JaCoP.search.*;



public class JaCoP_solver {

	public boolean single_reaction = true;
	
	private static final int LEFT  = 0;
	private static final int RIGHT = 1;
	private static final int UP    = 2;
	private static final int DOWN  = 3;
	private static final int VOID  = -5;

	private int maximalTime = 500;
	private int maximalEnergy = 3;

	private int    height, width; 		// Dimensions of the field
	private Store  store;
	private IntVar energy[][][]; 		// energy[x][y][t] := energy of the atom in the cell (x,y) at t instant, -5 iff cell is void. (Thus, when cell is void, energy[x][y][t] + incoming energies < maximalEnergy. Less tests requires for explosion detection.)
	private IntVar neutrino[][][][]; 	// With d a direction, if the cell (x,y) contains a neutrino traveling in direction d at t instant, neutrino[x][y][d][t] = 1 , else neutrino[x][y][d][t] = 0.
	private IntVar action[][][];		// At t instant, if energy is given to cell (x,y), action[x][y][t] = 1 else action[x][y][t] = 0.

	
	public JaCoP_solver(int[][] initialField) {
		this.store = new Store();
		this.width  = initialField.length;
		this.height = initialField[0].length;
		this.energy   = new IntVar[width][height][maximalTime];
		this.neutrino = new IntVar[width][height][maximalTime][4];
		this.action   = new IntVar[width][height][maximalTime];
		IntervalDomain energyDomain = new IntervalDomain(1, maximalEnergy);
		energyDomain.addLastElement(VOID);
		for (int t = 0; t < maximalTime; t++) {
			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++) {
					this.energy[x][y][t] = new IntVar(store, "Energy("+x+","+y+") at "+t, energyDomain); 
					this.action[x][y][t] = new IntVar(store, "Action("+x+","+y+") at "+t, 0, 1); 
					this.neutrino[x][y][t][LEFT]  = new IntVar(store, "Patricle to LEFT in ("+x+","+y+") at "+t, 0,1); 
					this.neutrino[x][y][t][RIGHT] = new IntVar(store, "Patricle to LEFT in ("+x+","+y+") at "+t, 0,1); 
					this.neutrino[x][y][t][DOWN]  = new IntVar(store, "Patricle to LEFT in ("+x+","+y+") at "+t, 0,1); 
					this.neutrino[x][y][t][UP]    = new IntVar(store, "Patricle to LEFT in ("+x+","+y+") at "+t, 0,1); 
				}
			}
		}
		setInitialConstraints(initialField);
		setTransitionConstraints();
		if (single_reaction) setSingleReaction();
	}

	
	/** Set initial constraints **/
	private void setInitialConstraints(int[][] initialField) {
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				store.impose(new XeqC(energy[x][y][0], (initialField[x][y] == 0) ? VOID : initialField[x][y]));
				store.impose(new XeqC(neutrino[x][y][LEFT][0],  0));
				store.impose(new XeqC(neutrino[x][y][RIGHT][0], 0));
				store.impose(new XeqC(neutrino[x][y][UP][0],    0));
				store.impose(new XeqC(neutrino[x][y][DOWN][0],  0));
			}
		}
	}
	
	
	/** Set the transition constraints **/
	private void setTransitionConstraints() { 

		for (int t = 1; t < maximalTime; t++) {
			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++) {

			// Energy evolution in the cell
					
					IntVar brutalEnergy   = new IntVar(store, "Brutal energy on ("+x+","+y+") at "+t,   -5, maximalEnergy);
					ArrayList<IntVar> s = new ArrayList<IntVar>();
					s.add(energy[x][y][t-1]);
					s.add(action[x][y][t]);
					if (x > 0) 	  	  s.add(neutrino[x-1][y][RIGHT][t]);
					if (x < width-1)  s.add(neutrino[x+1][y][LEFT][t]);
					if (y > 0)	  	  s.add(neutrino[x][y-1][DOWN][t]);
					if (y < height-1) s.add(neutrino[x][y+1][UP][t]);
					store.impose(new Sum(s, brutalEnergy));
					// brutalEnergy = old energy + neutrino from each direction + action
					PrimitiveConstraint empty     = new XeqC(energy[x][y][t-1], VOID);
					PrimitiveConstraint explosion = new XgtC(brutalEnergy, maximalEnergy);
					store.impose(new IfThenElse(new Or(explosion,empty), new XeqC(energy[x][y][t], VOID), new XeqY(energy[x][y][t], brutalEnergy)));

			// Particles created or passing through the cell
					
					//    Particle going to LEFT = Explosion OR (void cell & particle coming from RIGHT)
					// => Particle going to LEFT = IF Explosion THEN True ELSE IF not void cell THEN False ELSE Particle coming from RIGHT
					store.impose(
							new IfThenElse(explosion, new XeqC(neutrino[x][y][t][LEFT],1), 				// If particle explodes, it creates a neutrino,
													 (x < width-1) ? 									// Else a neutrino could come from the right...
															 new IfThenElse(empty, new XeqY(neutrino[x][y][t][LEFT], neutrino[x+1][y][t-1][LEFT]), 
																	 			   new XeqC(neutrino[x][y][t][LEFT], 0))
													 :		 new XeqC(neutrino[x][y][t][LEFT], 0))		// But only if there is a cell to the right.
					);
					store.impose(
							new IfThenElse(explosion, new XeqC(neutrino[x][y][t][RIGHT],1),
													 (x > 0) ?
															 new IfThenElse(empty, new XeqY(neutrino[x][y][t][RIGHT], neutrino[x-1][y][t-1][RIGHT]), 
																	 			   new XeqC(neutrino[x][y][t][RIGHT], 0))
													 :		 new XeqC(neutrino[x][y][t][RIGHT], 0))
					);
					store.impose(
							new IfThenElse(explosion, new XeqC(neutrino[x][y][t][UP],1),
													 (y < height-1) ?
															 new IfThenElse(empty, new XeqY(neutrino[x][y][t][UP], neutrino[x][y+1][t-1][UP]), 
																	 			   new XeqC(neutrino[x][y][t][UP], 0))
													 :		 new XeqC(neutrino[x][y][t][UP], 0))
					);
					store.impose(
							new IfThenElse(explosion, new XeqC(neutrino[x][y][t][DOWN],1),
													 (y > 0) ?
															 new IfThenElse(empty, new XeqY(neutrino[x][y][t][DOWN], neutrino[x][y-1][t-1][DOWN]), 
																	 			   new XeqC(neutrino[x][y][t][DOWN], 0))
													 :		 new XeqC(neutrino[x][y][t][DOWN], 0))
					);
				}
			}

		}
	}
	
	
	/** Forbids parallel chain reactions **/
	private void setSingleReaction() {
		for (int t = 0; t < maximalTime; t++) {
			IntVar neutrinosCount = new IntVar(store, "Neutrinos Count at "+t, 0, 1);
			IntVar actCount       = new IntVar(store, "Actions Count at "+t, 0, 1);
			IntVar p[] = new IntVar[4*width*height];
			IntVar a[] = new IntVar[width*height];
			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++) {
					a[x+y*width] = action[x][y][t];
					p[4*(x+y*width)  ] = neutrino[x][y][t][LEFT];
					p[4*(x+y*width)+1] = neutrino[x][y][t][RIGHT];
					p[4*(x+y*width)+2] = neutrino[x][y][t][DOWN];
					p[4*(x+y*width)+3] = neutrino[x][y][t][UP];
				}
			}
			store.impose(new Sum(a, actCount));
			store.impose(new Sum(p, neutrinosCount));
			// If there are neutrinos, there is no action. Else, there exactly 1 action.
			// TODO: Maybe there is a better way to impose only 1 action that is not 0.
			// TODO: What is better? This:
//			store.impose(new IfThenElse(new XgtC(neutrinosCount, 0), new XeqC(actCount, 0), new XeqC(actCount, 1)));
			// TODO: Or this:
			store.impose(new IfThen(new XeqC(neutrinosCount, 0), new XeqC(actCount, 1)));
			for (int x = 0; x < width; x++)
				for (int y = 0; y < height; y++)
					store.impose(new IfThen(new XgtC(neutrinosCount, 0), new XeqC(action[x][y][t],0)));
			
		}
	}

	
	/** Solves the CSP. Returns the list of energy gifts. **/
	public LinkedList<int[]> solve() {
		LinkedList<int[]> res = new LinkedList<int[]>();
		Search<IntVar> search = new DepthFirstSearch<IntVar>(); 
		SelectChoicePoint<IntVar> select = 
			new SimpleMatrixSelect<IntVar>(pr,
					new SmallestDomain(),
					new MostConstrainedDynamic(),
					new IndomainMin<IntVar>());
		boolean result = search.labeling(store, select);
	}
	
}