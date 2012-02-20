public class Particle {

	int x, y;
	Direction dir;
	GameSet game;
	
	public Particle(int x, int y, Direction dir, GameSet game) {
		this.x = x; this.y = y; this.dir = dir; this.game = game;
	}
	
	/** Particule moves of 1 step. Return True if particules leaves the game's field. **/ 
	public boolean go() {
		switch (dir) {
			case LEFT:  x--; return (x < 0);
			case RIGHT: x++; return (x >= game.width);
			case DOWN:  y--; return (y < 0);
			case UP:    y++; return (y >= game.height);
		}
		throw new Error();
	}
	
}
