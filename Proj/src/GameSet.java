import java.util.Arrays;
import java.util.Stack;


public class GameSet {
	
	static private boolean verbose = true;
	static private int     dTime   = 700;
	
	Ball[]  balls;
	int[][] field;
	int width, height;
	int levelMax = 3;
	
	public GameSet(int width, int height, int N) {
		this.width  = width;
		this.height = height;
		this.balls = new Ball[N];
		this.field = new int[width][height];
		for (int x=0; x<width; x++) for (int y=0; y<height; y++) field[x][y] = -1;
		for (int i = 0; i < N; i++) {
			int x = (int) (Math.random()*width);
			int y = (int) (Math.random()*width);
			while (field[x][y] >= 0) {
				x = (int) (Math.random()*width);
				y = (int) (Math.random()*width);
			}
			balls[i] = new Ball(x, y, 1 + (int) (Math.random()*levelMax));
			field[x][y] = i;
		}
	}
	
	/** Hits the i-th ball and processes chain reaction. **/
	public void hit(int i) {
		Stack<Particle> p = new Stack<Particle>(); 
		hit(i, p); 
		if (!p.empty()) destruction(p);
	}
	
	/** Hits the i-th ball. Destroys it and add creates particles to todo_particles. **/
	private void hit(int i, Stack<Particle> todo_particles) {
		if (--balls[i].level > 0) return;
		int x = balls[i].x, y = balls[i].y; 
		field[x][y] = -1;
		todo_particles.add(new Particle(x, y, Direction.LEFT,  this));
		todo_particles.add(new Particle(x, y, Direction.RIGHT, this));
		todo_particles.add(new Particle(x, y, Direction.DOWN,  this));
		todo_particles.add(new Particle(x, y, Direction.UP,    this));
	}
	
	/** Moves each particle once and destroys balls needing to be destroyed.
	 *  If needed, launch the next iteration of destruction. **/
	private void destruction(Stack<Particle> p) {
		if (verbose) print(p);
		Stack<Particle> p_todo = new Stack<Particle>();
		boolean[] to_hit = new boolean[balls.length];
		for (Particle p_ : p) {  // Move each particle
			if (p_.go()) continue;
			if (field[p_.x][p_.y] < 0) { p_todo.add(p_); continue; }
			to_hit[field[p_.x][p_.y]] = true;
		}
		for (int i = 0; i < balls.length; i++) {  // Hit each hit ball 
			if (to_hit[i]) hit(i, p_todo);
		}
		if (!p_todo.isEmpty()) destruction(p_todo);
	}
	
	public void print() {
		print(new Stack<Particle>());
	}
	public void print(Stack<Particle> particles) {
		System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
		boolean[][] particle = new boolean[width][height];
		for (Particle p : particles) particle[p.x][p.y] = true;
		String base = " +";
		for (int i = 0; i < width; i++) base += "---";
		base += "+";
		System.out.println(base);
		for (int y = 0; y < height; y++) {
			String line = " |";
			for (int x = 0; x < width; x++) {
				if (particle[x][y]) { line += " * "; continue; }
				if (field[x][y] >= 0 && balls[field[x][y]].level > 0)
					line += " " + balls[field[x][y]].level + " ";
				else
					line += "   ";
			}
			line += "|";
			System.out.println(line);
		}
		System.out.println(base);
		try { Thread.sleep(dTime); } catch (InterruptedException e) {}
	}
	
	public static void main(String[] a) {
		GameSet g = new GameSet(10,10,40);
		g.print();
		try { Thread.sleep(3000); } catch (InterruptedException e) {}
		while (g.balls[0].level > 0) g.hit(0);
		g.print();
	}
	
}
