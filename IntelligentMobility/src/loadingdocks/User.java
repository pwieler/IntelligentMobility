package loadingdocks;

import java.awt.Color;
import java.awt.Point;
import java.util.List;

public class User extends Entity {

	public enum USER_STATE {WAITING,INTERMEDIATE_STOP,PICKED_UP,DELIVERED};
	public USER_STATE state = USER_STATE.WAITING;

	public boolean MATCHED = false;
	public Agent matched_agent;

	public boolean DELIVER_MODE = false;


	Point intermediate_stop = new Point(-1,-1);
	int steps_to_intermediate_stop;


	static int id_count = 0;
	int ID;
    private Board referenceToBoard;
    UserStrategy strategy;
	Point target_position;
	Request myRequest;


	public User(Point init, Point target, Color color, Board boardReference) {
		super(init, color);
        referenceToBoard = boardReference;
		target_position = target;
		ID = id_count++;

		Core.registerToCore(this);
		this.sendRequest();
		strategy = UserStrategy.ShortestPickup;
	}
    public User(Point init, Point target, Color color, Board boardReference, UserStrategy userStrategy) {
        this(init,target,color,boardReference);
        strategy = userStrategy;
    }
	
	/*****************************
	 ***** AUXILIARY METHODS ***** 
	 *****************************/

	public void sendRequest(){

		if(point.equals(target_position)){
			userDelivered();
		}else{
			myRequest = new Request(ID,point,target_position);
			Core.appendRequest(myRequest);
		}
	}


	public boolean processOffers() {

		try {
            // if no match --> take next offer --> until match!
		    while ( !MATCHED && myRequest.offers.size() != 0) {
                if (strategy == UserStrategy.ShortestPickup) {
                    //choose offer with shortest euclidian distance (NOT shortest path, could also be an option)
                    myRequest.offers.sort((Agent offeringAgent1, Agent offeringAgent2) -> {

                        return referenceToBoard.pathLength(Board.shortestPath(offeringAgent1.point, this.point)) <
                                referenceToBoard.pathLength(Board.shortestPath(offeringAgent2.point, this.point)) ? -1 : 1;
                    });
                } else if (strategy == UserStrategy.Loner) {
                    //choose offer with as less current users  as possible (i want to be alone in the taxi!)
                    myRequest.offers.sort((Agent offeringAgent1, Agent offeringAgent2) -> {
                        return offeringAgent1.confirmed_users.size() <
                                offeringAgent2.confirmed_users.size() ? -1 : 1;
                    });
                }

				MATCHED = myRequest.offers.get(0).confirmMatch(myRequest);

                if (!MATCHED) {
                    myRequest.offers.remove(0);
                }else{
                	matched_agent = Core.agents.get(myRequest.matchedAgentID);
				}

            }

		}catch (Exception e){

		}

		return MATCHED;
	}

	public void userCooperationStart(Point intersection, int steps){
		System.out.println("cooperation start");
		intermediate_stop = intersection;
		steps_to_intermediate_stop = steps;
		color = Color.PINK;
		state = USER_STATE.INTERMEDIATE_STOP;
	}

	public void userCooperationEnd(){
		System.out.println("cooperation end: "+point);
		color = Color.PINK;
		state = USER_STATE.WAITING;
		intermediate_stop = new Point(-1,-1);
	}

	public void userPickedUp(){
		color = Color.BLUE;
		state = USER_STATE.PICKED_UP;
	}

	public boolean userDelivered(){
		color = Color.green;
		state = USER_STATE.DELIVERED;

		if(DELIVER_MODE){
			DELIVER_MODE = false;
			return true;
		}else{
			return false;
		}
	}


	public void moveUser(Point newpoint) {
		point = newpoint;
		if(state==USER_STATE.INTERMEDIATE_STOP){
			steps_to_intermediate_stop--;
		}
	}


}
