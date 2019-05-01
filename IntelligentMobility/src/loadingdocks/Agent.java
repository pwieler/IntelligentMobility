package loadingdocks;

import java.awt.Color;
import java.awt.Point;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Stack;

import loadingdocks.Agent.Action;
import loadingdocks.Agent.Desire;
import loadingdocks.Block.Type;

/**
 * Agent behavior
 * @author Rui Henriques
 */
public class Agent extends Entity {

	public enum Desire {pickup, drop, stop }
	public enum Action { moveAhead, pickup, stop, drop, rotateRight, rotateLeft}

	public int id ;
	
	public MobType type;
	public int speed;
	public int price;
	public int direction = 90;
	public User cargo;
	
	public Point initialPoint;
	public int usersDelivered, usersToPickUp;
	public Map<Point,Block> warehouse; //internal map of the warehouse
	public List<Point> freeTargetLocations; //free shelves
	public List<Point> pickUpCells; //pickup cells with boxes
	
	public List<Desire> desires;
	public AbstractMap.SimpleEntry<Desire,Point> intention;
	public Queue<Action> plan;
	//public List<Belief> beliefs;
	
	private Point ahead;
	public Point currentPosition;
	
	public int max_users;
	public ArrayList<User> users = new ArrayList<User>();
	
	public Map<Point,Block> map; //internal map of the warehouse
	public List<Point> destinations;
	
	
	public Agent(Point point, Color color,MobType type , int countUsers){
		super(point, color);
		this.type = type;
		initialPoint = point;
		usersDelivered = 0;
		usersToPickUp = countUsers;
		freeTargetLocations = new ArrayList<Point>();
		pickUpCells = new ArrayList<Point>();
		warehouse = new HashMap<Point,Block>();
		plan = new LinkedList<Action>();
	} 
	
	/**********************
	 **** A: decision ***** 
	 **********************/
		
	public void agentDecision() {

		updateBeliefs();
		
		if(!plan.isEmpty() && !succeededIntention() && !impossibleIntention()){
			Action action = plan.remove();
            if(isPlanSound(action)) execute(action); 
            else rebuildPlan();
            if(reconsider()) deliberate();
            
		} else {
			deliberate();
			buildPlan();
			if(plan.isEmpty()) agentReactiveDecision();
		}
	}		

	private void deliberate() {
		
		desires = new ArrayList<Desire>(); 
		if(cargo()) desires.add(Desire.drop); 
		if(usersToPickUp > 0) desires.add(Desire.pickup);
		if(usersToPickUp == 0) desires.add(Desire.stop);
		intention = new AbstractMap.SimpleEntry<>(desires.get(0),null);
		
		switch(intention.getKey()) { //high-priority desire
			case pickup:
				if(!pickUpCells.isEmpty()) intention.setValue(pickUpCells.get(0));
				break;
			case drop : 
				Color boxcolor = cargoColor();
				for(Point shelf : freeTargetLocations)
					if(warehouse.get(shelf).color.equals(boxcolor)) {
						intention.setValue(shelf);
						break;
					}
				break;
			case stop : intention.setValue(initialPoint);
		}
	}

	private void buildPlan() {
		plan = new LinkedList<Action>();
		if(intention.getValue()==null) return;
		switch(intention.getKey()) {
			case pickup:
				plan = buildPathPlan(point,intention.getValue());
				plan.add(Action.pickup);
				break;
			case drop : 
				plan = buildPathPlan(point,intention.getValue());
				plan.add(Action.drop);
				break;
			case stop : 
				plan = buildPathPlan(point,intention.getValue());
				plan.add(Action.moveAhead);
		}
	}

	private void rebuildPlan() { 
		plan = new LinkedList<Action>();
		for(int i=0; i<4; i++) agentReactiveDecision(); //attempt to come out of a conflict with full plan
	}

	private boolean isPlanSound(Action action) {
		switch(action) {
			case moveAhead : return isFreeCell();
			case pickup: return isBoxAhead();
			case drop : return isShelf() && !isBoxAhead() && shelfColor().equals(cargoColor());
			default : return true;
		}		
	}

	private void execute(Action action) {
		switch(action) {
			case moveAhead : moveAhead(); return;
			case rotateRight : rotateRight(); return;
			case rotateLeft : rotateLeft(); return;
			case pickup: grabBox(); return;
			case drop : dropBox(); return;
		}		
	}

