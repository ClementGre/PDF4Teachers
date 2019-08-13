package fr.themsou.utils;

public class Location {

	private int x = 0;
	private int y = 0;
	
	public Location(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}
	
	@Override
	public String toString() {
		return "Location [x=" + x + ", y=" + y + "]";
	}
	public boolean equals(Location loc){
		if(loc.x == x && loc.y == y) return true;
		return false;
	}
	

}
