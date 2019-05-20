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

                match_state = bestOffer.confirmMatch(myRequest);

                if (!match_state) {
                    myRequest.offers.remove(0);
                }


            }

		}catch (Exception e){
			throw e;
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


}