	private boolean impossibleIntention() {
		if(intention.getKey().equals(Desire.pickup)) return usersToPickUp == 0;
		else return false;
	}

	private boolean succeededIntention() {
		switch(intention.getKey()) {
			case pickup: return cargo();
			case drop : return !cargo();
			case stop : return point.equals(initialPoint);
		}
		return false;
	}
	
	private boolean reconsider() {
		return false;
	}

	/*******************************/
	/**** B: reactive behavior ****/
	/*******************************/

	public void agentReactiveDecision() {
	  ahead = aheadPosition();
	  if(isWall()) rotateRandomly();
	  else if(isRamp() && isBoxAhead() && !cargo()) grabBox();
	  else if(canDropBox()) dropBox();
	  else if(!isFreeCell()) rotateRandomly();
	  else if(random.nextInt(5) == 0) rotateRandomly();
	  else moveAhead();
	}
	
	/**************************/
	/**** C: communication ****/
	/**************************/
	
	private void updateBeliefs() {
		ahead = aheadPosition();
		if(isWall() || isRoomFloor()) return;
		if(isRamp()) Board.sendMessage(ahead, cellType(), cellColor(), cargo() ? !isBoxAhead() : true);
		else if(canDropBox()) Board.sendMessage(ahead, cellType(), cellColor(), false);
		else Board.sendMessage(ahead, cellType(), cellColor(), !isBoxAhead());
	}

	public void receiveMessage(Point point, Type type, Color color, Boolean free) {
		warehouse.put(point, new Block(type,color));
		if(type.equals(Type.target_location)) {
			if(free) freeTargetLocations.add(point);
			else freeTargetLocations.remove(point);
		}
		else if(type.equals(Type.pickup)) {
			if(free) pickUpCells.remove(point);
			else pickUpCells.add(point);
		}
	}
	
	public void receiveMessage(Action action, Point pt) {
		if(action.equals(Action.drop)) {
			usersDelivered++;
			freeTargetLocations.remove(pt);
		} else if(action.equals(Action.pickup)) {
			usersToPickUp--;
			pickUpCells.remove(pt);
		}
	} 

	
	/*******************************/
	/**** D: planning auxiliary ****/
	/*******************************/

	private Queue<Action> buildPathPlan(Point p1, Point p2) {
		Stack<Point> path = new Stack<Point>();
		Node node = shortestPath(p1,p2);
		path.add(node.point);
		while(node.parent!=null) {
			node = node.parent;
			path.push(node.point);
		}
		Queue<Action> result = new LinkedList<Action>();
		p1 = path.pop();
		int auxdirection = direction;
		while(!path.isEmpty()) {
			p2 = path.pop();
			result.add(Action.moveAhead);
			result.addAll(rotations(p1,p2));
			p1 = p2;
		}
		direction = auxdirection;
		result.remove();
		return result;
	}
	
	private List<Action> rotations(Point p1, Point p2) {
		List<Action> result = new ArrayList<Action>();
		while(!p2.equals(aheadPosition())) {
			Action action = rotate(p1,p2);
			if(action==null) break;
			execute(action);
			result.add(action);
		}
		return result;
	}

	private Action rotate(Point p1, Point p2) {
		boolean vertical = Math.abs(p1.x-p2.x)<Math.abs(p1.y-p2.y);
		boolean upright = vertical ? p1.y<p2.y : p1.x<p2.x;
		if(vertical) {  
			if(upright) { //move up
				if(direction!=0) return direction==90 ? Action.rotateLeft : Action.rotateRight;
			} else if(direction!=180) return direction==90 ? Action.rotateRight : Action.rotateLeft;
		} else {
			if(upright) { //move right
				if(direction!=90) return direction==180 ? Action.rotateLeft : Action.rotateRight;
			} else if(direction!=270) return direction==180 ? Action.rotateRight : Action.rotateLeft;
		}
		return null;
	}
	
	/********************/
	/**** E: sensors ****/
	/********************/
	
	/* Check if agent is carrying box */
	public boolean cargo() {
		return cargo != null;
	}
	
	/* Return the color of the box */
	public Color cargoColor() {
	  return cargo.color;
	}

	/* Return the color of the target_location ahead or 0 otherwise */
	public Color shelfColor(){
		return Board.getBlock(ahead).color;
	}

	/* Check if the cell ahead is floor (which means not a wall, not a target_location nor a pickup) and there are any robot there */
	public boolean isFreeCell() {
	  return isRoomFloor() && Board.getEntity(ahead)==null;
	}

