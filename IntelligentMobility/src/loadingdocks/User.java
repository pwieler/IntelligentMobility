package loadingdocks;

import java.awt.Color;
import java.awt.Point;
import java.util.List;

public class User extends Entity {

	public enum USER_STATE {WAITING,PICKED_UP,DELIVERED};
	public USER_STATE state = USER_STATE.WAITING;

	static int id_count = 0;
	int ID;
    private Board referenceToBoard;
    UserStrategy strategy;
	Point target_position;
	Request myRequest;
	int cluster;

	
	public Point getTarget_position() {
		return target_position;
	}
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
		myRequest = new Request(ID,point,target_position);
		Core.appendRequest(myRequest);
	}


	public boolean processOffers() {

		boolean match_state = false;

		try {
            // if no match --> take next offer --> until match!
		    while ( !match_state && myRequest.offers.size() != 0) {
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

                match_state = myRequest.offers.get(0).confirmMatch(myRequest);

                if (!match_state) {
                    myRequest.offers.remove(0);
                }


            }

		}catch (Exception e){

		}


		return match_state;
	}

	public void userPickedUp(){
		color = Color.BLUE;
		state = USER_STATE.PICKED_UP;
	}

	public void userDelivered(){
		color = Color.green;
		state = USER_STATE.DELIVERED;
	}


	public void moveUser(Point newpoint) {
		point = newpoint;
	}
	public void setCluster(int cluster) {
		this.cluster=cluster;
		
	}


}
