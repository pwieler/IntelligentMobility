package loadingdocks;

import java.awt.Color;
import java.awt.Point;
import java.util.LinkedList;
import java.util.List;

/**
 * Agent behavior
 * @author Rui Henriques
 */
public class Agent extends Entity {

	static int id_count = 0;
	int ID;

	public enum AGENT_STATE {IDLE,OCCUPIED,AWAITING_CONFIRMATION};

	public int direction = 90;

	public List<User> confirmed_users = new LinkedList<User>();
	public List<User> passengers = new LinkedList<User>();
	Board.Node route=null;



	public AGENT_STATE state = AGENT_STATE.IDLE;

	private MobType type;
	private Board referenceToBoard;
	public Agent(Point point, Color color,MobType pType, int countUsers, Board boardReference){
		super(point, color);
		ID = id_count++;
		Core.registerToCore(this);
		type = pType;
		this.referenceToBoard = boardReference;
	}

	public Board.Node buildRoute(){

		List<Point> pick_ups = new LinkedList<Point>();
		List<Point> targets = new LinkedList<Point>();

		for(User u: confirmed_users){
			pick_ups.add(u.point);
			targets.add(u.target_position);
		}

		Board.Node paths = referenceToBoard.shortestPath(point,pick_ups,targets);

		return paths;
	}


	// Core communication
	public boolean confirmMatch(Request user_request){

		if(state == AGENT_STATE.OCCUPIED){
			return false;
		}else{
			user_request.match(this.ID);
			state = AGENT_STATE.OCCUPIED;
			confirmed_users.add(Core.users.get(user_request.userID));

			route = buildRoute();

			return true;
		}
	}

	public void receiveRequests(List<Request> requestList) {



		if(state == AGENT_STATE.IDLE || state == AGENT_STATE.AWAITING_CONFIRMATION){

			//if agent is idle (state remains unimplemented), accept request based on the following
			//minimize "unpaid" time: sort to minimum pickup distance
			Request minDistToPickup = null;
			float minDist = Float.MAX_VALUE;
			//OR maximize guaranteed paid time
			float maxLength = 0.0f;
			float currentLength;
			Request maxPaidTime = null;
			for (Request request : requestList) {
				float currentDist = (float)this.point.distance(request.initPosition);
				if (currentDist < minDist) {
					minDistToPickup = request;
					minDist = currentDist;
				}
				currentLength = (float) request.initPosition.distance(request.targetPosition);
				if(currentLength > maxLength) {
					maxLength = currentLength;
					maxPaidTime = request;
				}
			}
			//TODO accept them both?
			minDistToPickup.appendOffer(this.ID	);
			maxPaidTime.appendOffer(this.ID);

//			//assuming the agent already has an accepted request and thinks about accepting another one:
//			Request oldRequest = new Request(Integer.MAX_VALUE,new Point(),new Point()); //TODO assume this request is the current one
//
//			//"protect" old request: compare old route to the one including the new user
//			//compare old route "firstPickup,firstDrop" with new route "firstPickup,secondPickup,firstDrop,secondDrop"
//			//TODO for simplicity assume this new route is the shortest possible one (not e.g. "secondPickup,firstPickup,firstDrop,secondDrop")
//			//float myPosToFirstPickupDist = pathLength(shortestPath(point,oldRequest.initPosition));
//			float firstPickupToFirstDropDist = referenceToBoard.pathLength(referenceToBoard.shortestPath(oldRequest.initPosition,oldRequest.targetPosition));
//			Request bestFittingRequest = null;
//			float minIncludingNewRequest = Float.MAX_VALUE;
//			for (Request newRequest : requestList) {
//				float firstPickupToSecondPickupDist = referenceToBoard.pathLength(referenceToBoard.shortestPath(oldRequest.initPosition,newRequest.initPosition));
//				float secondPickupToFirstDropDist = referenceToBoard.pathLength(referenceToBoard.shortestPath(newRequest.initPosition,oldRequest.targetPosition));
//				float includingNewPickup = firstPickupToSecondPickupDist + secondPickupToFirstDropDist;
//				if( includingNewPickup < minIncludingNewRequest) { // found a more fitting route combining the two requests
//					bestFittingRequest = newRequest;
//					minIncludingNewRequest = includingNewPickup;
//				}
//			}
//
//			float aThreshold = 100.0f; //TODO define threshold somewhere else
//			//found the best fitting new request. is the first user not annoyed ?
//			if( minIncludingNewRequest - firstPickupToFirstDropDist <  aThreshold) {
//				bestFittingRequest.appendOffer(this.ID); //TODO
//
//			}
//
//			//do not protect old request: just find the best combination of 2 (or n?),
//			//with best combination meaning maximum occupancy
//			Request first = null;
//			Request second = null;
//			float distanceSharing = 0.0f;
//			for(Request request1 : requestList) {
//				for(Request request2 : requestList) {
//					//TODO still assume that (first pickup, second pickup, first drop, second drop) is optimal
//					float secondPickupToFirstDropDist = referenceToBoard.pathLength(referenceToBoard.shortestPath(request2.initPosition,request1.targetPosition));
//					if(secondPickupToFirstDropDist > distanceSharing) {
//						first = request1;
//						second = request2;
//						distanceSharing = secondPickupToFirstDropDist;
//					}
//				}
//			}
//			//have found two requests with maximum shared distance.
//			// TODO but, is this a feasible ride ? probably no...
//			first.appendOffer(this.ID);
//			second.appendOffer(this.ID);

			state = AGENT_STATE.AWAITING_CONFIRMATION;

		}else{
			// do nothing, because already occupied
		}


	}

	public void followRoute(){
		if(state == AGENT_STATE.OCCUPIED){

			if(route != null){
				if(route.parent.pickUp){
					for(User u: confirmed_users){
						if(u.point==route.parent.point){
							passengers.add(u);
						}
					}
				}

				if(route.parent.dropOff){
					for(User u: confirmed_users){
						if(u.point==route.parent.point){
							passengers.remove(u);
						}
					}
				}


				move(route.parent.getPoint());
				route = route.parent;
				if(route.parent == null){
					route = null;
				}
			}

		}
	}

	/* Move agent forward */
	public void move(Point target) {
		Board.updateEntityPosition(point,target);
		if(!passengers.isEmpty()){
			for(User u:passengers){
				u.moveUser(target);
			}
		}
		point = target;
	}



}
