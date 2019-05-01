package loadingdocks;

import java.awt.Color;
import java.awt.Point;
import java.util.List;

public class User extends Entity {

	private Core core;
	static int id_count = 0;
	int ID;

	Point target_position;
	Request myRequest;


	public User(Core core, Point init, Point target, Color color) {
		super(init, color);

		this.core = core;
		target_position = target;
		ID = id_count++;

		core.registerToCore(this);
		this.sendRequest();
	}
	
	/*****************************
	 ***** AUXILIARY METHODS ***** 
	 *****************************/

	public void sendRequest(){
		myRequest = new Request(ID,point,target_position);
		core.appendRequest(myRequest);
	}

	public void receiveOffers(List<Offer> offers){

	}

	public void pickUpUser(Point newpoint) {
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
