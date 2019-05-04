package loadingdocks;

import java.awt.Color;
import java.awt.Point;
import java.util.List;

public class User extends Entity {

	static int id_count = 0;
	int ID;

	Point target_position;
	Request myRequest;


	public User(Point init, Point target, Color color) {
		super(init, color);

		target_position = target;
		ID = id_count++;

		Core.registerToCore(this);
		this.sendRequest();
	}
	
	/*****************************
	 ***** AUXILIARY METHODS ***** 
	 *****************************/

	public void sendRequest(){
		myRequest = new Request(ID,point,target_position);
		Core.appendRequest(myRequest);
	}


	public boolean processOffers() {

		boolean match_state = false;

		try{
			//choose offer with shortest euclidian distance (NOT shortest path, could also be an option)
			myRequest.offers.sort( (Agent offeringAgent1, Agent offeringAgent2 ) -> {
				return offeringAgent1.point.distance(this.point) <
						offeringAgent2.point.distance(this.point) ? -1 : 1;
			});
			//choose offer with as less current users as possible (i want to be alone in the taxi!)
			myRequest.offers.sort( (Agent offeringAgent1, Agent offeringAgent2 ) -> {
				return offeringAgent1.confirmed_users.size() <
						offeringAgent2.confirmed_users.size() ? -1 : 1;
			});

			match_state = myRequest.offers.get(0).confirmMatch(myRequest);

			if(!match_state){
				myRequest.offers.remove(0);
			}

			// if no match --> take next offer --> until match!

		}catch (Exception e){

		}


		return match_state;
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
