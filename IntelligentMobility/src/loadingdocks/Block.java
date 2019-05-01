package loadingdocks;

import java.awt.Color;

public class Block {

	public enum Type { target_location,free, building,pickup }
	public Type type;
	public Color color;
	
	public Block(Type type, Color color) {
		this.type = type;
		this.color = color;
	}

}
