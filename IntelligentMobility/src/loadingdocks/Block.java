package loadingdocks;

import java.awt.Color;

public class Block {

	public enum Shape { free, building }
	public Shape shape;
	public Color color;
	
	public Block(Shape shape, Color color) {
		this.shape = shape;
		this.color = color;
	}

}
