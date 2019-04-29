package loadingdocks;

import java.awt.Color;
import java.awt.Point;

public class User extends Entity {

	public User(Point point, Color color) {
		super(point, color);
	}
	
	/*****************************
	 ***** AUXILIARY METHODS ***** 
	 *****************************/

	public void pickupUser(Point newpoint) {
		Board.removeEntity(point);
		point = newpoint;
	}
	
	public void dropUser(Point newpoint) {
		Board.insertEntity(this,newpoint);
		point = newpoint;
	}

	public void moveUser(Point newpoint) {
		point = newpoint;
	}
}