	public boolean isRoomFloor() {
		return Board.getBlock(ahead).type.equals(Type.free);
	}
	
	/* Check if the cell ahead contains a box */
	public boolean isBoxAhead(){
		Entity entity = Board.getEntity(ahead);
		return entity!=null && entity instanceof User;
	}

	/* Return the type of cell */
	public Type cellType() {
	  return Board.getBlock(ahead).type;
	}

	/* Return the color of cell */
	public Color cellColor() {
	  return Board.getBlock(ahead).color;
	}

	/* Check if the cell ahead is a target_location */
	public boolean isShelf() {
	  Block block = Board.getBlock(ahead);
	  return block.type.equals(Type.target_location);
	}

	/* Check if the cell ahead is a pickup */
	public boolean isRamp(){
	  Block block = Board.getBlock(ahead);
	  return block.type.equals(Type.pickup);
	}

	/* Check if the cell ahead is a wall */
	private boolean isWall() {
		return ahead.x<0 || ahead.y<0 || ahead.x>=Board.nX || ahead.y>=Board.nY;
	}

	/* Check if the cell ahead is a wall */
	private boolean isWall(int x, int y) {
		return x<0 || y<0 || x>=Board.nX || y>=Board.nY;
	}
	
	/* Check if we can drop a box in the target_location ahead */
	private boolean canDropBox() {
		return isShelf() && !isBoxAhead() && cargo() && shelfColor().equals(cargoColor());
	}


	/**********************/
	/**** F: actuators ****/
	/**********************/

	/* Rotate agent to right */
	public void rotateRandomly() {
		if(random.nextBoolean()) rotateLeft();
		else rotateRight();
	}
	
	/* Rotate agent to right */
	public void rotateRight() {
		direction = (direction+90)%360;
	}
	
	/* Rotate agent to left */
	public void rotateLeft() {
		direction = (direction-90+360)%360;
	}
	
	/* Move agent forward */
	public void moveAhead() {
		Board.updateEntityPosition(point,ahead);
		if(cargo()) cargo.moveUser(ahead);
		point = ahead;
	}

	/* Cargo box */
	public void grabBox() {
	  cargo = (User) Board.getEntity(ahead);
	  cargo.pickupUser(point);
	  Board.sendMessage(Action.pickup, ahead);
	}

	/* Drop box */
	public void dropBox() {
		cargo.dropUser(ahead);
	    cargo = null;
		Board.sendMessage(Action.drop, ahead); 
	}
	
	/**********************/
	/**** G: auxiliary ****/
	/**********************/

	/* Position ahead */
	private Point aheadPosition() {
		Point newpoint = new Point(point.x,point.y);
		switch(direction) {
			case 0: newpoint.y++; break;
			case 90: newpoint.x++; break;
			case 180: newpoint.y--; break;
			default: newpoint.x--; 
		}
		return newpoint;
	}
	
	//For queue used in BFS 
	public class Node { 
	    Point point;   
	    Node parent; //cell's distance to source 
	    public Node(Point point, Node parent) {
	    	this.point = point;
	    	this.parent = parent;
	    }
	    public String toString() {
	    	return "("+point.x+","+point.y+")";
	    }
	} 
	
	public Node shortestPath(Point src, Point dest) { 
	    boolean[][] visited = new boolean[100][100]; 
	    visited[src.x][src.y] = true; 
	    Queue<Node> q = new LinkedList<Node>(); 
	    q.add(new Node(src,null)); //enqueue source cell 
	    
		//access the 4 neighbours of a given cell 
		int row[] = {-1, 0, 0, 1}; 
		int col[] = {0, -1, 1, 0}; 
	     
	    while (!q.isEmpty()){//do a BFS 
	        Node curr = q.remove(); //dequeue the front cell and enqueue its adjacent cells
	        Point pt = curr.point; 
			//System.out.println(">"+pt);
	        for (int i = 0; i < 4; i++) { 
	            int x = pt.x + row[i], y = pt.y + col[i]; 
    	        if(x==dest.x && y==dest.y) return new Node(dest,curr); 
	            if(!isWall(x,y) && !warehouse.containsKey(new Point(x,y)) && !visited[x][y]){ 
	                visited[x][y] = true; 
	    	        q.add(new Node(new Point(x,y), curr)); 
	            } 
	        }
	    }
	    return null; //destination not reached
	}
}
