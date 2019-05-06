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
			for(Integer agent_id:myRequest.offers){
				match_state = Core.agents.get(agent_id).confirmMatch(myRequest);
				if(!match_state){
					myRequest.offers.remove(agent_id); //remove(agent_id);
				}else{
					break;
				}
			}



			// if no match --> take next offer --> until match!

		}catch (Exception e){

		}


		return match_state;
	}

	public void userPickedUp(){
		color = Color.BLUE;
	}

	public void userDelivered(){
		color = Color.green;
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
