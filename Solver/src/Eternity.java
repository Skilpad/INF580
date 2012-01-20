/* ecole polytechnique - inf580 - programmation par contraintes
 * c.durr - 2012 v3

   cet exemple de CSP sert a montrer l'avantage de l'arc consistance
   mais il y a encore un probleme soit avec le code (isValid),
   soit avec le codage des pieces.
 
   Eternity un jeu de pavage.
   k*k pieces carres doivent paver une grille de dimension k*k
   les couleurs de cotes des pieces doivent correspondre.
   la couleur du bord ne peut apparaitre qu'au bord.

   codage : val[x]/4 = piece en case x, val[x]%4 = rotation de cette piece

   ////////////
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
 */

public class Eternity extends CSP{
    
    static final int N = 6;  // dimension
    
    static int [][] c = 
    {{5, 2, 2, 5},{5, 2, 5, 2},{5, 2, 5, 4},{5, 2, 2, 4},
     {5, 2, 5, 4},{5, 5, 2, 4},{2, 3, 0, 1},{5, 2, 2, 5},
     {2, 5, 5, 5},{6, 2, 6, 0},{6, 5, 1, 0},{0, 3, 2, 6},
     {6, 0, 7, 5},{3, 7, 0, 0},{0, 1, 2, 7},{5, 6, 0, 1},
     {7, 0, 7, 5},{6, 2, 1, 0},{2, 5, 2, 5},{0, 6, 2, 7},
     {2, 6, 0, 1},{1, 5, 3, 0},{2, 1, 0, 7},{2, 2, 5, 5},
     {3, 2, 1, 0},{0, 3, 3, 0},{2, 5, 5, 5},{0, 7, 2, 3},
     {4, 4, 5, 5},{2, 2, 4, 2},{7, 0, 6, 5},{2, 2, 2, 5},
     {2, 2, 4, 5},{0, 7, 1, 0},{2, 5, 2, 2},{0, 0, 3, 3}};
                    
    public Eternity(int dom[][]) {
        super(dom);
    }

    public boolean isValid(int x, int u) { // unary relation
	int row = x/N, col=x%N;
	int p = u/4, rot=u%4;
        return ( (row==0)   == (c[p][rot]==0) &&
                 (col==0)   == (c[p][(rot+1)%4]==0) &&
                 (row==N-1) == (c[p][(rot+2)%4]==0) &&
                 (col==N-1) == (c[p][(rot+3)%4]==0) );
    }

    int count=0;
    public boolean isValid(int x, int u, int y, int v) {
    	assert(x!=y);
    	count++;
        assert(x!=y);
        count ++;
        int rowx = x/N, colx=x%N;  // cell
        int rowy = y/N, coly=y%N;
        int px   = u/4, rx=u%4;    // piece, orientation
        int py   = v/4, ry=v%4;
        return ( px!=py &&                         // pieces diff ?
                 (rowx!=rowy || colx!=coly-1 ||    // bord compatibles ?
                  c[px][(rx+3)%4]==c[py][(ry+1)%4]) &&  //   x y 
                 (rowx!=rowy || colx!=coly+1 || 
                  c[px][(rx+1)%4]==c[py][(ry+3)%4]) &&  //   y x
                 (colx!=coly || rowx!=rowy-1 ||                      // x
                  c[px][(rx+2)%4]==c[py][ry] ) &&                    // y
                 (colx!=coly || rowx!=rowy+1 ||           // y
                  c[px][rx]==c[py][(ry+2)%4] ));          // x
    }

    
    public static void main(String[] args){
        int [][] dom = new int[N*N][N*N*4];
        for (int x=0; x<N*N; x++)
            for (int u=0; u<N*N*4; u++)
                dom[x][u] = u;
        Eternity E = new Eternity(dom);
        E.solve();
	
    }

	public void print_possibility() {
	}

	public void print_result() {
		for (int row=0; row<N; row++) {		
			for (int s=0; s<4; s++) {
				for (int col=0; col<N; col++) {
					int x = row*N+col;
					int p = aff[x]/4, r = aff[x]%4;
					int top   = c[p][r];
					int left  = c[p][(r+1)%4];
					int bot   = c[p][(r+2)%4];
					int right = c[p][(r+3)%4];
					switch (s) {
					case 0: System.out.print(" "+top+" |"); break;
					case 1: System.out.print(""+left+" "+right+"|"); break;
					case 2: System.out.print(" "+bot+" |"); break;
					case 3: System.out.print("---+"); break;
					}
				}
				System.out.println();
			}
		}
		System.out.println("   "+count+" tests effectues");
	}

}
