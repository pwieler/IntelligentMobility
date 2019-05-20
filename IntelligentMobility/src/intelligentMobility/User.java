package intelligentMobility;

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
		if(point.equals(target_position)){
			userDelivered();
		}else{
			myRequest = new Request(this,point,target_position);
			Core.appendRequest(myRequest);
		}
	}


	public boolean processOffers() {

		try {
            // if no match --> take next offer --> until match!
		    while ( !MATCHED && myRequest.offers.size() != 0) {
		    	Agent bestOffer = myRequest.offers.get(0); //default
                if (strategy == UserStrategy.ShortestPickup) {
					bestOffer = new ShortestPickupOfferUtilityCalculator(myRequest.offers,referenceToBoard,this.point).calculateMaxUtility();
                } else if (strategy == UserStrategy.Loner) {
					bestOffer = new LonerOfferUtilityCalculator(myRequest.offers,referenceToBoard).calculateMaxUtility();
                } else if (strategy == UserStrategy.MostPassengers) {
					bestOffer = new MostPassengersOfferUtilityCalculator(myRequest.offers,referenceToBoard).calculateMaxUtility();
				} else if (strategy == UserStrategy.TimeStressed) {
                	bestOffer = new TimeStressedOfferUtilityCalculator(myRequest.offers,referenceToBoard,this.point,this.target_position).calculateMaxUtility();
				}

				MATCHED = bestOffer.confirmMatch(myRequest);

                if (!MATCHED) {
                    myRequest.offers.remove(0);
                }else{
                	matched_agent = Core.agents.get(myRequest.matchedAgentID);
				}

            }

		}catch (Exception e){
			throw e;
		}

		return MATCHED;
	}

	public void userCooperationStart(Point intersection, int steps){
		intermediate_stop = intersection;
		steps_to_intermediate_stop = steps;
		color = Color.PINK;
		state = USER_STATE.INTERMEDIATE_STOP;
	}

	public void userCooperationEnd(){
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
	public void setCluster(int cluster) {
		this.cluster=cluster;
		
	}
	public int getCluster() {
		return cluster;
	}


}
