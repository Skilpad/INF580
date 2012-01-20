/* ecole polytechnique - inf580 - programmation par contraintes
 * c.durr - 2012 v1

 appeler avec un seul argument, consistant en les 81 caracteres de la grille,
 ou k^4 en general pour de grilles plus grandes.
 Tout caractere different de 1..9A..Z peut representer une case vide.
 
 si on appele sans argument alors une grille par defaut est utilisee.

 Exemples

 java Sudoku --A----C-----O-I-J--A-B-P-CGF-H---D--F-I-E----P--G-EL-H----M-J------E----C--G----I--K-GA-B---E-JD-GP--J-F----A---E---C-B--DP--O-E--F-M--D--L-K-A-C--------O-I-L-H-P-C--F-A--B------G-OD---J----HK---J----H-A-P-L--B--P--E--K--A--H--B--K--FI-C----F---C--D--H-N-

java Sudoku --C6--7-I-5O-A1--4-------2-J-D---A--------I5-----1-------M----3-2--EC-G8P---G---2N--DCM---LFJ3----E-N-O-----P84-GJL--7---3C-9-4-2-------A-OCHG---5------9--6P---8-53------K--IJF-AB---ICJ-------N--7--4--------E-M--IGK-6BD-------M-P--1H547--E-83L--B---6-KDF------9--2-P-18--5-L--1----GA-7--4K--9--E-O-H-P25-----D-----M-----J18----7L--C-2H---I6G--F--D-A-8AICG9---5----J--H-L-F--M-8--F-3-6-L--7-IE5-1--------J-1-GB---AMPF------L---31-L--4----2-D-OP--E--6--------F-CE-6HO-------D---5NG4-DO72-9--F3-M------8--PK2-J----1----L3--C----GC-5-BL-N--F----J9-----PA----9KM74-3-EPI-B-----1-FO-6-M8-PE-AB-9-K1G-7-N--DEDL1--5---6-M-NA---2--I7B
 */

public class Sudoku extends CSP {

    int k,k2; // dimension du jeu de Sudoku (normalement k=3)

    int count = 0; // nb d'appels à isValid

    // symboles, par exemple 1...F est utilise pour k=4
    static final String symb = "123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public Sudoku(String s) {
	super(parseGrid(s));
	k  = fourthRoot(s.length());
    }

    static int fourthRoot(int k4) {
	int k=1;
	while (k*k*k*k < k4) 
	    k++;
	assert(k*k*k*k == k4);
	return k;
    }

    public boolean isValid(int x, int u, int y, int v) {
	assert(x!=y);			   
	count++;
	int k2 = k*k;
	int rowx = x/k2, rowy = y/k2;
	int colx = x%k2, coly = y%k2;
	int blkx = (colx/k)*k + rowx/k;
	int blky = (coly/k)*k + rowy/k;
	return (rowx!=rowy && colx!=coly && blkx!=blky) || u!=v;
    }
    
    static int[][] parseGrid(String s) {
	int k = fourthRoot(s.length());
	int k2 = k*k;
	int k4 = k2*k2;
	int dom[][] = new int[k4][];
	System.out.println("Sodoku "+k2+" x "+k2);
	for (int x=0; x<k4; x++) {
	    int i = symb.indexOf(s.charAt(x));
	    if (i==-1) {                        // case vide
		dom[x] = new int[k2];
		for (int u=0; u<k2; u++)
		    dom[x][u] = u;
	    }
	    else {
		dom[x] = new int[1];            // singleton
		dom[x][0] = i;
	    }	    
	}
	return dom;	  
    }

    static String sample = 
	"__4__9__8" +
	"_3__5__1_" +
	"7__4__2__" +
	"3__8__1__" + 
	"_5_____9_" +
	"__6__1__2" +
	"__8__3__1" +
	"_2__4__5_" +
	"6__1__7__";

    public static void main(String[] args) {
	Sudoku S;
	if (args.length!=1)
	    S = new Sudoku(sample);
	else
	    S = new Sudoku(args[0]);
	S.solve();
    }

	public void print_possibility() {
		int k2 = k*k;
		for (int i=0; i<k2; i++) {
		    for (int j=0; j<k2; j++) {
			int x = i*k2+j;
			if (aff[x]>=0) 
			    System.out.print(symb.charAt(aff[x]));
			else
			    System.out.print('?'); // ne devrait jamais arriver
		    }
		    System.out.println();
		}
		System.out.println("   "+count+" tests effectues");
	}

	public void print_result() {
		print_possibility();
	}
}
