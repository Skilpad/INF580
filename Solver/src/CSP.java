import java.util.Arrays;



public abstract class CSP {
		
	protected int n;			// Nombre de variables du domaine
	
	protected int val[][];
	protected int size[][];
	private   int d;
	
	protected int FREE;
	protected int aff[];
	
	
	/** Returns if affectation u to x and v to y are consistent. **/
	abstract public boolean isValid(int x, int u, int y, int v);
			 public boolean isValid(int x, int u) { return true; }
	abstract public void  	print_possibility();
	abstract public void  	print_result();
	
	
	public CSP(int dom[][]) {
		this.n = dom.length;
		this.val  = new int[n][];
		this.size = new int[n][n];
		this.FREE = dom[0][0];
		for (int i = 0; i < n; i++) {
			this.val[i] = new int[dom[i].length];
			this.size[i][0] = dom[i].length;
			for (int j = 0; j < dom[i].length; j++) {
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
	
	
	/** Reduce domains including node consistency & arc consistency**/
	private void reduceDomains() {
		node_consistency();
		arc_consistency();
	}
	
	
	/** For x variable, makes i-th value leaving the domain **/
	private void invalidValue(int x, int i) {
		size[x][d]--;
		int v = val[x][i];
		val[x][i] = val[x][size[x][d]];
		val[x][size[x][d]] = v;		
	}
	
	/** Reduce domains with node consistency **/
	private void node_consistency() {
		for (int x = 0; x < n; x++) {
			for (int j = size[x][d]-1; j >= 0; j--) {  // {val[y][j]} domaine actuel de y
				if (!isValid(x, val[x][j])) {
					invalidValue(x, j);
				}
			}
		}
	}	

	/** Reduce domains with arc consistency (using AC3) **/
	private void arc_consistency() {
		boolean modif = true;
		boolean modified[]  = new boolean[n];
		boolean modified_[] = new boolean[n];  // new values
		Arrays.fill(modified , true);
		while (modif) {
			modif = false;
			for (int x = 0; x < n; x++) {
				modified_[x] = false;
				for (int y = 0; y < n; y++) {
					if (!modified[y]) continue;
					if (revise(x, y)) { modified_[x] = true; modif = true; }
				}
			}
			for (int x = 0; x < n; x++) modified[x] = modified_[x];
		}
	}
	
	/** Reduces domain of n for arc consistency. Return true iff the domain has been reduced. **/
	private boolean revise(int x, int y) {
		if (x == y) return false;
		boolean effect = false;
		for (int i = size[x][d]-1; i >= 0; i--) {  // u = val[x][i]
			boolean support = false;
			for (int j = size[y][d]-1; j >= 0; j--) {  // v = val[y][j]
				if (isValid(x, val[x][i], y, val[y][j])) { support = true; continue; }
			}
			if (!support) { invalidValue(x, i); effect = true; }
		}
		return effect;
	}
	

	/** Solve CSP completing aff. Return true iff solution found. **/
	private boolean solve_(boolean verbose) {
		if (verbose) print_possibility();
		int x = choseVar();
		if (x < 0) return true;
		for (int i = 0; i < size[x][d]; i++) {			
			if (assign(x,val[x][i]) && solve_(verbose)) return true;
			unassign(x);
		}
		return false;
	}
	
	/** Solve CSP and print solution. Prints intermediary steps iff verbose == true. **/
	public void solve(boolean verbose) {
		reduceDomains();
		if (solve_(verbose)) print_result(); else System.out.println("No solution found.");
	}
	
	/** Solve CSP and print solution. **/
	public void solve() {
		reduceDomains();
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
