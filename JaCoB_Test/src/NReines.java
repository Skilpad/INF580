/* ecole polytechnique - inf580 - programmation par contraintes
 * c.durr - 2012 v1
 */

public class NReines extends CSP{

	public NReines (int dom[][]) {
		super(dom);
	}

	public boolean isValid(int x, int u, int y, int v) {
		assert(x!=y);
		return u!=v && (x-y)!=(u-v) && (x-y)!=(v-u);
	}

	public void print_result() {
		System.out.println("\n");
		for (int x=0; x<n; x++) {
			System.out.print("       ");
			for (int u=0; u<n; u++) 
				System.out.print((u == aff[x]) ? " #" : " .");
			System.out.println();	
		}
		System.out.println("\n");
	}

	public void print_possibility() {
		for (int x=0; x<n; x++) {
			if (isFree(x)) {
				System.out.print("       ");
				for (int u=0; u<n; u++) 
					System.out.print((isInDomain(x, u)) ? " x" : " .");
				System.out.println();
			} else {
				System.out.print("       ");
				for (int u=0; u<n; u++) 
					System.out.print((u == aff[x]) ? " #" : " .");
				System.out.println();				
			}
		}
		System.out.println();
		System.out.println("#################################");
		System.out.println();
	}

	public static void main(String[] args){
		//		int n = Integer.parseInt(args[0]);
		int n = 10;
		int[][] dom = new int[n][n];
		for (int x=0; x<n; x++)
			for (int u=0; u<n; u++)
				dom[x][u] = u;
		NReines C = new NReines(dom);
		C.solve(true);
//		C.solve();
	}


}
