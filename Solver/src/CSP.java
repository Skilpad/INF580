import java.util.Stack;


public abstract class CSP {
		
	protected int n;			// Nombre de variables du domaine
	
	protected int val[][];
	protected int size[][];
	private   int d;
	
	protected int FREE;
	protected int aff[];
	
	
	/** Returns if affectation u to x and v to y are consistent. **/
	abstract public boolean isValid(int x, int u, int y, int v);
	abstract public void  	print_possibility();
	abstract public void  	print_result();
	
	
	public CSP(int dom[][]) {
		this.n = dom.length;
		this.val  = new int[n][];
		this.size = new int[n][n];
		this.FREE = dom[0][0];
		for (int i = 0; i < n; i++) {
			this.val[i] = new int[dom[i].length];
			this.size[i][0] = n;
			for (int j = 0; j < dom[0].length; j++) {
				val[i][j] = dom[i][j];
				if (dom[i][j] < this.FREE) this.FREE = dom[i][j];
			}
		}
		this.FREE--;
		this.d = 0;
		this.aff  = new int[n];
		for (int i = 0; i < n; i++) this.aff[i] = this.FREE;		
	}
	
	
	/** Chose a free variable to assign, minimizing its domain size. 
	 *  Return -1 if no free variable. **/
	private int choseVar() {
		int r = -1;
		int s = n+1;
		for (int i = 0; i < n; i++)
			if (aff[i] == FREE && size[i][d] < s) {
				if (size[i][d] == 1) return i;
				s = size[i][d]; r = i; 
			} 
		return r;
	}
	
	
	/** Assigns u to x. Returns true iff no void domain is created. **/
	private boolean assign(int x, int u) {
		aff[x] = u;
		for (int y = 0; y < n; y++) {
			if (aff[y] != FREE) continue;
			// Réduisons le domaine de y
			size[y][d+1] = size[y][d];
			for (int j = size[y][d]-1; j >= 0; j--) {  // {val[y][j]} domaine actuel de y
				if (!isValid(x, u, y, val[y][j])) {    // On retire val[y][j] du domaine
					size[y][d+1]--;
					int v = val[y][j];
					val[y][j] = val[y][size[y][d+1]];
					val[y][size[y][d+1]] = v;
				}
			}
			if (size[y][d+1] == 0) return false;
		}
		d++;
		return true;
	}
	
	/** Unassigns x **/
	private void unassign(int x) {
		aff[x] = FREE;
		d--;
	}
	
	
	/** Solve CSP completing aff. Return true iff solution found. **/
	private boolean solve_(boolean verbose) {
		if (verbose) print_possibility();
		int x = choseVar();
		if (x < 0) return true;
		for (int i = 0; i < size[x][d]; i++) {
			if (!assign(x,val[x][i])) return false;
			if (solve_(verbose)) return true;
			unassign(x);
		}
		return false;
	}
	
	/** Solve CSP and print solution. Prints intermediary steps iff verbose == true. **/
	public void solve(boolean verbose) {
		if (solve_(verbose)) print_result(); else System.out.println("No solution found.");
	}
	
	/** Solve CSP and print solution. **/
	public void solve() {
		if (solve_(false)) print_result(); else System.out.println("No solution found.");
	}
	
	
	
	protected boolean isFree(int x) { return aff[x] == FREE; }

	protected boolean isInDomain(int x, int u) {
		if (aff[x] != FREE) return aff[x] == u;
		for (int i = 0; i < size[x][d]; i++)
			if (val[x][i] == u) return true;
		return false;
	}
	
	
}




