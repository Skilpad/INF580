/** inf580  - c.durr 2012 - ecole polytechnique

    Eternity utilisant Jacop

    Implemente un probleme de pavage dans Jacop pour illustrer cette
    bibliotheque.
 */

import java.util.*;

import JaCoP.constraints.*;
import JaCoP.core.*;
import JaCoP.search.*;

public class Eternity {
	////////////               ------- definition des tuiles
	// Legend //
	////////////
	// 0: Border
	// 1: Blue flower
	// 2: Yellow star
	// 3: Green ring
	// 4: Pink cross
	// 5: Violet snowflake
	// 6: Orange ninja dart
	// 7: Fucsia gem holder

	////////////
	// cotes  //
	////////////
	// 0: haut
	// 1: gauche
	// 2: bas
	// 3: droite

	static final int N = 6;  // dimension --- grille N*N
	static int [][] cp = // ------------------- couleurs des cotes des tuiles
	{{5, 2, 2, 5},  {5, 2, 5, 2},  {5, 2, 5, 4},  {5, 2, 2, 4},
		{5, 2, 5, 4},  {5, 5, 2, 4},  {2, 3, 0, 1},  {5, 2, 2, 5},
		{2, 5, 5, 5},  {6, 2, 6, 0},  {6, 5, 1, 0},  {0, 3, 2, 6},
		{6, 0, 7, 5},  {3, 7, 0, 0},  {0, 1, 2, 7},  {5, 6, 0, 1},
		{7, 0, 7, 5},  {6, 2, 1, 0},  {2, 5, 2, 5},  {0, 6, 2, 7},
		{2, 6, 0, 1},  {1, 5, 3, 0},  {2, 1, 0, 7},  {2, 2, 5, 5},
		{3, 2, 1, 0},  {0, 3, 3, 0},  {2, 5, 5, 5},  {0, 7, 2, 3},
		{4, 4, 5, 5},  {2, 2, 4, 2},  {7, 0, 6, 5},  {2, 2, 2, 5},
		{2, 2, 4, 5},  {0, 7, 1, 0},  {2, 5, 2, 2},  {0, 0, 3, 3}};

	// on code une tuile p avec rotation r dans un entier tuile-rotation 4*p+r
	// cpr[s][i] donne le cote s de tuile-rotation i
	static int[][]cpr = new int [4][N*N*4];

	static void init() {
		for (int s=0; s<4; s++) 
			for (int p=0; p<N*N; p++)
				for (int r=0; r<4; r++) 
					cpr[s][4*p+r] = cp[p][(s+r)%4];
	}	

	static Store store;
	static IntVar pr[][]; // le pavage

	static void model() {
		store = new Store();

		// h, v couleurs des bords des cases :
		// h[i][j] haut de case (i,j), v[i][j] gauche de la case (i,j)

		IntVar h[][] = new IntVar[N+1][N];
		IntVar v[][] = new IntVar[N][N+1];
		for (int i=0; i<N+1; i++)
			for (int j=0; j<N+1; j++) {
				String ij = "["+i+","+j+"]", hij="h"+ij, vij="v"+ij; 
				if ((i==0 || i==N)&& j<N)
					h[i][j] = new IntVar(store, hij, 0,0); // couleur du bord
				if ((j==0 || j==N) && i<N) 
					v[i][j] = new IntVar(store, vij, 0,0);
				if (i>0 && i<N && j<N)
					h[i][j] = new IntVar(store, hij, 1,7);
				if (j>0 && j<N && i<N)
					v[i][j] = new IntVar(store, vij, 1,7);
			}
		// p = tuiles 
		IntVar p[][]  = new IntVar[N][N];
		IntVar ptab[] = new IntVar[N*N];
		pr = new IntVar[N][N];
		for (int i=0; i<N; i++) 
			for (int j=0; j<N; j++) {
				ptab[i*N+j] = p[i][j]  = new IntVar(store,  "p["+i+","+j+"]", 0, N*N-1);
				pr[i][j] = new IntVar(store, "pr["+i+","+j+"]", 0, 4*N*N-1);
			} 
		store.impose(new Alldistinct(ptab));


		// relier pr et p : floor(pr[i][j] /4) = p[i][j]
		IntVar quatre = new IntVar(store, "4", 4,4);
		for (int i=0; i<N; i++) 
			for (int j=0; j<N; j++) 
				store.impose(new XdivYeqZ(pr[i][j], quatre, p[i][j]));


		// relier les couleurs des bords avec les tuiles-rotation
		// le parametre -1 indique que nos indices debutent a 0.
		// cette fonctionalite n'est decrite que dans les sources de Jacop
		for (int i=0; i<N; i++) 
			for (int j=0; j<N; j++) {
				store.impose(new Element(pr[i][j], cpr[0], h[i][j],   -1)); 
				store.impose(new Element(pr[i][j], cpr[1], v[i][j],   -1)); 
				store.impose(new Element(pr[i][j], cpr[2], h[i+1][j], -1)); 
				store.impose(new Element(pr[i][j], cpr[3], v[i][j+1], -1)); 
			}
	}	


	static void solve() {
		Search<IntVar> search = new DepthFirstSearch<IntVar>(); 
		SelectChoicePoint<IntVar> select = 
			new SimpleMatrixSelect<IntVar>(pr,
					new SmallestDomain(),
					new MostConstrainedDynamic(),
					new IndomainMin<IntVar>());
		boolean result = search.labeling(store, select);
	}


	static public void main(String args[]) {   
		init();
		model();
		solve();
	}
}
