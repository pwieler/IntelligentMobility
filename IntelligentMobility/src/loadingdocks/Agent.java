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
	public enum AGENT_STATE {IDLE, AWAITING_CONFIRMATION, PARTLY_BOOKED, FULLY_BOOKED};

	public AGENT_STATE state;

	public int capacity = 4;

	public List<User> confirmed_users = new LinkedList<User>();
	public List<User> passengers = new LinkedList<User>();
	public List<User> tmp_passengers = new LinkedList<User>();

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
		List<Point> tmp_destinations = new LinkedList<Point>();

		for(User u:tmp_passengers){
			tmp_destinations.add(u.intermediate_stop); // tgt_point or intermediate_stop?
		}

		for(User u: confirmed_users){
			if(u.state == User.USER_STATE.PICKED_UP || u.state == User.USER_STATE.DELIVERED){
				pick_ups.add(new Point(-1,-1));
			}else if(u.state == User.USER_STATE.INTERMEDIATE_STOP){
				pick_ups.add(u.intermediate_stop);
			}else{
				pick_ups.add(u.point);
			}

			targets.add(u.target_position);
		}

		Board.Node paths = referenceToBoard.shortestPathComplex(point,pick_ups,targets,tmp_destinations);

		return paths;
	}


	// Core communication
	public boolean confirmMatch(Request user_request){

		if(state == AGENT_STATE.FULLY_BOOKED){
			return false;
		}else{
			user_request.match(this.ID);
			confirmed_users.add(Core.users.get(user_request.userID));

			if(confirmed_users.size()>=capacity){
				setState(AGENT_STATE.FULLY_BOOKED);
			}else{
				setState(AGENT_STATE.PARTLY_BOOKED);
			}

			return true;
		}
	}

	public void unMatch(User u){
		confirmed_users.remove(u);
		tmp_passengers.remove(u);
		passengers.remove(u);
		updateState();
	}

	public void changeMatch(User u){

		if(u.MATCHED){
			u.matched_agent.unMatch(u);
		}

		u.MATCHED = true;
		u.matched_agent = this;

		confirmed_users.add(u);

		updateState();

	}

	public void updateState(){
		if (confirmed_users.size() > 0 && confirmed_users.size() < capacity) {
			setState(AGENT_STATE.PARTLY_BOOKED);
		}else if(confirmed_users.size() == 0) {
			setState(AGENT_STATE.IDLE);
		}else{
			setState(AGENT_STATE.FULLY_BOOKED);
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
			case PARTLY_BOOKED:
				this.state = AGENT_STATE.PARTLY_BOOKED;
				color = Color.CYAN;
				break;
			case FULLY_BOOKED:
				this.state = AGENT_STATE.FULLY_BOOKED;
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

		if(state != AGENT_STATE.FULLY_BOOKED){

			//if agent is idle accept request based on the following
			//minimize "unpaid" time: sort to minimum pickup distance
			Request minDistToPickup = null;
			float minDist = Float.MAX_VALUE;
			//OR maximize guaranteed paid time
			float maxLength = 0.0f;
			float currentLength;
			Request maxPaidTime = null;
			for (Request request : requestList) {
				float currentDist = referenceToBoard.pathLength( referenceToBoard.shortestPath(this.point, request.initPosition));
				if (currentDist < minDist) {
					minDistToPickup = request;
					minDist = currentDist;
				}
				currentLength = referenceToBoard.pathLength( referenceToBoard.shortestPath(request.initPosition, request.targetPosition));
				if(currentLength > maxLength) {
					maxLength = currentLength;
					maxPaidTime = request;
				}
			}
            if(strategy == AgentStrategy.MinUnpaidTime)
			    minDistToPickup.appendOffer(this	);
            else
			    maxPaidTime.appendOffer(this);


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
//				bestFittingRequest.appendOffer(this); //TODO
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
//			first.appendOffer(this);
//			second.appendOffer(this);

			if(state == AGENT_STATE.PARTLY_BOOKED){
				// leave state the same!
			}else{
				setState(AGENT_STATE.AWAITING_CONFIRMATION);
			}


		}else{
			// do nothing, because already full
		}


	}

	public List<Point> routeToList(Board.Node start_node, Point endPoint){
		List<Point> routeList = new LinkedList<Point>();

		Board.Node tmp = start_node;
		while(tmp!=null){
			routeList.add(tmp.getPoint());

			if(tmp.equals(endPoint)){
				break;
			}
			tmp = tmp.parent;
		}

		return routeList;
	}

	public List<Point> checkRouteWithoutPickingUpPassenger(User u_c){

		// Returns route until target_location of user without picking up!
		// --> Later here we can e.g. also decide that its better if the other agent brings the passenger also to the target!

		List<Point> pick_ups = new LinkedList<Point>();
		List<Point> targets = new LinkedList<Point>();
		List<Point> tmp_destinations = new LinkedList<Point>();

		for(User u:tmp_passengers){
			tmp_destinations.add(u.target_position);
		}

		for(User u: confirmed_users){
			if(u.state == User.USER_STATE.PICKED_UP || u.state == User.USER_STATE.DELIVERED || u.equals(u_c)){
				pick_ups.add(new Point(-1,-1));
			}else{
				pick_ups.add(u.point);
			}

			targets.add(u.target_position);
		}

		return routeToList(referenceToBoard.shortestPathComplex(point,pick_ups,targets,tmp_destinations),u_c.target_position);
	}

	public void cooperate(){

		User u = sense(route.parent.getPoint());

		if(u!=null && !confirmed_users.contains(u)){
			System.out.println("now");


			// Calculate peer_agent_route_without picking up!
			List<Point> new_route_peer_agent = new LinkedList<Point>();
			if(u.MATCHED) {
				new_route_peer_agent = u.matched_agent.checkRouteWithoutPickingUpPassenger(u);
			}

			// See where it intersects with our route!
			Board.Node tmp = route;
			Point intersection = point;

			int tmp_capacity = passengers.size() + 1; // Current capacity + the one user that we pick up now!
			int steps = 0;
			int index;

			boolean deliverUser = false;

			while(tmp!=null){

				if(tmp.pickUp){
					tmp_capacity++;
				}
				if(tmp.dropOff){
					tmp_capacity--;
				}

				// Search for an intersection of peer_agent route and our route!
				index = new_route_peer_agent.indexOf(tmp.getPoint());
				if(index!=-1 && index >= steps){
					intersection = tmp.getPoint();
				}

				// If current tmp is the target_location just stop!
				if(tmp.getPoint().equals(u.target_position)){
					intersection = tmp.getPoint();
					deliverUser = true;
					break;
				}

				// If car is too full --> user has to go off!
				if(tmp_capacity>capacity){
					intersection = tmp.getPoint();
					break;
				}

				tmp = tmp.parent;
				steps++;
			}

			// We reach intersection point in "steps"!


			// Decision part!



			if (deliverUser) {
				// This agent delivers the user to its goal --> put him to confirmed_users and add him to passengers!
				// Tell peer agent that he doesn't need to deliver him anymore!
				System.out.println("deliver");

				if(u.MATCHED) {
					System.out.println("matched");
				}else{
					System.out.println("free");
				}

				changeMatch(u);

				if (!passengers.contains(u) && passengers.size() < capacity) {
					passengers.add(u);
					u.userPickedUp();
				}

			} else {
				if (intersection.equals(point)) {
					// do nothing
					// peer agent goes through that cell anyways --> so no need to pick him up!
					// (still it could be good to move him closer to the target and maybe other agents bring him again further!)
					// but:
					// this is the only intersection!! --> so if we move the agent somewhere else the peer agent has to do a detour!
					System.out.println("do nothing");
				} else {
					System.out.println("cooperate");
					// drive user to intersection, drop him there!
					// tell peer agent where to pick user up! (intersection point!)
					if (!passengers.contains(u) && passengers.size() < capacity) {
						passengers.add(u);
						tmp_passengers.add(u);
						u.userCooperationStart(intersection);
					}

					System.out.println("ID: "+ID+" Current: "+point+" Stop: "+intersection);
				}
			}








		}
	}

	public void act(){
		if(state == AGENT_STATE.PARTLY_BOOKED || state == AGENT_STATE.FULLY_BOOKED){

			// Replan
			route = buildRoute();

			if(route != null){
				if(route.parent != null) {

					// Try to cooperate
					cooperate();

					// Move to next route element
					move(route.parent.getPoint());

					// Check if dropOff possible
					if (route.parent.dropOff) {

						Iterator<User> iter = passengers.iterator();
						while (iter.hasNext()) {
							User u = iter.next();
							if ((u.target_position.equals(route.parent.point))||(u.intermediate_stop.equals(route.parent.point))) {
								iter.remove();

								if(u.target_position.equals(route.parent.point)){
									confirmed_users.remove(u);
								}

								tmp_passengers.remove(u);

								if(u.state == User.USER_STATE.INTERMEDIATE_STOP){
									u.userCooperationEnd();
								}else{
									u.userDelivered();
								}
							}
						}

						updateState();
					}

					// Check if pickup possible
					if (route.parent.pickUp) {
						for (User u : confirmed_users) {
							if (u.point.equals(route.parent.point)) {
								if (!passengers.contains(u) && passengers.size()<capacity) {
									passengers.add(u);
									u.userPickedUp();
								}
							}
						}
					}

					// Update route
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
		Board.updateEntityPosition(point,target);
		totalDistanceTraveled++;
		if(!passengers.isEmpty()){
			for(User u:passengers){
				u.moveUser(target);
			}
		}
		point = target;
	}

	public User sense(Point p){
		return referenceToBoard.checkCellForUser(p);
	}



}
