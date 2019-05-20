package loadingdocks;

import java.awt.Color;
import java.awt.Point;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;



public class Agent extends Entity {

	static int id_count = 0;
	int ID;
    AgentStrategy strategy;
	public enum AGENT_STATE {IDLE,AWAITING_CONFIRMATION,OCCUPIED,FULL};

	public AGENT_STATE state;

	public int capacity = 4;

	public List<User> confirmed_users = new LinkedList<User>();
	public List<User> passengers = new LinkedList<User>();
	Board.Node route=null;

	private MobType type;
	private Board referenceToBoard;
	public int direction = 90;
	private int totalDistanceTraveled = 0;



	public Agent(Point point, Color color,MobType pType, int countUsers, Board boardReference){
		super(point, color);
		ID = id_count++;
		Core.registerToCore(this);
		type = pType;
		this.referenceToBoard = boardReference;
		strategy = AgentStrategy.MinUnpaidTime;

		setState(AGENT_STATE.IDLE);
	}

    public Agent(Point point, Color color,MobType pType, int countUsers, Board boardReference, AgentStrategy agentStrategy ) {
        this(point,color,pType,countUsers,boardReference);
        strategy = agentStrategy;
    }


	public Board.Node buildRoute(){

		List<Point> pick_ups = new LinkedList<Point>();
		List<Point> targets = new LinkedList<Point>();

		for(User u: confirmed_users){
			if(u.state == User.USER_STATE.PICKED_UP || u.state == User.USER_STATE.DELIVERED){
				pick_ups.add(new Point(-1,-1));
			}else{
				pick_ups.add(u.point);
			}

			targets.add(u.target_position);
		}

		Board.Node paths = referenceToBoard.shortestPathComplex(point,pick_ups,targets);

		return paths;
	}


	// Core communication
	public boolean confirmMatch(Request user_request){

		if(state == AGENT_STATE.FULL){
			return false;
		}else{
			user_request.match(this.ID);
			confirmed_users.add(Core.users.get(user_request.userID));

			if(confirmed_users.size()>=capacity){
				setState(AGENT_STATE.FULL);
			}else{
				setState(AGENT_STATE.OCCUPIED);
			}



			return true;
		}
	}

	public void setState(AGENT_STATE state){
		switch(state){
			case IDLE:
				this.state = AGENT_STATE.IDLE;
				color = Color.BLACK;
				break;
			case AWAITING_CONFIRMATION:
				this.state = AGENT_STATE.AWAITING_CONFIRMATION;
				color = Color.GRAY;
				break;
			case OCCUPIED:
				this.state = AGENT_STATE.OCCUPIED;
				color = Color.CYAN;
				break;
			case FULL:
				this.state = AGENT_STATE.FULL;
				color = Color.YELLOW;
				break;
			default:
				break;
		}
	}

