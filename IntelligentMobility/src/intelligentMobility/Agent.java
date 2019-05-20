package intelligentMobility;

import org.apache.commons.math3.stat.clustering.Cluster;

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

	public int DELIVER_MODE_USERS = 0;

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
		strategy = AgentStrategy.ClusterBased;

		setState(AGENT_STATE.IDLE);
	}

    public Agent(Point point, Color color,MobType pType, int countUsers, Board boardReference, AgentStrategy agentStrategy ) {
        this(point,color,pType,countUsers,boardReference);
        strategy = agentStrategy;
    }

	public void augmentRoute(){

		List<Point> pickups = new LinkedList<Point>();
		List<Point> targets = new LinkedList<Point>();

		for(User u:tmp_passengers){
			targets.add(u.intermediate_stop); // tgt_point or intermediate_stop?
		}

		for(User u:passengers){
			targets.add(u.target_position); // tgt_point or intermediate_stop?
		}

		for(User u: confirmed_users){
			if(u.state == User.USER_STATE.PICKED_UP || u.state == User.USER_STATE.DELIVERED){
				pickups.add(new Point(-1,-1));
			}else if(u.state == User.USER_STATE.INTERMEDIATE_STOP){
				pickups.add(u.intermediate_stop);
			}else{
				pickups.add(u.point);
			}

			targets.add(u.target_position);
		}

		Board.Node tmp = route;
		while(tmp!=null){

			if(pickups.contains(tmp.point)){
				tmp.setPickUp();
			}
			if(targets.contains(tmp.point)){
				tmp.setDropOff();
			}

			tmp = tmp.parent;
		}
	}


    public Board.Node potentialNewRoute(Point includingMyPickup, Point includingMyDropoff) {
		List<Point> pickups = new LinkedList<Point>();
		pickups.add(includingMyPickup);
		List<Point> targets = new LinkedList<Point>();
		targets.add(includingMyDropoff);
		return buildRoute(pickups,targets);
	}

	/**
	 *
	 * @param pick_ups a list containing other pickup locations than this' confirmed_users' pickups
	 * @param targets a list containing other target locations than this' confirmed_users' targets
	 * @return the route this agent would take to pickup and drop all confirmed_users + the given positions
	 */
	public Board.Node buildRoute(List<Point> pick_ups,List<Point> targets){

		DELIVER_MODE_USERS = 0;
		for(User u_p : passengers){
			if(u_p.DELIVER_MODE){
				DELIVER_MODE_USERS++;
			}
		}

		if(DELIVER_MODE_USERS>0){
			return route;
		}

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
			confirmed_users.add(user_request.user);

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

		//confirmed_users.add(u);

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
		if(passengers.size()>=capacity){
			color = Color.YELLOW;
		}else if(passengers.size()>0){
			color = Color.CYAN;
		}else{
			color = Color.GRAY;
		}
		switch(state){
			case IDLE:
				this.state = AGENT_STATE.IDLE;
				color = Color.BLACK;
				break;
			case AWAITING_CONFIRMATION:
				this.state = AGENT_STATE.AWAITING_CONFIRMATION;
				//color = Color.GRAY;
				break;
			case PARTLY_BOOKED:
				this.state = AGENT_STATE.PARTLY_BOOKED;
				//color = Color.CYAN;
				break;
			case FULLY_BOOKED:
				this.state = AGENT_STATE.FULLY_BOOKED;
				//color = Color.YELLOW;
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

		    if(confirmed_users.isEmpty()) {
                if (strategy == AgentStrategy.MinUnpaidTime) {
                    MinUnpaidTimeRequestUtilityCalculator calc = new MinUnpaidTimeRequestUtilityCalculator(requestList, referenceToBoard, this.point);
                    calc.calculateMaxUtility().appendOffer(this);
                }
                else { //choose longest trip
                    MaxPaidTimeRequestUtilityCalculator calc = new MaxPaidTimeRequestUtilityCalculator(requestList,referenceToBoard);
                    calc.calculateMaxUtility().appendOffer(this);
                }
            } else { //we already have a passenger
		    	if (strategy == AgentStrategy.ClusterBased) {
					ClusterBasedRequestUtilityCalculator calc = new ClusterBasedRequestUtilityCalculator(requestList,referenceToBoard,confirmed_users);
					calc.calculateMaxUtility().appendOffer(this);
				} //else dont make offfer


            }

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

	public List<Point> checkRouteWithoutPickingUpPassenger(User u_c, Point intermediate_stop){

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
				if(u.equals(u_c) && intermediate_stop!=null){
					pick_ups.add(intermediate_stop);
				}else{
					pick_ups.add(new Point(-1,-1));
				}
			}else{
				pick_ups.add(u.point);
			}

			targets.add(u.target_position);
		}

		return routeToList(referenceToBoard.shortestPathComplex(point,pick_ups,targets,tmp_destinations),u_c.target_position);
	}

	public void cooperate(User u, boolean ITERATIVE){


		// Replan with the temporary users!
		if(ITERATIVE){
			List<User> tmp_passengers_copy = new LinkedList<>();
			tmp_passengers_copy.addAll(tmp_passengers);

			for(User u_tmp:tmp_passengers){
				passengers.remove(u_tmp);
				u_tmp.userCooperationEnd();
			}
			tmp_passengers.clear();

			for(User u_tmp:tmp_passengers_copy){
				cooperate(u_tmp, false);
			}
		}

		// Set correct pickups and targets to make correct cooperation planning
		augmentRoute();


		if(u!=null && !confirmed_users.contains(u)){

			// Calculate peer_agent_route_without picking up!
			List<Point> new_route_peer_agent = new LinkedList<Point>();
			if(u.MATCHED) {
				new_route_peer_agent = u.matched_agent.checkRouteWithoutPickingUpPassenger(u,null);
			}

			// Calculate shortestPath of user
			List<Point> shortest_path_user_target = routeToList(referenceToBoard.shortestPath(point,u.target_position),null);

			// See where it intersects with our route!
			Board.Node tmp = route.parent;
			Point intersection = point;

			int tmp_capacity = passengers.size() + 1; // Current capacity + the one user that we pick up now!
			int steps = 0;
			int index;

			boolean deliverUser = false;

			int distance_to_goal = 9999;

			while(tmp!=null){

				if(tmp.pickUp){
					tmp_capacity++;
				}
				if(tmp.dropOff){
					tmp_capacity--;
				}

				// If track differs from shortestPath, stop. But: if we can find intersection with peer_agent_route maybe we should go there --> lets see!
				// Calc always shortestDistance if getting closer go there!
//				try{
//					if(tmp.getPoint()==shortest_path_user_target.get(steps)){
//						intersection = tmp.getPoint();
//					}
//				}catch (Exception e){
//
//				}

				int tmp_distance_to_goal = routeToList(referenceToBoard.shortestPath(tmp.getPoint(),u.target_position),null).size();
				if(tmp_distance_to_goal<distance_to_goal){
					//intersection = tmp.getPoint();
					distance_to_goal = tmp_distance_to_goal;
					if(u.MATCHED){
						List<Point> route_with_intermediate_stop_peer_agent = u.matched_agent.checkRouteWithoutPickingUpPassenger(u,tmp.getPoint());
						// It is possible to bring user there! with shorter distance_to_goal then at the moment! --> check if route makes sense for peer agent!
						index = route_with_intermediate_stop_peer_agent.indexOf(tmp.getPoint());
						if(index!=-1 && index >= steps){
							intersection = tmp.getPoint();
						}else{

						}
					}else{
						intersection = tmp.getPoint();
					}


				}


				// Search for an intersection of peer_agent route and our route! <-- earlier intersection would be better!
				index = new_route_peer_agent.indexOf(tmp.getPoint());
				if(index!=-1 && index >= steps){
					intersection = tmp.getPoint();
				}

				// If current tmp is the target_location just stop!
				if(tmp.getPoint().equals(u.target_position)){
					intersection = tmp.getPoint();
					deliverUser = true;
					DELIVER_MODE_USERS++;
					break;
				}

				// If car is too full --> user has to go off!
				if(tmp_capacity>capacity){
					//intersection = tmp.getPoint();
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

				u.DELIVER_MODE = true;

				changeMatch(u);

				if (!passengers.contains(u) && passengers.size() < capacity) {
					passengers.add(u);
					u.userPickedUp();
				}

				augmentRoute();

			} else {
				if (intersection.equals(point)) {
					// do nothing
					// peer agent goes through that cell anyways --> so no need to pick him up!
					// (still it could be good to move him closer to the target and maybe other agents bring him again further!)
					// but:
					// this is the only intersection!! --> so if we move the agent somewhere else the peer agent has to do a detour!
				} else {
					// drive user to intersection, drop him there!
					// tell peer agent where to pick user up! (intersection point!)
					if (!passengers.contains(u) && passengers.size() < capacity) {
						passengers.add(u);
						tmp_passengers.add(u);
						u.userCooperationStart(intersection, steps);
					}
				}
			}

		}

	}



	public void act(){
		if(state == AGENT_STATE.PARTLY_BOOKED || state == AGENT_STATE.FULLY_BOOKED){

			// Replan
			route = buildRoute(new LinkedList<Point>(), new LinkedList<Point>());


			if(route != null){
				if(route.parent != null) {

					// Try to cooperate
					User u_sense = sense(route.parent.getPoint());
					cooperate(u_sense, true);

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
									boolean USER_WAS_IN_DELIVER_MODE = u.userDelivered();

									if(USER_WAS_IN_DELIVER_MODE){
										DELIVER_MODE_USERS--;
									}
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

					updateState();
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

	public User sense(Point p){
		return referenceToBoard.checkCellForUser(p);
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