	public void receiveRequests(List<Request> requestList) {

		if(requestList.isEmpty()){
			return;
		}

		if(state != AGENT_STATE.FULL){

		    if(confirmed_users.isEmpty()) {
                if (strategy == AgentStrategy.MinUnpaidTime) {
                    MinUnpaidTimeRequestUtilityCalculator calc = new MinUnpaidTimeRequestUtilityCalculator(requestList, referenceToBoard, this.point);
                    calc.calculateMaxUtilityRequest().appendOffer(this);
                }
                else {
                    MaxPaidTimeRequestUtilityCalculator calc = new MaxPaidTimeRequestUtilityCalculator(requestList,referenceToBoard);
                    calc.calculateMaxUtilityRequest().appendOffer(this);
                }
            } else {

		        
                //assuming the agent already has an accepted request and thinks about accepting another one:
                Request oldRequest = new Request(Integer.MAX_VALUE,new Point(),new Point()); //TODO assume this request is the current one

                //"protect" old request: compare old route to the one including the new user
                //compare old route "firstPickup,firstDrop" with new route "firstPickup,secondPickup,firstDrop,secondDrop"
                //TODO for simplicity assume this new route is the shortest possible one (not e.g. "secondPickup,firstPickup,firstDrop,secondDrop")
                //float myPosToFirstPickupDist = pathLength(shortestPath(point,oldRequest.initPosition));
                float firstPickupToFirstDropDist = referenceToBoard.pathLength(referenceToBoard.shortestPath(oldRequest.initPosition,oldRequest.targetPosition));
                Request bestFittingRequest = null;
                float minIncludingNewRequest = Float.MAX_VALUE;
                for (Request newRequest : requestList) {
                    float firstPickupToSecondPickupDist = referenceToBoard.pathLength(referenceToBoard.shortestPath(oldRequest.initPosition,newRequest.initPosition));
                    float secondPickupToFirstDropDist = referenceToBoard.pathLength(referenceToBoard.shortestPath(newRequest.initPosition,oldRequest.targetPosition));
                    float includingNewPickup = firstPickupToSecondPickupDist + secondPickupToFirstDropDist;
                    if( includingNewPickup < minIncludingNewRequest) { // found a more fitting route combining the two requests
                        bestFittingRequest = newRequest;
                        minIncludingNewRequest = includingNewPickup;
                    }
                }

                float aThreshold = 100.0f; //TODO define threshold somewhere else
                //found the best fitting new request. is the first user not annoyed ?
                if( minIncludingNewRequest - firstPickupToFirstDropDist <  aThreshold) {
                    bestFittingRequest.appendOffer(this); //TODO

                }

                //do not protect old request: just find the best combination of 2 (or n?),
                //with best combination meaning maximum occupancy
                Request first = null;
                Request second = null;
                float distanceSharing = 0.0f;
                for(Request request1 : requestList) {
                    for(Request request2 : requestList) {
                        //TODO still assume that (first pickup, second pickup, first drop, second drop) is optimal
                        float secondPickupToFirstDropDist = referenceToBoard.pathLength(referenceToBoard.shortestPath(request2.initPosition,request1.targetPosition));
                        if(secondPickupToFirstDropDist > distanceSharing) {
                            first = request1;
                            second = request2;
                            distanceSharing = secondPickupToFirstDropDist;
                        }
                    }
                }
                //have found two requests with maximum shared distance.
                // TODO but, is this a feasible ride ? probably no...
                first.appendOffer(this);
                second.appendOffer(this);
            }

			if(state == AGENT_STATE.OCCUPIED){
				// leave state the same!
			}else{
				setState(AGENT_STATE.AWAITING_CONFIRMATION);
			}


		}else{
			// do nothing, because already full
		}


	}

	public void act(){
		if(state == AGENT_STATE.OCCUPIED || state == AGENT_STATE.FULL){

			route = buildRoute();

			if(route != null){
				if(route.parent != null) {

					move(route.parent.getPoint());

					if (route.parent.pickUp) {
						for (User u : confirmed_users) {
							if (u.point.equals(route.parent.point)) {
								if (!passengers.contains(u)) {
									passengers.add(u);
									u.userPickedUp();
								}
							}
						}
					}

					if (route.parent.dropOff) {

						Iterator<User> iter = passengers.iterator();
						while (iter.hasNext()) {
							User u = iter.next();
							if (u.target_position.equals(route.parent.point)) {
								iter.remove();
								confirmed_users.remove(u);
								u.userDelivered();
							}
						}

						if (confirmed_users.size() > 0) {
							setState(AGENT_STATE.OCCUPIED);
						} else {
							setState(AGENT_STATE.IDLE);
						}
					}


					route = route.parent;
					if (route.parent == null) {
						route = null;
					}
				}
			}

		}
	}

	public int getTotalDistance() {
		return this.totalDistanceTraveled;
	}
	
	/* Move agent forward */
	public void move(Point target) {
		rotate(point,target);
		Board.updateEntityPosition(point,target);
		totalDistanceTraveled++;
		if(!passengers.isEmpty()){
			for(User u:passengers){
				u.moveUser(target);
			}
		}
		point = target;
	}
	
	private void rotate(Point p1, Point p2) {
		boolean vertical = Math.abs(p1.x-p2.x)<Math.abs(p1.y-p2.y);
		boolean upright = vertical ? p1.y<p2.y : p1.x<p2.x;
		if(vertical) {  
			if(upright) { //move up
				if(direction!=0) direction=0 ;
			} else if(direction!=180) direction=180 ;
		} else {
			if(upright) { //move right
				if(direction!=90) direction= 90 ;
			} else if(direction!=270) direction=270 ;
		}
	}


}
